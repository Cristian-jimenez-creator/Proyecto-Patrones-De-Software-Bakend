package com.poolguard.risk;

import com.poolguard.camera.CameraClient;
import java.util.Optional;

public interface RiskAnalysisStrategy {
    Optional<RiskAnalysis> analyze(CameraClient.Frame frame);
}
