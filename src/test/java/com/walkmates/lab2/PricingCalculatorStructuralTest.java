package com.walkmates.lab2;

import com.walkmates.model.Booking;
import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import com.walkmates.service.PricingCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lab 2, Part A — structural testing for {@link PricingCalculator} (FR-4.3).
 *
 * <p>Run coverage with {@code mvn clean test jacoco:report} and open
 * {@code target/site/jacoco/index.html}. Find the uncovered branches and add tests to reach
 * them — then look hard at the <em>overnight surcharge boundary</em>: there is a path that your
 * happy-path test "covers" but does not actually check (coverage ≠ correctness).</p>
 */
class PricingCalculatorStructuralTest {

    private final PricingCalculator pricing = new PricingCalculator();

    private Seeker seeker(TrustTier tier) {
        Seeker s = new Seeker("p@example.com", "Pat", "0701112233");
        s.setTrustTier(tier);
        return s;
    }

    private Listing listing(ListingType type) {
        return new Listing("provider-1", "A listing", "desc", type);
    }

    // ---- Worked example: a short standard walk, no overnight surcharge ----
    @Test
    @DisplayName("60 min DOG_WALK for a VERIFIED seeker = 80 base + 12% fee = 89.60")
    void shortWalkPrice() {
        Booking booking = new Booking("seeker-1", "listing-1", 60);

        double price = pricing.priceFor(booking, listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED));

        assertThat(price).isEqualTo(89.60);
    }

    @Test
    @DisplayName("Null booking throws IllegalArgumentException")
    void shouldRejectNullBooking() {
        Booking booking = null;

        assertThrows(
                IllegalArgumentException.class, () -> pricing.priceFor(
                        booking, listing(ListingType.DOG_WALK), seeker(TrustTier.VERIFIED))
        );
    }

    @Test
    @DisplayName("Null listing throws IllegalArgumentException")
    void shouldRejectNullListing() {
        Booking booking = new Booking("seeker-1", "listing-1", 60);

        assertThrows(
                IllegalArgumentException.class, () -> pricing.priceFor(
                        booking, null, seeker(TrustTier.VERIFIED))
        );
    }

    @Test
    @DisplayName("Null seeker throws IllegalArgumentException")
    void shouldRejectNullSeeker() {
        Booking booking = new Booking("seeker-1", "listing-1", 60);

        assertThrows(
                IllegalArgumentException.class, () -> pricing.priceFor(
                        booking, listing(ListingType.DOG_WALK), null)
        );
    }
}
