package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

public interface PoolSafetyState {
    SafetySnapshot snapshot();
    PoolSafetyState apply(RiskAnalysis analysis);
}
