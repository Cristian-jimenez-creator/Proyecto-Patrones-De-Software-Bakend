package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

public final class WaitingForAnalysisState implements PoolSafetyState {
    @Override public SafetySnapshot snapshot() { return new SafetySnapshot("WAITING FOR REAL ANALYSIS", -1, "Risk service not configured or unavailable", 0); }
    @Override public PoolSafetyState apply(RiskAnalysis analysis) { return StateFactory.from(analysis); }
}
