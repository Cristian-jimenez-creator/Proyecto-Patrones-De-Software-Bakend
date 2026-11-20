package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

public final class NormalState implements PoolSafetyState {
    private final RiskAnalysis analysis;
    public NormalState(RiskAnalysis analysis) { this.analysis = analysis; }
    @Override public SafetySnapshot snapshot() { return new SafetySnapshot("NORMAL", 0, analysis.reason(), analysis.confidence()); }
    @Override public PoolSafetyState apply(RiskAnalysis analysis) { return StateFactory.from(analysis); }
}
