package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

public final class DangerState implements PoolSafetyState {
    private final RiskAnalysis analysis;
    public DangerState(RiskAnalysis analysis) { this.analysis = analysis; }
    @Override public SafetySnapshot snapshot() { return new SafetySnapshot("DANGER", 2, analysis.reason(), analysis.confidence()); }
    @Override public PoolSafetyState apply(RiskAnalysis analysis) { return StateFactory.from(analysis); }
}
