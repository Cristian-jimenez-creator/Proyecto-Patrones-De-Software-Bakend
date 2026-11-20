package com.poolguard.risk;

public enum RiskLevel {
    NORMAL(0), CAUTION(1), DANGER(2);
    private final int code;
    RiskLevel(int code) { this.code = code; }
    public int code() { return code; }
    public static RiskLevel fromCode(int code) {
        return switch (code) {
            case 0 -> NORMAL;
            case 1 -> CAUTION;
            case 2 -> DANGER;
            default -> throw new IllegalArgumentException("Unknown risk level: " + code);
        };
    }
}
