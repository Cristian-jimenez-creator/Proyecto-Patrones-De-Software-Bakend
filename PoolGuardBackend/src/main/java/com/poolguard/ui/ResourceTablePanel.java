package com.poolguard.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public final class ResourceTablePanel extends JPanel {
    private final JLabel message = new JLabel("Waiting for backend data", SwingConstants.CENTER);
    private final DefaultTableModel model;
    public ResourceTablePanel(String title, String[] columns, Runnable refresh) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        add(heading, BorderLayout.NORTH);
        model = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(message, BorderLayout.CENTER);
        JButton button = new JButton("Refresh from backend");
        button.addActionListener(event -> refresh.run());
        footer.add(button, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }
    public void clear() { model.setRowCount(0); }
    public void setMessage(String value) { message.setText(value); }
}
