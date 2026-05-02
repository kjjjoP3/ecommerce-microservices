package com.ecommerce.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 1: Bug Condition — Preflight with x-user-name Header Blocked
 *
 * <p>This test class surfaces counterexamples that demonstrate the bug in
 * {@link GatewayCorsWebFilter}: the filter writes a hardcoded
 * {@code Access-Control-Allow-Headers} value that omits {@code x-user-name},
 * causing browsers to reject preflight requests that include that header.
 *
 * <p><strong>IMPORTANT</strong>: These tests are written BEFORE the fix and are
 * expected to FAIL on unfixed code. Failure confirms the bug exists. After the
 * fix (deletion of {@code GatewayCorsWebFilter.java}), the tests are re-run
 * against {@link CorsConfig} and are expected to PASS.
 *
 * <p>Bug Condition (isBugCondition):
 * <pre>
 *   request.method == OPTIONS
 *   AND request.headers["Access-Control-Request-Headers"] CONTAINS "x-user-name"
 *   AND GatewayCorsWebFilter IS registered AND runs at HIGHEST_PRECEDENCE
 *   AND GatewayCorsWebFilter writes hardcoded Access-Control-Allow-Headers
 *       that does NOT include "x-user-name"
 * </pre>
 *
 * <p>Expected Behavior (Property P):
 * <pre>
 *   response.headers["Access-Control-Allow-Headers"] CONTAINS "x-user-name"
 *   OR response.headers["Access-Control-Allow-Headers"] == "*"
 * </pre>
 *
 * Requirements: 1.1, 1.3
 */
@DisplayName("Property 1: Bug Condition — Preflight with x-user-name Header Blocked")
class CorsXUserNameBugConditionTest {

    /**
     * Simulates a no-op filter chain (the request is fully handled by the CORS filter).
     */
    private static final WebFilterChain PASSTHROUGH_CHAIN = exchange -> Mono.empty();

    // -------------------------------------------------------------------------
    // Helper: build a preflight OPTIONS exchange
    // -------------------------------------------------------------------------

    private MockServerWebExchange preflightExchange(String requestedHeaders) {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8080/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", requestedHeaders)
                .build();
        return MockServerWebExchange.from(request);
    }

    // -------------------------------------------------------------------------
    // Bug Condition Tests — expected to FAIL on unfixed code
    // -------------------------------------------------------------------------

    /**
     * Scoped PBT: preflight with ONLY x-user-name in Access-Control-Request-Headers.
     *
     * <p>Counterexample on unfixed code:
     * {@code Access-Control-Allow-Headers = "Origin,Content-Type,Accept,Authorization,X-Requested-With"}
     * — does not contain "x-user-name".
     */
    @Test
    @DisplayName("Preflight with x-user-name only — Access-Control-Allow-Headers must permit x-user-name")
    void preflight_xUserNameOnly_mustBePermitted() {
        GatewayCorsWebFilter filter = new GatewayCorsWebFilter();
        MockServerWebExchange exchange = preflightExchange("x-user-name");

        StepVerifier.create(filter.filter(exchange, PASSTHROUGH_CHAIN))
                .verifyComplete();

        String allowedHeaders = exchange.getResponse()
                .getHeaders()
                .getFirst("Access-Control-Allow-Headers");

        // Property P: the response must permit x-user-name
        assertThat(allowedHeaders)
                .as("Access-Control-Allow-Headers must contain 'x-user-name' or be '*'")
                .satisfiesAnyOf(
                        h -> assertThat(h).containsIgnoringCase("x-user-name"),
                        h -> assertThat(h).isEqualTo("*")
                );
    }

    /**
     * Scoped PBT: preflight with x-user-name combined with Authorization.
     *
     * <p>Counterexample on unfixed code: same hardcoded list, still missing x-user-name.
     */
    @Test
    @DisplayName("Preflight with x-user-name + Authorization — Access-Control-Allow-Headers must permit x-user-name")
    void preflight_xUserNameAndAuthorization_mustBePermitted() {
        GatewayCorsWebFilter filter = new GatewayCorsWebFilter();
        MockServerWebExchange exchange = preflightExchange("x-user-name, Authorization");

        StepVerifier.create(filter.filter(exchange, PASSTHROUGH_CHAIN))
                .verifyComplete();

        String allowedHeaders = exchange.getResponse()
                .getHeaders()
                .getFirst("Access-Control-Allow-Headers");

        assertThat(allowedHeaders)
                .as("Access-Control-Allow-Headers must contain 'x-user-name' or be '*'")
                .satisfiesAnyOf(
                        h -> assertThat(h).containsIgnoringCase("x-user-name"),
                        h -> assertThat(h).isEqualTo("*")
                );
    }

    /**
     * Parameterized / scoped PBT: preflight to various routes with x-user-name.
     *
     * <p>Verifies the bug is route-independent — it affects /products, /cart, /auth, etc.
     */
    @ParameterizedTest(name = "Route: {0}")
    @ValueSource(strings = {"/products", "/cart", "/auth/login", "/orders", "/payments"})
    @DisplayName("Preflight with x-user-name to any route — Access-Control-Allow-Headers must permit x-user-name")
    void preflight_xUserName_anyRoute_mustBePermitted(String path) {
        GatewayCorsWebFilter filter = new GatewayCorsWebFilter();

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8080" + path)
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "x-user-name")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, PASSTHROUGH_CHAIN))
                .verifyComplete();

        String allowedHeaders = exchange.getResponse()
                .getHeaders()
                .getFirst("Access-Control-Allow-Headers");

        assertThat(allowedHeaders)
                .as("Access-Control-Allow-Headers for route %s must contain 'x-user-name' or be '*'", path)
                .satisfiesAnyOf(
                        h -> assertThat(h).containsIgnoringCase("x-user-name"),
                        h -> assertThat(h).isEqualTo("*")
                );
    }

    /**
     * Preflight with x-user-name and Content-Type — both must be permitted.
     */
    @Test
    @DisplayName("Preflight with x-user-name + Content-Type — Access-Control-Allow-Headers must permit x-user-name")
    void preflight_xUserNameAndContentType_mustBePermitted() {
        GatewayCorsWebFilter filter = new GatewayCorsWebFilter();
        MockServerWebExchange exchange = preflightExchange("x-user-name, Content-Type");

        StepVerifier.create(filter.filter(exchange, PASSTHROUGH_CHAIN))
                .verifyComplete();

        String allowedHeaders = exchange.getResponse()
                .getHeaders()
                .getFirst("Access-Control-Allow-Headers");

        assertThat(allowedHeaders)
                .as("Access-Control-Allow-Headers must contain 'x-user-name' or be '*'")
                .satisfiesAnyOf(
                        h -> assertThat(h).containsIgnoringCase("x-user-name"),
                        h -> assertThat(h).isEqualTo("*")
                );
    }

    /**
     * Confirms the exact counterexample: the hardcoded header list from GatewayCorsWebFilter.
     *
     * <p>This test documents the observed defective value so the root cause is unambiguous.
     */
    @Test
    @DisplayName("Counterexample documentation — hardcoded header list does not include x-user-name")
    void counterexample_hardcodedHeaderList_doesNotIncludeXUserName() {
        GatewayCorsWebFilter filter = new GatewayCorsWebFilter();
        MockServerWebExchange exchange = preflightExchange("x-user-name");

        StepVerifier.create(filter.filter(exchange, PASSTHROUGH_CHAIN))
                .verifyComplete();

        String allowedHeaders = exchange.getResponse()
                .getHeaders()
                .getFirst("Access-Control-Allow-Headers");

        // Document the counterexample: the hardcoded value is exactly this string
        // This assertion PASSES on unfixed code (it confirms the bug) and FAILS after the fix
        // (which is expected — after the fix this test class is superseded by the post-fix run)
        assertThat(allowedHeaders)
                .as("Counterexample: GatewayCorsWebFilter writes this hardcoded value")
                .isEqualTo("Origin,Content-Type,Accept,Authorization,X-Requested-With");
    }
}
