package com.example.deletion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small HTTP client for the two controls used by account deletion. */
public final class InfraiClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
    }

    public String listSessions(String userId) throws IOException, InterruptedException {
        return send("GET", "/v1/auth/session/list_for_user/" + encode(userId), "{}");
    }

    public String revokeSession(String sessionId, String idempotencyKey) throws IOException, InterruptedException {
        String body = idempotencyKey == null ? "" : "{\"idempotency_key\":" + jsonString(idempotencyKey) + "}";
        return send("POST", "/v1/auth/session/revoke/" + encode(sessionId), body);
    }

    public String revokeAccountKey(String keyId) throws IOException, InterruptedException {
        return send("DELETE", "/v1/account/keys/revoke/" + encode(keyId), "");
    }

    private String send(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .method(method, body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (!body.isEmpty()) builder.header("Content-Type", "application/json");
        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (!envelope.contains("\"ok\":true")) {
            throw new InfraiException("Infrai rejected request: " + envelope, response.statusCode());
        }
        if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: " + response.statusCode());
        return envelope;
    }

    private static String encode(String value) { return value.replace("/", "%2F"); }
    private static String jsonString(String value) {
        StringBuilder json = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') json.append('\\');
            if (c < 0x20) {
                json.append(String.format("\\u%04x", (int) c));
            } else {
                json.append(c);
            }
        }
        return json.append('"').toString();
    }
    public static final class InfraiException extends IOException {
        private final int status;
        public InfraiException(String message, int status) { super(message); this.status = status; }
        public int status() { return status; }
    }
}
