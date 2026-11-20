package com.poolguard.state;

import com.poolguard.risk.RiskAnalysis;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SafetyStateMachine {
    private final List<SafetyObserver> observers = new CopyOnWriteArrayList<>();
    private PoolSafetyState current = new WaitingForAnalysisState();

    public synchronized void apply(RiskAnalysis analysis) {
        current = current.apply(analysis);
        SafetySnapshot snapshot = current.snapshot();
        observers.forEach(observer -> observer.onSafetyChanged(snapshot));
    }

    public synchronized SafetySnapshot current() { return current.snapshot(); }
    public void addObserver(SafetyObserver observer) { observers.add(observer); }
}
