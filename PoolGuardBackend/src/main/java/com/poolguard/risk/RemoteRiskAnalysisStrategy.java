package com.poolguard.risk;

import com.poolguard.camera.CameraClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RemoteRiskAnalysisStrategy implements RiskAnalysisStrategy {
    private static final Pattern LEVEL = Pattern.compile("\\\"level\\\"\\s*:\\s*(\\d)");
    private static final Pattern CONFIDENCE = Pattern.compile("\\\"confidence\\\"\\s*:\\s*([0-9.]+)");
    private static final Pattern REASON = Pattern.compile("\\\"reason\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private final URI endpoint;
    private final HttpClient client = HttpClient.newHttpClient();

    public RemoteRiskAnalysisStrategy(URI endpoint) { this.endpoint = endpoint; }

    @Override
    public Optional<RiskAnalysis> analyze(CameraClient.Frame frame) {
        if (endpoint == null) return Optional.empty();
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "image/jpeg")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(frame.jpeg()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) return Optional.empty();
            return Optional.of(parse(response.body()));
        } catch (Exception exception) {
            System.err.println("Risk analysis failed: " + exception.getMessage());
            return Optional.empty();
        }
    }

    private RiskAnalysis parse(String json) {
        int level = Integer.parseInt(value(LEVEL, json));
        double confidence = Double.parseDouble(value(CONFIDENCE, json));
        String reason = value(REASON, json);
        return new RiskAnalysis(RiskLevel.fromCode(level), confidence, reason);
    }

    private String value(Pattern pattern, String json) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Invalid risk response");
        return matcher.group(1);
    }
}
