package com.poolguard.state;

@FunctionalInterface
public interface SafetyObserver {
    void onSafetyChanged(SafetySnapshot snapshot);
}
