# CORS x-user-name Header Fix - Bugfix Design

## Overview

The API Gateway's `GatewayCorsWebFilter` runs at `Ordered.HIGHEST_PRECEDENCE` and unconditionally writes a hardcoded `Access-Control-Allow-Headers` value (`Origin,Content-Type,Accept,Authorization,X-Requested-With`) to every response. This header is written before Spring Cloud Gateway's own CORS machinery (driven by `CorsConfig.java` and `application.yml`) can apply its wildcard `*` configuration, and because the `DedupeResponseHeader` filter is set to `RETAIN_FIRST`, the hardcoded value wins. The result is that any preflight request carrying the custom `x-user-name` header is rejected by the browser.

The fix removes `GatewayCorsWebFilter` entirely, delegating all CORS handling to the already-correct `CorsConfig` bean and the `globalcors` block in `application.yml`.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug — a preflight OPTIONS request arrives at the gateway carrying the `x-user-name` header, and `GatewayCorsWebFilter` writes a hardcoded `Access-Control-Allow-Headers` that omits `x-user-name`.
- **Property (P)**: The desired behavior when the bug condition holds — the preflight response's `Access-Control-Allow-Headers` value must permit `x-user-name` (either by listing it explicitly or by returning `*`).
- **Preservation**: All existing CORS behaviors (allowed origin, credentials, methods, standard headers) that must remain unchanged after the fix.
- **GatewayCorsWebFilter**: The `@Component` in `GatewayCorsWebFilter.java` that runs at `Ordered.HIGHEST_PRECEDENCE` and manually sets CORS response headers, overriding the framework-managed configuration.
- **CorsConfig**: The `@Configuration` class in `CorsConfig.java` that registers a Spring `CorsWebFilter` bean with `allowedHeaders("*")`.
- **globalcors**: The `spring.cloud.gateway.globalcors` block in `application.yml` that configures `allowedHeaders: ['*']` for all routes.
- **DedupeResponseHeader**: A Spring Cloud Gateway filter set to `RETAIN_FIRST`, which keeps the first occurrence of a duplicate response header — meaning whichever filter writes the header first wins.
- **x-user-name**: A custom HTTP request header sent by the Vue.js frontend to pass the authenticated username to downstream services.

## Bug Details

### Bug Condition

The bug manifests when the Vue.js frontend (running at `http://localhost:5173`) sends a CORS preflight OPTIONS request to any API Gateway route and includes `x-user-name` in the `Access-Control-Request-Headers` field. `GatewayCorsWebFilter`, executing at `Ordered.HIGHEST_PRECEDENCE`, writes `Access-Control-Allow-Headers: Origin,Content-Type,Accept,Authorization,X-Requested-With` before any other CORS handler runs. Because `DedupeResponseHeader` retains the first value, the wildcard `*` from `CorsConfig` and `application.yml` is discarded.

**Formal Specification:**
```
FUNCTION isBugCondition(request)
  INPUT: request of type ServerHttpRequest
  OUTPUT: boolean

  RETURN request.method == OPTIONS
         AND request.headers["Access-Control-Request-Headers"] CONTAINS "x-user-name"
         AND GatewayCorsWebFilter IS registered AND runs at HIGHEST_PRECEDENCE
         AND GatewayCorsWebFilter writes hardcoded Access-Control-Allow-Headers
             that does NOT include "x-user-name"
END FUNCTION
```

### Examples

- **Bug present**: Frontend sends `OPTIONS /products` with `Access-Control-Request-Headers: x-user-name, Authorization`. Gateway responds with `Access-Control-Allow-Headers: Origin,Content-Type,Accept,Authorization,X-Requested-With`. Browser blocks the actual `GET /products` request with a CORS policy error.
- **Bug present**: Frontend sends `OPTIONS /auth/login` with `Access-Control-Request-Headers: x-user-name, Content-Type`. Gateway responds with the same hardcoded header list. Browser blocks the actual `POST /auth/login`.
- **Bug present**: Frontend sends `OPTIONS /cart` with `Access-Control-Request-Headers: x-user-name`. Gateway responds with the hardcoded list. Browser blocks the actual `POST /cart`.
- **Edge case (no bug)**: Frontend sends `OPTIONS /products` with `Access-Control-Request-Headers: Authorization, Content-Type` (no `x-user-name`). The hardcoded list happens to cover these headers, so the browser proceeds — but the underlying override problem still exists.

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Requests from `http://localhost:5173` and `http://127.0.0.1:5173` must continue to receive `Access-Control-Allow-Origin: http://localhost:5173` (or the matching origin).
- Responses must continue to include `Access-Control-Allow-Credentials: true` for credentialed requests.
- Preflight requests for `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS` methods must continue to receive `200 OK` with the appropriate `Access-Control-Allow-Methods` header.
- Standard headers (`Content-Type`, `Authorization`, `Accept`, `Origin`, `X-Requested-With`) must continue to be allowed through the gateway.
- Requests from origins other than the two allowed origins must continue to be rejected with a CORS error.

**Scope:**
All requests that do NOT involve the `x-user-name` header in a preflight context should be completely unaffected by this fix. This includes:
- All non-preflight (simple and actual) requests.
- Preflight requests that only include standard headers already covered by the existing configuration.
- Any server-to-server requests that bypass the browser CORS mechanism.

**Note:** The expected correct behavior for the bug condition itself is defined in the Correctness Properties section (Property 1). This section focuses on what must NOT change.

## Hypothesized Root Cause

Based on the bug description and code review, the root cause is confirmed:

1. **Hardcoded `Access-Control-Allow-Headers` in `GatewayCorsWebFilter`**: The filter explicitly sets `Access-Control-Allow-Headers` to a fixed list that was written before `x-user-name` was introduced as a custom header. The list was never updated to include it.

2. **`HIGHEST_PRECEDENCE` ordering wins the deduplication race**: Because `GatewayCorsWebFilter` runs at `Ordered.HIGHEST_PRECEDENCE` and `DedupeResponseHeader` is configured to `RETAIN_FIRST`, the hardcoded value is always the one that survives — even though `CorsConfig` and `application.yml` both correctly specify `allowedHeaders: *`.

3. **Redundant CORS configuration layers**: The project has three overlapping CORS configurations (`GatewayCorsWebFilter`, `CorsConfig`, `application.yml`). The manual filter was likely added as a quick fix at some point and was never removed when the declarative configuration was put in place. The declarative configuration is correct; the manual filter is the sole source of the defect.

4. **No test coverage for custom headers in preflight**: There are no existing tests that assert the preflight response includes custom headers, so the regression went undetected.

## Correctness Properties

Property 1: Bug Condition - Preflight Response Permits x-user-name Header

_For any_ OPTIONS request where the bug condition holds (the request carries `x-user-name` in `Access-Control-Request-Headers`), the fixed gateway SHALL respond with an `Access-Control-Allow-Headers` value that includes `x-user-name` (or is the wildcard `*`), allowing the browser to proceed with the actual request.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Standard CORS Behaviors Remain Unchanged

_For any_ request where the bug condition does NOT hold (no `x-user-name` in a preflight, or a non-preflight request), the fixed gateway SHALL produce the same CORS response headers as the original gateway, preserving the allowed origin, credentials flag, allowed methods, and standard allowed headers.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

Assuming the root cause analysis is correct, the fix is a single-file deletion:

**File**: `ecommerce-microservices/api-gateway/src/main/java/com/ecommerce/gateway/GatewayCorsWebFilter.java`

**Action**: Delete the file entirely.

**Rationale**: `CorsConfig.java` already registers a `CorsWebFilter` bean with `allowedHeaders("*")`, and `application.yml` already configures `globalcors` with `allowedHeaders: ['*']`. Both configurations are correct and complete. `GatewayCorsWebFilter` provides no value beyond what these configurations already supply, and its hardcoded header list is the sole cause of the defect.

**Specific Changes**:

1. **Delete `GatewayCorsWebFilter.java`**: Remove the file. This eliminates the `@Component` that runs at `HIGHEST_PRECEDENCE` and writes the hardcoded `Access-Control-Allow-Headers`.

2. **Verify `CorsConfig.java` is sufficient**: Confirm that the `CorsWebFilter` bean in `CorsConfig.java` with `addAllowedHeader("*")` handles all required CORS scenarios. No changes needed to this file.

3. **Verify `application.yml` is sufficient**: Confirm that `globalcors.corsConfigurations['/**'].allowedHeaders: ['*']` covers all routes. No changes needed to this file.

4. **Verify `DedupeResponseHeader` filter**: With `GatewayCorsWebFilter` removed, there will be no duplicate headers to deduplicate. The `DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST` filter can remain in place as a harmless safeguard, or be removed if desired. No change is strictly required.

5. **No changes to downstream services**: The fix is entirely within the API Gateway module. Auth service, cart service, and other downstream services are unaffected.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on the unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm the root cause analysis. If the tests do not fail as expected on unfixed code, the root cause hypothesis must be revised.

**Test Plan**: Write a `WebTestClient`-based test (or a `MockServerWebExchange`-based unit test) that sends an OPTIONS request with `Access-Control-Request-Headers: x-user-name` to the gateway filter chain and asserts that the response `Access-Control-Allow-Headers` includes `x-user-name`. Run these tests against the UNFIXED code to observe failures and confirm the root cause.

**Test Cases**:
1. **Preflight with x-user-name only**: Send `OPTIONS` with `Access-Control-Request-Headers: x-user-name` — will fail on unfixed code because the hardcoded list does not include `x-user-name`.
2. **Preflight with x-user-name and Authorization**: Send `OPTIONS` with `Access-Control-Request-Headers: x-user-name, Authorization` — will fail on unfixed code for the same reason.
3. **Preflight to /products route**: Send `OPTIONS /products` with `Access-Control-Request-Headers: x-user-name` — will fail on unfixed code.
4. **Preflight to /cart route**: Send `OPTIONS /cart` with `Access-Control-Request-Headers: x-user-name` — will fail on unfixed code.

**Expected Counterexamples**:
- `Access-Control-Allow-Headers` in the response does not contain `x-user-name`.
- The response header value is exactly `Origin,Content-Type,Accept,Authorization,X-Requested-With`, confirming the hardcoded list from `GatewayCorsWebFilter` is the source.

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed gateway produces the expected behavior.

**Pseudocode:**
```
FOR ALL request WHERE isBugCondition(request) DO
  response := fixedGateway.handle(request)
  ASSERT response.headers["Access-Control-Allow-Headers"] CONTAINS "x-user-name"
         OR response.headers["Access-Control-Allow-Headers"] == "*"
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed gateway produces the same CORS response headers as the original gateway.

**Pseudocode:**
```
FOR ALL request WHERE NOT isBugCondition(request) DO
  originalResponse := originalGateway.handle(request)
  fixedResponse    := fixedGateway.handle(request)
  ASSERT originalResponse.headers["Access-Control-Allow-Origin"]      == fixedResponse.headers["Access-Control-Allow-Origin"]
  ASSERT originalResponse.headers["Access-Control-Allow-Credentials"] == fixedResponse.headers["Access-Control-Allow-Credentials"]
  ASSERT originalResponse.headers["Access-Control-Allow-Methods"]     == fixedResponse.headers["Access-Control-Allow-Methods"]
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (random origins, methods, header combinations).
- It catches edge cases that manual unit tests might miss.
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs.

**Test Plan**: Observe the behavior of the unfixed code for non-`x-user-name` preflight requests and non-preflight requests, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Standard headers preservation**: Verify that preflight requests with only `Content-Type`, `Authorization`, `Accept` continue to receive a permitting response after the fix.
2. **Allowed origin preservation**: Verify that `Access-Control-Allow-Origin: http://localhost:5173` continues to appear in responses from the allowed origin.
3. **Credentials preservation**: Verify that `Access-Control-Allow-Credentials: true` continues to appear in responses.
4. **Methods preservation**: Verify that `Access-Control-Allow-Methods` continues to list `GET,POST,PUT,PATCH,DELETE,OPTIONS` for preflight requests.
5. **Disallowed origin rejection**: Verify that requests from origins other than the two allowed origins continue to be rejected.

### Unit Tests

- Test that `GatewayCorsWebFilter` is no longer registered as a bean after the fix (negative test).
- Test that `CorsConfig.corsWebFilter()` bean is present and configured with `allowedHeaders("*")`.
- Test that a `MockServerWebExchange` OPTIONS request with `x-user-name` processed through `CorsConfig.corsWebFilter()` returns a response that permits `x-user-name`.
- Test edge cases: preflight with no `Access-Control-Request-Headers`, preflight with an unknown custom header.

### Property-Based Tests

- Generate random sets of request headers (including and excluding `x-user-name`) and verify that the fixed gateway always returns a preflight response that permits all requested headers when the origin is allowed.
- Generate random allowed-origin requests and verify that `Access-Control-Allow-Origin` is always set correctly in the fixed gateway.
- Generate random non-preflight requests and verify that the fixed gateway's CORS response headers match the expected values across many input combinations.

### Integration Tests

- Full flow: frontend sends preflight with `x-user-name`, then sends the actual request — verify both succeed end-to-end.
- Context switching: verify that switching between `/auth/**`, `/products/**`, and `/cart/**` routes all correctly handle `x-user-name` in preflight requests.
- Verify that the `DedupeResponseHeader` filter does not cause issues when only one CORS filter is writing headers.
