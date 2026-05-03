# Implementation Plan

- [-] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Preflight with x-user-name Header Blocked
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate that `GatewayCorsWebFilter` writes a hardcoded `Access-Control-Allow-Headers` that omits `x-user-name`
  - **Scoped PBT Approach**: Scope the property to the concrete failing cases — OPTIONS requests carrying `x-user-name` in `Access-Control-Request-Headers` processed through `GatewayCorsWebFilter`
  - Create `ecommerce-microservices/api-gateway/src/test/java/com/ecommerce/gateway/CorsXUserNameBugConditionTest.java`
  - Use `MockServerWebExchange` to build an OPTIONS request with `Origin: http://localhost:5173` and `Access-Control-Request-Headers: x-user-name`
  - Pass the exchange through `GatewayCorsWebFilter` directly (instantiate it, call `filter()`)
  - Assert that `Access-Control-Allow-Headers` in the response contains `x-user-name` or equals `*`
  - Run test on UNFIXED code (before deleting `GatewayCorsWebFilter.java`)
  - **EXPECTED OUTCOME**: Test FAILS — response header is `Origin,Content-Type,Accept,Authorization,X-Requested-With`, confirming the bug
  - Document counterexamples found (e.g., `Access-Control-Allow-Headers` = `Origin,Content-Type,Accept,Authorization,X-Requested-With` instead of including `x-user-name`)
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.3_

- [ ] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Standard CORS Behaviors Remain Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-`x-user-name` preflight requests and non-preflight requests
  - Create `ecommerce-microservices/api-gateway/src/test/java/com/ecommerce/gateway/CorsPreservationTest.java`
  - Observe: OPTIONS request with `Access-Control-Request-Headers: Authorization, Content-Type` from `http://localhost:5173` → `Access-Control-Allow-Origin: http://localhost:5173`, `Access-Control-Allow-Credentials: true`, `Access-Control-Allow-Methods` includes GET/POST/PUT/PATCH/DELETE/OPTIONS
  - Observe: Non-preflight GET request from `http://localhost:5173` → CORS headers present
  - Observe: Request from disallowed origin → no `Access-Control-Allow-Origin` header
  - Write property-based tests using `@ParameterizedTest` or jqwik/junit-quickcheck covering:
    - For all standard-header-only preflight requests from allowed origins: `Access-Control-Allow-Origin` matches the request origin, `Access-Control-Allow-Credentials: true`, `Access-Control-Allow-Methods` covers all configured methods
    - For all requests from disallowed origins: no `Access-Control-Allow-Origin` header is set
  - Verify tests PASS on UNFIXED code (confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3. Fix: Delete GatewayCorsWebFilter and verify CORS is handled by CorsConfig

  - [ ] 3.1 Delete GatewayCorsWebFilter.java
    - Delete `ecommerce-microservices/api-gateway/src/main/java/com/ecommerce/gateway/GatewayCorsWebFilter.java`
    - This removes the `@Component` running at `Ordered.HIGHEST_PRECEDENCE` that writes the hardcoded `Access-Control-Allow-Headers` list
    - `CorsConfig.java` already has `addAllowedHeader("*")` — no changes needed
    - `application.yml` already has `allowedHeaders: ['*']` — no changes needed
    - `DedupeResponseHeader` filter can remain as a harmless safeguard
    - _Bug_Condition: isBugCondition(request) where request.method == OPTIONS AND request.headers["Access-Control-Request-Headers"] CONTAINS "x-user-name" AND GatewayCorsWebFilter IS registered_
    - _Expected_Behavior: response.headers["Access-Control-Allow-Headers"] CONTAINS "x-user-name" OR equals "*"_
    - _Preservation: All non-x-user-name preflight and non-preflight CORS behaviors remain identical_
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Preflight with x-user-name Header Permitted
    - **IMPORTANT**: Re-run the SAME test from task 1 — do NOT write a new test
    - The test from task 1 encodes the expected behavior (response permits `x-user-name`)
    - When this test passes, it confirms `CorsConfig`'s wildcard `*` is now the active CORS handler
    - Run `CorsXUserNameBugConditionTest` from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Standard CORS Behaviors Remain Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 — do NOT write new tests
    - Run `CorsPreservationTest` from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions in allowed origin, credentials, methods, standard headers, and disallowed origin rejection)
    - Confirm all tests still pass after fix (no regressions)

- [ ] 4. Checkpoint - Ensure all tests pass
  - Run the full api-gateway test suite: `mvn test -pl api-gateway` from the `ecommerce-microservices` directory
  - Ensure all tests pass; ask the user if questions arise
