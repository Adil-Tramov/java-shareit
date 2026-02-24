package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          Map<String, Object> parameters, T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        // ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ
        System.out.println("\n========== GATEWAY REQUEST DEBUG ==========");
        System.out.println("Method: " + method);
        System.out.println("Full URL to server: http://localhost:9090" + path);
        System.out.println("Path: " + path);
        System.out.println("User ID: " + userId);
        System.out.println("Parameters: " + parameters);
        System.out.println("Request Body: " + (body != null ? body : "null"));
        System.out.println("==========================================\n");

        ResponseEntity<Object> shareitServerResponse;
        try {
            if (parameters != null) {
                shareitServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                shareitServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }

            System.out.println("\n========== SERVER RESPONSE ==========");
            System.out.println("Status code: " + shareitServerResponse.getStatusCode());
            System.out.println("Status value: " + shareitServerResponse.getStatusCode().value());
            System.out.println("Response body: " + shareitServerResponse.getBody());
            System.out.println("======================================\n");

        } catch (HttpStatusCodeException e) {
            System.out.println("\n========== SERVER EXCEPTION ==========");
            System.out.println("Exception status code: " + e.getStatusCode());
            System.out.println("Exception status value: " + e.getStatusCode().value());
            System.out.println("Exception response body: " + e.getResponseBodyAsString());
            System.out.println("Exception headers: " + e.getResponseHeaders());
            System.out.println("======================================\n");

            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}