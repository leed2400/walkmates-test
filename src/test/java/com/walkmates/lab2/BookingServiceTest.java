package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.BookingStatus;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Provider;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.repository.BookingRepository;
import com.walkmates.repository.ListingRepository;
import com.walkmates.repository.ProviderRepository;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.BookingService;
import com.walkmates.service.BookingService.BookingRejectedException;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lab 2, Activity 4.2 — Component isolation for {@link BookingService}.
 *
 * <p>Mocks repositories and seams (pricing, notification) to test booking creation,
 * confirmation notifications, and the booking-limit boundary rule (FR-4.4 rule 2).</p>
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SeekerRepository seekers;

    @Mock
    private ListingRepository listings;

    @Mock
    private ProviderRepository providers;

    @Mock
    private BookingRepository bookings;

    @Mock
    private PricingCalculator pricing;

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private BookingService bookingService;

    private Seeker createSeeker(String id, TrustTier tier, double balance) {
        Seeker s = new Seeker("seeker@example.com", "Seeker Name", "0701234567");
        s.setTrustTier(tier);
        s.addFunds(balance);
        return s;
    }

    private Listing createListing(String id, String providerId) {
        return new Listing(providerId, "Dog walking", "Nice walk", ListingType.DOG_WALK);
    }

    private Provider createProvider(String id, int capacity) {
        return new Provider("Provider Name", 59.33, 18.06, capacity);
    }

    @Test
    @DisplayName("Successful booking confirms booking, charges wallet, saves entities, and sends notification")
    void successfulBookingSendsConfirmationNotification() {
        // Arrange
        String seekerId = "seeker-1";
        String listingId = "listing-1";
        String providerId = "provider-1";

        Seeker seeker = createSeeker(seekerId, TrustTier.VERIFIED, 500.00);
        Listing listing = createListing(listingId, providerId);
        Provider provider = createProvider(providerId, 2);

        when(seekers.findById(seekerId)).thenReturn(Optional.of(seeker));
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(providers.findById(providerId)).thenReturn(Optional.of(provider));
        when(bookings.findBySeekerId(seekerId)).thenReturn(List.of());
        when(listings.findByProviderId(providerId)).thenReturn(List.of(listing));
        when(bookings.findByListingId(listing.getId())).thenReturn(List.of());
        when(pricing.priceFor(any(Booking.class), any(Listing.class), any(Seeker.class))).thenReturn(89.60);

        // Act
        Booking confirmedBooking = bookingService.createBooking(seekerId, listingId, 60);

        // Assert
        assertThat(confirmedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(confirmedBooking.getPrice()).isEqualTo(89.60);
        assertThat(seeker.getBalance()).isEqualTo(500.00 - 89.60);

        // Verify seams and persistence
        verify(bookings).save(confirmedBooking);
        verify(seekers).save(seeker);
        verify(listings).save(listing);
        verify(notifications).sendBookingConfirmed(seeker, confirmedBooking);
    }

    @Test
    @DisplayName("Seeker at active booking limit is rejected when attempting another booking (FR-4.4 Rule 2)")
    void bookingRejectedWhenSeekerAtActiveBookingLimit() {
        // Arrange: A NEW seeker has max 1 concurrent active booking.
        String seekerId = "seeker-1";
        String listingId = "listing-2";
        String providerId = "provider-1";

        Seeker seeker = createSeeker(seekerId, TrustTier.NEW, 500.00); // max concurrent = 1
        Listing listing = createListing(listingId, providerId);

        when(seekers.findById(seekerId)).thenReturn(Optional.of(seeker));
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));

        // Seeker already has 1 active booking
        Booking existingActiveBooking = new Booking(seekerId, "listing-1", 60);
        existingActiveBooking.transitionTo(BookingStatus.CONFIRMED);
        when(bookings.findBySeekerId(seekerId)).thenReturn(List.of(existingActiveBooking));

        // Act & Assert: Creating a second booking must fail with BookingRejectedException
        BookingRejectedException ex = assertThrows(BookingRejectedException.class, () ->
                bookingService.createBooking(seekerId, listingId, 60));

        assertThat(ex.getMessage()).contains("Seeker booking limit reached");
    }
}
