package com.poolguard.state;

public record SafetySnapshot(String label, int level, String reason, double confidence) { }
