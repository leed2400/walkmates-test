# Lab 2 Analysis — WalkMates

**Course:** Principles & Practices in Software Testing (DV033G)  
**Pair:** Lena Edlind, Jakob Cederblad & Gemini CLI (AI Assistant)  

---

## Part A — Structural Testing & Coverage (M3)

### Activity 3.1 — Measure Baseline Coverage

**Target:** `com.walkmates.service.PricingCalculator` (FR-4.3)  
**Initial test suite:** `PricingCalculatorStructuralTest` (only the `shortWalkPrice` test case)

#### JaCoCo Baseline Measurements:
- **Instruction Coverage:** 83% (56 of 67 instructions covered, 11 missed)
- **Branch Coverage:** 50% (5 of 10 branches covered, 5 missed)
- **Line Coverage:** 80% (12 of 15 lines covered, 3 missed: line 35, 41, 50)
- **Complexity (Cxty):** 37% (3 of 8 covered, 5 missed)

#### Identified Uncovered Branches and Lines:
1. **Null-guard branch (lines 34–35):**  
   `if (booking == null || listing == null || seeker == null)`  
   - Because the condition uses `||` (OR), each check creates a separate branch:
     - `booking == null`
     - `listing == null`
     - `seeker == null`
   - Since all three parameters were non-null in the initial test, none of these 3 true-branches were executed (3 missed branches).
2. **Free-listing path (lines 40–41):**  
   `if (baseRate == 0.0)`  
   - Because `DOG_WALK` was used, the base rate was 80.0 SEK/h ($> 0.0$). The branch where `baseRate == 0.0` (e.g., for `SHELTER_VOLUNTEER`) was never executed and the return statement `0.00` (line 41) was not reached. 1 branch missed.
3. **Overnight-surcharge path (lines 49–50):**  
   `if (booking.getDurationMinutes() >= OVERNIGHT_THRESHOLD_MINUTES)`  
   - The initial test used a booking duration of 60 minutes, which is below the threshold of 480 minutes (8 hours). Therefore, the branch applying the 20% surcharge (line 50) was never executed. 1 branch missed.

---

### Activity 3.2 — Raise Branch Coverage

Test cases implemented to achieve 100% branch coverage:
1. `shouldRejectNullBooking()` — Asserts that `booking == null` throws an `IllegalArgumentException`.
2. `shouldRejectNullListing()` — Asserts that `listing == null` throws an `IllegalArgumentException`.
3. `shouldRejectNullSeeker()` — Asserts that `seeker == null` throws an `IllegalArgumentException`.
4. `shelterVolunteerIsAlwaysFree()` — Asserts that a `SHELTER_VOLUNTEER` listing (baseRate == 0.0) costs 0.00 SEK regardless of duration.
5. `overnightBookingIncludesSurcharge()` — Asserts that a booking clearly exceeding the threshold (e.g., 600 minutes) includes the 20% overnight surcharge.

#### JaCoCo Measurements After Activity 3.2:
- **Instruction Coverage:** **100%** (67 of 67 instructions covered, 0 missed) — *increased from 83%*
- **Branch Coverage:** **100%** (10 of 10 branches covered, 0 missed) — *increased from 50%*
- **Line Coverage:** **100%** (15 of 15 lines covered, 0 missed) — *increased from 80%*
- **Complexity (Cxty):** **100%** (8 of 8 covered, 0 missed) — *increased from 37%*

All previously uncovered lines and branches (null guards, free listing path, and overnight surcharge branch) are now fully covered.

---

### Activity 3.3 — Coverage ≠ Correctness

*(To be filled in)*

---

## Part B — Test Optimization (M4)

### Activity 4.1 — Mutation Testing (PIT)
*(To be filled in during Part B)*

### Activity 4.2 — Component Isolation with Mocking
*(To be filled in during Part B)*

### Activity 4.3 — Regression Selection & Prioritization
*(To be filled in during Part B)*
