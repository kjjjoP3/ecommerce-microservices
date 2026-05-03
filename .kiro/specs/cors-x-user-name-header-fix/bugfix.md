# Bugfix Requirements Document

## Introduction

The API Gateway (port 8080) blocks preflight CORS requests from the Vue.js frontend (http://localhost:5173) whenever the request includes the custom `x-user-name` header. The `GatewayCorsWebFilter` component sets a hardcoded `Access-Control-Allow-Headers` response header that does not include `x-user-name`, causing browsers to reject the request before it reaches the backend. This affects all routes proxied through the gateway, including `/auth/**` and `/products/**`, and prevents authenticated users from making API calls.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the frontend sends a preflight OPTIONS request to any API Gateway route with the `x-user-name` header THEN the system responds with `Access-Control-Allow-Headers: Origin,Content-Type,Accept,Authorization,X-Requested-With`, which does not include `x-user-name`

1.2 WHEN the browser receives a preflight response that does not list `x-user-name` in `Access-Control-Allow-Headers` THEN the system blocks the actual request with a CORS policy error

1.3 WHEN the `GatewayCorsWebFilter` processes any request THEN the system overwrites the `Access-Control-Allow-Headers` value set by `CorsConfig` and `application.yml` with the hardcoded list, ignoring the wildcard `*` configuration

### Expected Behavior (Correct)

2.1 WHEN the frontend sends a preflight OPTIONS request to any API Gateway route with the `x-user-name` header THEN the system SHALL respond with an `Access-Control-Allow-Headers` value that includes `x-user-name`, permitting the browser to proceed

2.2 WHEN the browser receives a preflight response that includes `x-user-name` in `Access-Control-Allow-Headers` THEN the system SHALL allow the actual request to proceed to the backend service

2.3 WHEN the `GatewayCorsWebFilter` processes a preflight request THEN the system SHALL NOT override the allowed headers configuration with a hardcoded list that excludes custom application headers

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the frontend sends a request with standard headers (`Content-Type`, `Authorization`, `Accept`) THEN the system SHALL CONTINUE TO allow those requests through the gateway without CORS errors

3.2 WHEN the frontend sends a request from the allowed origin `http://localhost:5173` THEN the system SHALL CONTINUE TO include `Access-Control-Allow-Origin: http://localhost:5173` in the response

3.3 WHEN the frontend sends a request with credentials THEN the system SHALL CONTINUE TO respond with `Access-Control-Allow-Credentials: true`

3.4 WHEN the frontend sends a preflight request for any HTTP method (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) THEN the system SHALL CONTINUE TO respond with `200 OK` and the appropriate `Access-Control-Allow-Methods` header

3.5 WHEN a request originates from an origin other than `http://localhost:5173` or `http://127.0.0.1:5173` THEN the system SHALL CONTINUE TO reject it with a CORS error
