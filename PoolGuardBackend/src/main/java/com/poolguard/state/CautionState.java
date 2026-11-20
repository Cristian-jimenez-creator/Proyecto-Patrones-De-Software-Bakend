package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

public final class CautionState implements PoolSafetyState {
    private final RiskAnalysis analysis;
    public CautionState(RiskAnalysis analysis) { this.analysis = analysis; }
    @Override public SafetySnapshot snapshot() { return new SafetySnapshot("CAUTION", 1, analysis.reason(), analysis.confidence()); }
    @Override public PoolSafetyState apply(RiskAnalysis analysis) { return StateFactory.from(analysis); }
}
