package com.walkmates.lab1;

import com.walkmates.model.Seeker;
import com.walkmates.model.TrustTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lab 1, Part B — specification-based tests for {@link Seeker}.
 *
 * <p>Design your tests on paper first (equivalence partitions, boundary values, decision table)
 * from {@code docs/REQUIREMENTS.md} FR-1.1 / FR-1.3 / FR-1.2, then implement them here. One
 * worked example is provided; the {@code TODO}s are yours.</p>
 */
class SeekerSpecBasedTest {

    // ---- Worked example: boundary value at the maximum single top-up (FR-1.3) ----
    @Test
    @DisplayName("Top-up exactly at the 5000 SEK single-transaction maximum is accepted")
    void topUpAtSingleMaximumIsAccepted() {
        // Arrange
        Seeker seeker = new Seeker("sam@example.com", "Sam", "0707654321");

        // Act
        seeker.addFunds(Seeker.MAX_SINGLE_TOP_UP); // 5000.00, the boundary value

        // Assert
        assertThat(seeker.getBalance()).isEqualTo(Seeker.MAX_SINGLE_TOP_UP);
    }

    // =========================================================================
    // Activity 2.1: Equivalence Partitioning (FR-1.1 & FR-1.3)
    // =========================================================================

    @Nested
    @DisplayName("Activity 2.1: Equivalence Partitioning — Seeker Fields")
    class EquivalencePartitioningTests {

        // --- Valid Fields ---
        @Test
        @DisplayName("Valid registrations with Swedish format starts with 07 and International format starts with +467 are accepted")
        void validSeekerRegistrationIsAccepted() {
            // Arrange & Act
            Seeker seeker1 = new Seeker("sam@example.com", "Sam", "0701234567");
            Seeker seeker2 = new Seeker("sam.o-connor@example.com", "Sam O'Connor-Smith", "+46712345678");

            // Assert
            assertThat(seeker1.getEmail()).isEqualTo("sam@example.com");
            assertThat(seeker1.getDisplayName()).isEqualTo("Sam");
            assertThat(seeker1.getPhoneNumber()).isEqualTo("0701234567");

            assertThat(seeker2.getPhoneNumber()).isEqualTo("+46712345678");
        }

        // --- Email Partition Tests (FR-1.1) ---
        @ParameterizedTest
        @ValueSource(strings = {
                "not-an-email",        // Missing '@'
                "@example.com",        // Empty local part
                "sam@example",         // Missing '.' in domain
                "sam@@example.com"     // Multiple '@' characters
        })
        @DisplayName("Invalid email formats throw IllegalArgumentException")
        void invalidEmailFormatIsRejected(String invalidEmail) {
            // Arrange, Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(invalidEmail, "Sam", "0701234567"));
        }

        @Test
        @DisplayName("Email exceeding 254 characters is rejected")
        void emailExceedingMaxLengthIsRejected() {
            // Arrange
            String tooLongEmail = "a".repeat(246) + "@example.com"; // 255 characters total

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker(tooLongEmail, "Sam", "0701234567"));
        }

        // --- Display Name Partition Tests (FR-1.1) ---
        @Test
        @DisplayName("Display name too short (< 2 characters) is rejected")
        void displayNameTooShortIsRejected() {
            // Arrange, Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker("sam@example.com", "S", "0701234567"));
        }

        @Test
        @DisplayName("Display name too long (> 40 characters) is rejected")
        void displayNameTooLongIsRejected() {
            // Arrange
            String tooLongName = "Name".repeat(11); // 44 characters

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker("sam@example.com", tooLongName, "0701234567"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Sam123",       // Contains numbers
                "Sam_O'Connor", // Contains forbidden underscore
                "Sam!"          // Contains special characters
        })
        @DisplayName("Display name containing forbidden characters is rejected")
        void displayNameWithInvalidCharactersIsRejected(String invalidName) {
            // Arrange, Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker("sam@example.com", invalidName, "0701234567"));
        }

        // --- Phone Number Partition Tests (FR-1.1) ---
        @ParameterizedTest
        @ValueSource(strings = {
                "0812345678",   // Not starting 07 or +467
                "07012345",     // Too short Swedish format (8 digits)
                "+467123456",   // Too short international format
                "07012345678"   // Too long
        })
        @DisplayName("Invalid phone number format or length is rejected")
        void invalidPhoneNumberIsRejected(String invalidPhone) {
            // Arrange, Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> new Seeker("sam@example.com", "Sam", invalidPhone));
        }

        // --- Wallet Top-up Amount Partition Tests (FR-1.3) ---
        @Test
        @DisplayName("Valid wallet top-up (EP-TU-VAL) is accepted")
        void validWalletTopUpIsAccepted() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act
            seeker.addFunds(100.00);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(100.00);
        }

        @Test
        @DisplayName("Wallet top-up below 10.00 SEK (EP-TU-INV-1) is rejected")
        void walletTopUpBelowMinimumIsRejected() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> seeker.addFunds(9.99));
        }

        @Test
        @DisplayName("Wallet top-up exceeding 5000.00 SEK (EP-TU-INV-2) is rejected")
        void walletTopUpExceedingMaximumIsRejected() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> seeker.addFunds(5000.01));
        }
    }

    // =========================================================================
    // Activity 2.2: Boundary Value Analysis (FR-1.3)
    // =========================================================================

    @Nested
    @DisplayName("Activity 2.2: Boundary Value Analysis — Wallet Top-Up")
    class BoundaryValueAnalysisTests {

        // --- Minimum Top-Up Boundaries (10.00 SEK) ---
        @Test
        @DisplayName("Top-up just below the 10 SEK minimum (9.99 SEK) is rejected")
        void topUpJustBelowMinimumIsRejected() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> seeker.addFunds(9.99));
        }

        @Test
        @DisplayName("Top-up exactly at the 10 SEK minimum is accepted")
        void topUpExactlyAtMinimumIsAccepted() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act
            seeker.addFunds(10.00);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(10.00);
        }

        @Test
        @DisplayName("Top-up just above the 10 SEK minimum (10.01 SEK) is accepted")
        void topUpJustAboveMinimumIsAccepted() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act
            seeker.addFunds(10.01);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(10.01);
        }

        // --- Maximum Single Top-Up Boundaries (5000.00 SEK) ---
        @Test
        @DisplayName("Top-up just below the 5000 SEK single-transaction maximum (4999.99 SEK) is accepted")
        void topUpJustBelowSingleMaximumIsAccepted() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act
            seeker.addFunds(4999.99);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(4999.99);
        }

        // Note: exactly at 5000.00 SEK is already covered by the worked example test above.

        @Test
        @DisplayName("Top-up just above the 5000 SEK single-transaction maximum (5000.01 SEK) is rejected")
        void topUpJustAboveSingleMaximumIsRejected() {
            // Arrange
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> seeker.addFunds(5000.01));
        }

        // --- Maximum Wallet Balance Boundaries (20000.00 SEK) ---
        // Helper method to set balance to 18,000.00 SEK (using multiple deposits).
        private Seeker createSeekerWith18000Balance() {
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");
            seeker.addFunds(5000.00);
            seeker.addFunds(5000.00);
            seeker.addFunds(5000.00);
            seeker.addFunds(3000.00);
            assertThat(seeker.getBalance()).isEqualTo(18000.00);
            return seeker;
        }

        @Test
        @DisplayName("Top-up resulting in a balance just below the 20000 SEK maximum (19999.99 SEK) is accepted")
        void topUpResultingJustBelowMaxBalanceIsAccepted() {
            // Arrange
            Seeker seeker = createSeekerWith18000Balance();

            // Act
            seeker.addFunds(1999.99);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(19999.99);
        }

        @Test
        @DisplayName("Top-up resulting in a balance exactly at the 20000 SEK maximum is accepted")
        void topUpResultingExactlyAtMaxBalanceIsAccepted() {
            // Arrange
            Seeker seeker = createSeekerWith18000Balance();

            // Act
            seeker.addFunds(2000.00);

            // Assert
            assertThat(seeker.getBalance()).isEqualTo(20000.00);
        }

        @Test
        @DisplayName("Top-up resulting in a balance just above the 20000 SEK maximum (20000.01 SEK) is rejected")
        void topUpResultingJustAboveMaxBalanceIsRejected() {
            // Arrange
            Seeker seeker = createSeekerWith18000Balance();

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> seeker.addFunds(2000.01));
        }
    }

    // =========================================================================
    // Activity 2.3: Decision Table Testing (FR-1.2)
    // =========================================================================

    @Nested
    @DisplayName("Activity 2.3: Decision Table — Trust Tiers & Limits")
    class DecisionTableTests {

        @ParameterizedTest(name = "Tier {0} should allow {1} active bookings with fee {2}")
        @CsvSource({
                "NEW,         1,  0.15",
                "VERIFIED,    3,  0.12",
                "TRUSTED,     5,  0.08",
                "PRO_SITTER,  10, 0.05"
        })
        @DisplayName("Each trust tier has the correct max concurrent bookings and platform fee")
        void trustTierLimitsAndFeesMatchSpecification(String tierName, int expectedBookings, double expectedFee) {
            // Arrange
            TrustTier tier = TrustTier.valueOf(tierName);
            Seeker seeker = new Seeker("sam@example.com", "Sam", "0701234567");

            // Act
            seeker.setTrustTier(tier);

            // Assert
            assertThat(tier.getMaxConcurrentBookings()).isEqualTo(expectedBookings);
            assertThat(tier.getPlatformFee()).isEqualTo(expectedFee);

            assertThat(seeker.getTrustTier()).isEqualTo(tier);
            assertThat(seeker.getMaxConcurrentBookings()).isEqualTo(expectedBookings);
        }
    }
}
