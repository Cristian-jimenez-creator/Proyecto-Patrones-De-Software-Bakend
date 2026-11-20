package com.poolguard.risk;

public record RiskAnalysis(RiskLevel level, double confidence, String reason) { }
