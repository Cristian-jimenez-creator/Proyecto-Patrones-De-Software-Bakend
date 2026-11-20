package com.poolguard.ui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class BackendStatusClient {
    private final URI backendUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public BackendStatusClient(URI backendUrl) { this.backendUrl = backendUrl; }

    public String health() {
        if (backendUrl == null) return "Backend URL is not configured";
        try {
            HttpRequest request = HttpRequest.newBuilder(backendUrl.resolve("/actuator/health"))
                    .timeout(Duration.ofSeconds(4)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? "Backend connected" : "Backend returned HTTP " + response.statusCode();
        } catch (Exception exception) {
            return "Backend unavailable";
        }
    }
}
