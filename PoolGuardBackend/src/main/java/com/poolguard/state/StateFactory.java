package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;

final class StateFactory {
    private StateFactory() { }
    static PoolSafetyState from(RiskAnalysis analysis) {
        return switch (analysis.level()) {
            case NORMAL -> new NormalState(analysis);
            case CAUTION -> new CautionState(analysis);
            case DANGER -> new DangerState(analysis);
        };
    }
}
