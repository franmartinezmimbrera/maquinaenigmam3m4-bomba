package com.example.enigma.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class AppIcon {

    private AppIcon() {
    }

    static BufferedImage create(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = size / 12;
        int diameter = size - margin * 2;

        g2.setColor(new Color(60, 56, 52));
        g2.fillOval(margin, margin, diameter, diameter);

        g2.setColor(new Color(150, 140, 120));
        g2.setStroke(new java.awt.BasicStroke(Math.max(1f, size / 24f)));
        g2.drawOval(margin, margin, diameter, diameter);

        int lampSize = Math.max(2, size / 10);
        g2.setColor(new Color(255, 205, 60));
        int ringRadius = diameter / 2 - lampSize;
        int cx = size / 2;
        int cy = size / 2;
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(90 + i * 72);
            int lx = (int) (cx + ringRadius * Math.cos(angle)) - lampSize / 2;
            int ly = (int) (cy - ringRadius * Math.sin(angle)) - lampSize / 2;
            g2.fillOval(lx, ly, lampSize, lampSize);
        }

        g2.setColor(new Color(30, 28, 26));
        int coreSize = diameter / 3;
        g2.fillOval(cx - coreSize / 2, cy - coreSize / 2, coreSize, coreSize);

        g2.dispose();
        return image;
    }
}
