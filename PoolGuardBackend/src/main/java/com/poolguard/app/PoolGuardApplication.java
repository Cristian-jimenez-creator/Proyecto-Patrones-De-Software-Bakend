package com.poolguard.app;

import com.poolguard.camera.CameraClient;
import com.poolguard.camera.CameraClientFactory;
import com.poolguard.camera.SecuredCameraClientProxy;
import com.poolguard.config.ApplicationContext;
import com.poolguard.monitor.PoolMonitoringFacade;
import com.poolguard.risk.RemoteRiskAnalysisStrategy;
import com.poolguard.ui.MonitorWindow;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.CountDownLatch;

public final class PoolGuardApplication {
    private PoolGuardApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = ApplicationContext.getInstance();
        CameraClient camera = CameraClientFactory.create(context.cameraUrl());
        CameraClient securedCamera = new SecuredCameraClientProxy(camera, context.operatorKey(), context.sessionKey());
        PoolMonitoringFacade monitor = new PoolMonitoringFacade(
                securedCamera,
                new RemoteRiskAnalysisStrategy(context.riskApiUrl())
        );

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                MonitorWindow window = new MonitorWindow(monitor);
                window.showWindow();
            });
        } else {
            monitor.start();
            Runtime.getRuntime().addShutdownHook(new Thread(monitor::stop));
            new CountDownLatch(1).await();
        }
    }
}
