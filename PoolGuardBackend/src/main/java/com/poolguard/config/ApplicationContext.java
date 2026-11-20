package com.poolguard.config;

import java.net.URI;

public final class ApplicationContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();
    private final URI cameraUrl;
    private final URI riskApiUrl;
    private final URI backendUrl;
    private final String operatorKey;
    private final String sessionKey;

    private ApplicationContext() {
        this.cameraUrl = URI.create(value("POOLGUARD_CAMERA_URL", "http://192.168.101.11:8080/video"));
        String riskApi = System.getenv("POOLGUARD_RISK_API");
        this.riskApiUrl = riskApi == null || riskApi.isBlank() ? null : URI.create(riskApi);
        String backendApi = System.getenv("POOLGUARD_BACKEND_URL");
        this.backendUrl = backendApi == null || backendApi.isBlank() ? null : URI.create(backendApi);
        this.operatorKey = System.getenv("POOLGUARD_OPERATOR_KEY");
        this.sessionKey = System.getenv("POOLGUARD_SESSION_KEY");
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public URI cameraUrl() {
        return cameraUrl;
    }

    public URI riskApiUrl() {
        return riskApiUrl;
    }

    public URI backendUrl() {
        return backendUrl;
    }

    public String operatorKey() {
        return operatorKey;
    }

    public String sessionKey() {
        return sessionKey;
    }

    private static String value(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
