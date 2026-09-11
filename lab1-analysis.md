# Lab 1 Analysis — WalkMates

**Course:** Principles & Practices in Software Testing (DV033G)  
**Pair:** Lena Edlind, Jakob Cederblad & Gemini CLI (AI Assistant)  

---

## Part A — Fundamentals (M1)
### Activity 1.1 — Quality-Attribute Analysis (ISO/IEC 25010)
#### FR-1.1 Registration
- Functional Suitability: System should only allow valid input and reject invalid
- Interaction capability: Seeker need to understand why a registration was rejected

#### FR-3.1 Types and base rate
- Functional Suitability: System should only allow one type
- Flexibility: Can a new listing type be added without scattered changes

#### FR-5.1 Match explanation
- Functional Suitability: The AI Feature should give the seeker relevant matches
- Maintainability: Can LLM clients be changed and tested efficiently

#### Testable Quality Requirement
FR1.1 Registration: For Functional Suitability, I would test whether the system accepts display names containing
2-40 characters when registering a Seeker.

### Activity 1.2
#### Trace the chain: what was the likely human error, the fault in the code, and the failure the user saw?
Human Error: The developer used the wrong operator or misunderstood the requirements.
Fault: Code does not reject a second booking when a new Seeker already has one active booking.
Failure: The new Seeker has two active bookings even if the tier only allow for one booking.

#### Find the responsible code (hint: the FR-4.4 rule-2 check). Describe the fault precisely.
Responsible code: ```if (seekerActive > seeker.getMaxConcurrentBookings())```. This is a boundary issue since the FR-4.4
rule-2 states that the ``The Seeker's active Bookings < the trust-tier max``. The trust-tier maximum for active bookings
for a New Seeker is 1. However, the code checks whether the seeker's current active bookings are greater than
getMaxConcurrentBookings before rejecting. A New Seeker with 1 active booking is therefore still allowed to create a
second booking. The system only rejects the booking when the number of current active bookings is  greater than 1.

#### Which test level (unit / integration / system) should have caught this, and what kind of test (which technique)?
A Unit Test should've caught this defect.
This would've been caught with a simple Arrange-Act-Assert test structure using boundary value analysis.

---

## Part B — Specification-Based Design (M2)

### Activity 2.1 — Equivalence Partitioning (Seeker Unit)

Derived from `FR-1.1` and `FR-1.3` for the `Seeker` class.

| Input Field | Partition ID | Description | Representative Input | Expected Outcome |
|---|---|---|---|---|
| **Email** | EP-EM-VAL | Valid format, length $\le 254$ | `test@example.com` | Accepted |
| | EP-EM-INV-1 | Invalid: missing `@` | `testexample.com` | Rejected (`IllegalArgumentException`) |
| | EP-EM-INV-2 | Invalid: empty local part | `@example.com` | Rejected (`IllegalArgumentException`) |
| | EP-EM-INV-3 | Invalid: missing `.` in domain | `test@example` | Rejected (`IllegalArgumentException`) |
| | EP-EM-INV-4 | Invalid: length > 254 chars | `"a".repeat(246) + "@example.com"` | Rejected (`IllegalArgumentException`) |
| | EP-EM-INV-5 | Invalid: multiple `@` characters | `sam@@example.com` | Rejected (`IllegalArgumentException`) |
| **Display Name**| EP-DN-VAL | Valid: 2-40 chars, letters/spaces/hyphens/apostrophes | `O'Connor-Smith Jr` | Accepted |
| | EP-DN-INV-1 | Invalid: too short (< 2 chars) | `A` | Rejected (`IllegalArgumentException`) |
| | EP-DN-INV-2 | Invalid: too long (> 40 chars) | `"Name".repeat(11)` (44 chars) | Rejected (`IllegalArgumentException`) |
| | EP-DN-INV-3 | Invalid: forbidden characters | `Sam123!` | Rejected (`IllegalArgumentException`) |
| **Phone Number**| EP-PH-VAL-1 | Valid Swedish format (07 + 8 digits) | `0701234567` | Accepted |
| | EP-PH-VAL-2 | Valid Swedish international format (+467 + 8 digits) | `+46712345678` | Accepted |
| | EP-PH-INV-1 | Invalid format (does not start 07/+467) | `0812345678` | Rejected (`IllegalArgumentException`) |
| | EP-PH-INV-2 | Invalid length (too short) | `07012345` | Rejected (`IllegalArgumentException`) |
| | EP-PH-INV-3 | Invalid length (too long) | `07012345678` | Rejected (`IllegalArgumentException`) |
| **Wallet Top-Up**| EP-TU-VAL | Valid single top-up amount [10.00, 5000.00] | `100.00` | Accepted |
| | EP-TU-INV-1 | Invalid: top-up amount below 10.00 SEK | `9.99` | Rejected (`IllegalArgumentException`) |
| | EP-TU-INV-2 | Invalid: top-up amount above 5,000.00 SEK | `5000.01` | Rejected (`IllegalArgumentException`) |

---

### Activity 2.2 — Boundary Value Analysis (Wallet Top-Up)

Derived from `FR-1.3` for `Seeker.addFunds(double amount)` starting with a balance of `0.00` (unless otherwise noted).

| Boundary Base | Test Case ID | Test Value | Expected Outcome | Justification / Note |
|---|---|---|---|---|
| **Min Top-up (10.00)** | BVA-MIN-BELOW | `9.99` | Rejected (`IllegalArgumentException`) | Just below the minimum limit |
| | BVA-MIN-AT | `10.00` | Accepted (Balance: `10.00`) | Exactly at the minimum limit |
| | BVA-MIN-ABOVE | `10.01` | Accepted (Balance: `10.01`) | Just above the minimum limit |
| **Max Single (5000.00)**| BVA-MAX-BELOW | `4999.99` | Accepted (Balance: `4999.99`) | Just below the single transaction maximum |
| | BVA-MAX-AT | `5000.00` | Accepted (Balance: `5000.00`) | Exactly at the single maximum (worked example) |
| | BVA-MAX-ABOVE | `5000.01` | Rejected (`IllegalArgumentException`) | Just above the single maximum limit |
| **Max Balance (20000.00)**| BVA-BAL-BELOW | Top up `1999.99` | Accepted (Balance: `19999.99`) | Balance just below 20000.00 (start balance: 18000.00) |
| | BVA-BAL-AT | Top up `2000.00` | Accepted (Balance: `20000.00`) | Balance exactly at 20000.00 (start balance: 18000.00) |
| | BVA-BAL-ABOVE | Top up `2000.01` | Rejected (`IllegalArgumentException`) | Balance would exceed 20000.00 (start balance: 18000.00) |

---

### Activity 2.3 — Decision Table (Trust Tier $\rightarrow$ Limits)

Derived from `FR-1.2` for testing the correlation between a `Seeker`'s trust tier and their maximum active bookings and platform fee.

| Rule / Trust Tier | Max Concurrent Bookings | Platform Fee | Test Representative |
|---|---|---|---|
| **Rule 1: `NEW`** | 1 | 15% (`0.15`) | `TrustTier.NEW` |
| **Rule 2: `VERIFIED`** | 3 | 12% (`0.12`) | `TrustTier.VERIFIED` |
| **Rule 3: `TRUSTED`** | 5 | 8% (`0.08`) | `TrustTier.TRUSTED` |
| **Rule 4: `PRO_SITTER`** | 10 | 5% (`0.05`) | `TrustTier.PRO_SITTER` |
