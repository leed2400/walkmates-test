package com.walkmates.lab2;

import com.walkmates.model.Seeker;
import com.walkmates.repository.SeekerRepository;
import com.walkmates.service.NotificationService;
import com.walkmates.service.PaymentService;
import com.walkmates.service.SeekerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lab 2, Activity 4.2 — Component isolation for {@link SeekerService}.
 *
 * <p>Tests the payment gateway seam (success, decline, timeout) in isolation
 * using Mockito to verify the wallet balance is never credited on failures.</p>
 */
@ExtendWith(MockitoExtension.class)
class SeekerServiceTest {

    @Mock
    private SeekerRepository seekers;

    @Mock
    private PaymentService payments;

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private SeekerService seekerService;

    @Test
    @DisplayName("Successful topUp charges payment gateway, credits wallet and saves seeker")
    void topUpSuccessCreditsWalletAndSaves() throws Exception {
        // Arrange
        Seeker seeker = new Seeker("alice@example.com", "Alice", "0701234567");
        when(seekers.findById("seeker-1")).thenReturn(Optional.of(seeker));
        when(payments.charge("seeker-1", "pm-1", 100.00)).thenReturn("charge-123");
        when(seekers.save(seeker)).thenReturn(seeker);

        // Act
        Seeker result = seekerService.topUp("seeker-1", "pm-1", 100.00);

        // Assert
        assertThat(result.getBalance()).isEqualTo(100.00);
        verify(payments).charge("seeker-1", "pm-1", 100.00);
        verify(seekers).save(seeker);
    }

    @Test
    @DisplayName("Declined topUp throws PaymentException and does not credit wallet or save")
    void topUpDeclineDoesNotCreditWallet() throws Exception {
        // Arrange
        Seeker seeker = new Seeker("alice@example.com", "Alice", "0701234567");
        when(seekers.findById("seeker-1")).thenReturn(Optional.of(seeker));
        when(payments.charge("seeker-1", "pm-1", 100.00))
                .thenThrow(new PaymentService.PaymentException("Card declined"));

        // Act & Assert
        assertThrows(PaymentService.PaymentException.class, () ->
                seekerService.topUp("seeker-1", "pm-1", 100.00));

        assertThat(seeker.getBalance()).isEqualTo(0.00);
        verify(seekers, never()).save(any());
    }

    @Test
    @DisplayName("Timeout on topUp throws PaymentTimeoutException and does not credit wallet or save")
    void topUpTimeoutDoesNotCreditWallet() throws Exception {
        // Arrange
        Seeker seeker = new Seeker("alice@example.com", "Alice", "0701234567");
        when(seekers.findById("seeker-1")).thenReturn(Optional.of(seeker));
        when(payments.charge("seeker-1", "pm-1", 100.00))
                .thenThrow(new PaymentService.PaymentTimeoutException("Gateway timed out"));

        // Act & Assert
        assertThrows(PaymentService.PaymentTimeoutException.class, () ->
                seekerService.topUp("seeker-1", "pm-1", 100.00));

        assertThat(seeker.getBalance()).isEqualTo(0.00);
        verify(seekers, never()).save(any());
    }
}
