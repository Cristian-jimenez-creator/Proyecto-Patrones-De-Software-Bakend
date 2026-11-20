package com.poolguard.ui;

import com.poolguard.config.ApplicationContext;
import com.poolguard.monitor.PoolMonitoringFacade;
import com.poolguard.state.SafetySnapshot;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MonitorWindow {
    private static final Color NAVY = new Color(16, 31, 47);
    private final PoolMonitoringFacade monitor;
    private final BackendStatusClient backend = new BackendStatusClient(ApplicationContext.getInstance().backendUrl());
    private final JFrame frame = new JFrame("PoolGuard - Pool Safety Platform");
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel video = new JLabel("Connecting to IP camera...", SwingConstants.CENTER);
    private final JLabel status = new JLabel("WAITING FOR REAL ANALYSIS", SwingConstants.CENTER);
    private final JLabel detail = new JLabel("", SwingConstants.CENTER);
    private final JLabel backendStatus = new JLabel("Checking backend...", SwingConstants.RIGHT);
    private final Map<String, ResourceTablePanel> pages = new LinkedHashMap<>();

    public MonitorWindow(PoolMonitoringFacade monitor) { this.monitor = monitor; configure(); }
    public void showWindow() { frame.setVisible(true); monitor.start(); refreshBackendStatus(); }

    private void configure() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1180, 760));
        frame.setLayout(new BorderLayout());
        frame.add(header(), BorderLayout.NORTH);
        frame.add(navigation(), BorderLayout.WEST);
        createPages();
        frame.add(content, BorderLayout.CENTER);
        frame.add(footer(), BorderLayout.SOUTH);
        monitor.addFrameObserver(cameraFrame -> SwingUtilities.invokeLater(() -> showFrame(cameraFrame.image())));
        monitor.addSafetyObserver(snapshot -> SwingUtilities.invokeLater(() -> showSafety(snapshot)));
        frame.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent event) { monitor.stop(); } });
        frame.pack(); frame.setLocationRelativeTo(null);
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(NAVY); panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel brand = new JLabel("POOLGUARD"); brand.setForeground(Color.WHITE); brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel subtitle = new JLabel("  Live pool safety monitoring"); subtitle.setForeground(new Color(189, 211, 231));
        JPanel left = new JPanel(new BorderLayout()); left.setOpaque(false); left.add(brand, BorderLayout.WEST); left.add(subtitle, BorderLayout.CENTER);
        backendStatus.setForeground(Color.WHITE); panel.add(left, BorderLayout.WEST); panel.add(backendStatus, BorderLayout.EAST); return panel;
    }

    private JPanel navigation() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setBackground(NAVY); panel.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        ButtonGroup group = new ButtonGroup();
        addNav(panel, group, "Dashboard", "DASHBOARD", true);
        addNav(panel, group, "Pools", "POOLS", false);
        addNav(panel, group, "Cameras", "CAMERAS", false);
        addNav(panel, group, "Active Alerts", "ALERTS", false);
        addNav(panel, group, "Incidents", "INCIDENTS", false);
        addNav(panel, group, "Users", "USERS", false);
        return panel;
    }

    private void addNav(JPanel panel, ButtonGroup group, String label, String page, boolean selected) {
        JToggleButton button = new JToggleButton(label, selected);
        button.setFocusPainted(false); button.setHorizontalAlignment(SwingConstants.LEFT); button.setForeground(Color.WHITE);
        button.setBackground(selected ? new Color(35, 87, 132) : NAVY); button.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 40));
        button.addActionListener(event -> cards.show(content, page)); group.add(button); panel.add(button);
    }

    private void createPages() {
        content.add(dashboard(), "DASHBOARD");
        addPage("POOLS", "Pools", new String[]{"Name", "Location", "Capacity", "Status"});
        addPage("CAMERAS", "Cameras", new String[]{"Name", "Pool", "Stream URL", "Connection", "Status"});
        addPage("ALERTS", "Active Alerts", new String[]{"Created at", "Pool", "Camera", "Risk", "Status"});
        addPage("INCIDENTS", "Incidents", new String[]{"Created at", "Pool", "Camera", "Classification", "Status"});
        addPage("USERS", "Users", new String[]{"Name", "Email", "Role", "Account status"});
    }

    private JPanel dashboard() {
        JPanel panel = new JPanel(new BorderLayout(16, 16)); panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel risk = new JPanel(new BorderLayout()); status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24)); status.setOpaque(true); status.setForeground(Color.WHITE);
        risk.add(status, BorderLayout.CENTER); detail.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5)); risk.add(detail, BorderLayout.SOUTH); panel.add(risk, BorderLayout.NORTH);
        video.setOpaque(true); video.setBackground(Color.BLACK); video.setForeground(Color.WHITE); panel.add(video, BorderLayout.CENTER);
        JPanel controls = new JPanel(); JButton check = new JButton("Check backend connection"); check.addActionListener(event -> refreshBackendStatus());
        JButton stop = new JButton("Stop monitoring"); stop.addActionListener(event -> monitor.stop()); controls.add(check); controls.add(stop); panel.add(controls, BorderLayout.SOUTH); return panel;
    }

    private void addPage(String key, String title, String[] columns) {
        ResourceTablePanel panel = new ResourceTablePanel(title, columns, () -> refreshResource(key)); pages.put(key, panel); content.add(panel, key);
    }
    private JPanel footer() { JPanel panel = new JPanel(new BorderLayout()); panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); panel.add(new JLabel("Camera: " + ApplicationContext.getInstance().cameraUrl()), BorderLayout.WEST); panel.add(new JLabel("No synthetic detections"), BorderLayout.EAST); return panel; }
    private void refreshBackendStatus() { Thread.ofVirtual().start(() -> { String message = backend.health(); SwingUtilities.invokeLater(() -> backendStatus.setText(message)); }); }
    private void refreshResource(String key) { ResourceTablePanel page = pages.get(key); page.clear(); Thread.ofVirtual().start(() -> { String message = backend.health(); SwingUtilities.invokeLater(() -> page.setMessage(message + ". Backend records will appear after API deployment.")); }); }
    private void showFrame(java.awt.image.BufferedImage image) { Image scaled = image.getScaledInstance(Math.max(1, video.getWidth()), Math.max(1, video.getHeight()), Image.SCALE_SMOOTH); video.setText(""); video.setIcon(new ImageIcon(scaled)); }
    private void showSafety(SafetySnapshot snapshot) { status.setText(snapshot.label()); status.setBackground(switch (snapshot.level()) { case 0 -> new Color(26, 127, 58); case 1 -> new Color(194, 142, 0); case 2 -> new Color(183, 38, 38); default -> new Color(72, 78, 88); }); detail.setText(snapshot.reason() + (snapshot.level() >= 0 ? " | Confidence: " + Math.round(snapshot.confidence() * 100) + "%" : "")); }
}
