package com.poolguard.monitor;

import com.poolguard.camera.CameraClient;
import com.poolguard.camera.FrameObserver;
import com.poolguard.risk.RiskAnalysisStrategy;
import com.poolguard.state.SafetyObserver;
import com.poolguard.state.SafetyStateMachine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public final class PoolMonitoringFacade {
    private final CameraClient camera;
    private final CameraFrameSource source;
    private final RiskAnalysisStrategy analysisStrategy;
    private final SafetyStateMachine safety = new SafetyStateMachine();
    private final ExecutorService analyzer = Executors.newSingleThreadExecutor();
    private final AtomicLong lastAnalysis = new AtomicLong();

    public PoolMonitoringFacade(CameraClient camera, RiskAnalysisStrategy analysisStrategy) {
        this.camera = camera;
        this.source = new CameraFrameSourceAdapter(camera);
        this.analysisStrategy = analysisStrategy;
        source.subscribe(this::analyzeFrame);
    }

    public void start() { camera.start(); }
    public void stop() { camera.stop(); analyzer.shutdownNow(); }
    public void addFrameObserver(FrameObserver observer) { camera.addFrameObserver(observer); }
    public void addSafetyObserver(SafetyObserver observer) { safety.addObserver(observer); observer.onSafetyChanged(safety.current()); }

    private void analyzeFrame(CameraClient.Frame frame) {
        long now = System.currentTimeMillis();
        if (now - lastAnalysis.get() < 1_000 || !lastAnalysis.compareAndSet(lastAnalysis.get(), now)) return;
        analyzer.submit(() -> analysisStrategy.analyze(frame).ifPresent(safety::apply));
    }
}
