# Lab Reflection — WalkMates
**Lab:** 3
**Pair:** Lena Edlind, Jakob Cederblad
**Repo commit/tag:** [Insert commit hash / link here]

---

### 1. What we did
In Part A, we tested the AI matching feature, but without actually connecting to a live LLM. First, we checked the prompt builder (`MatchExplanationService`) to make sure it includes all the required fields (like base rate and title) and safely wraps the provider's free text inside data delimiters.

Next, we used Mockito to test the fallback behavior. We made the LLM mock throw a timeout exception and return null/blank strings to verify that the code returns the hardcoded fallback text instead of crashing. We also wrote a test to check that prompt injections (like *"Ignore previous instructions..."*) stay trapped inside the delimiter tags. For the metamorphic testing part, we wrote tests to prove that adding random sentences to a description (MR-1) or shuffling the list of candidates (MR-2) doesn't change which listing the system picks as the best match. Finally, we wrote Spring `MockMvc` tests to verify that the controller returns 200 OK and the right JSON structure when both the normal and the fallback explanations are triggered.

### 2. What we found
The most interesting thing was realizing how to test AI code when you don't have a strict "right answer" (an oracle). You can't really assert exactly what text an LLM will generate. But we found that we *can* test everything around it deterministically: how the prompt is built, how fallbacks are handled, and how the ranking algorithm behaves when we tweak the input in ways that shouldn't matter (the metamorphic relations). 

We also saw why the tie-breaker in `recommendBestMatch` is so important. Because it falls back on `Listing::getId` when the base rates are equal, we could shuffle the list as much as we wanted in MR-2 and it still picked the exact same listing every time.

### 3. AI use (be honest — it doesn't lower your grade)
We used the AI as a pair-programming buddy to discuss how to set up the tests and structure the code.

- **Concrete example 1:** When we started Activity 5.1, the AI wanted to rewrite the worked examples and add a bunch of extra validation tests. We had to step in and tell it to stop, keep the template as it was, and focus only on the four specific `// TODO` comments the teacher provided.
- **Concrete example 2:** We asked the AI if we should write a test for negative hourly rates. The AI pointed out that `baseRatePerHour` is tied to an enum (`ListingType`) with fixed values, so a user can't actually pass in a negative number. Because of that, we skipped the negative test and focused on testing the `SHELTER_VOLUNTEER` type which has a rate of exactly `0.0`.
- **Concrete example 3:** For the order invariance test (MR-2), the AI suggested we just reverse the list. We decided to take it a step further and told it to also test rotating the list (`Collections.rotate`), to be really sure the order didn't matter.

### 4. Judgment
We had to use our own judgment in a few places where the AI couldn't just give us the answer:
- **What counts as "irrelevant":** For MR-1, we had to come up with realistic sentences that shouldn't affect the match (like pets liking tennis balls or sleeping in the sun) to make sure the test actually made sense in the context of the app.
- **Security vs. Unit tests:** We had to realize that checking for delimiters in a unit test proves that the code formats the string correctly, but it doesn't actually guarantee that a live LLM won't get tricked by the prompt injection. It's just a structural check.
- **Keeping HTTP tests isolated:** We had to decide to mock the entire AI service in `MatchControllerWebTest` so that we were strictly testing the HTTP layer (like 200 OK and 404 Not Found), rather than testing the matching logic twice.

### 5. What we'd test next
If we had another hour, we would definitely write a test to see what happens if a user tries to break out of the delimiters by putting the literal string `"LISTING_DESCRIPTION_DATA>>>"` in their listing description. We'd also like to try running the metamorphic tests with a property-based testing tool that throws hundreds of random, shuffled lists at the algorithm just to be absolutely sure it never breaks.
