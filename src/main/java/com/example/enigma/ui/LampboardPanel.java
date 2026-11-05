package com.example.enigma.ui;

import com.example.enigma.model.MachineListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LampboardPanel extends JPanel implements MachineListener {

    private static final String[] ROWS = {"QWERTZUIO", "ASDFGHJK", "PYXCVBNML"};

    private final Map<Character, Lamp> lamps = new LinkedHashMap<>();

    public LampboardPanel() {
        super(new GridLayout(ROWS.length, 1, 0, 4));
        setBackground(Theme.PANEL_BACKGROUND);

        for (String row : ROWS) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
            rowPanel.setOpaque(false);
            for (char letter : row.toCharArray()) {
                Lamp lamp = new Lamp(letter);
                lamps.put(letter, lamp);
                rowPanel.add(lamp);
            }
            add(rowPanel);
        }
    }

    @Override
    public void lampLit(char letter) {
        SwingUtilities.invokeLater(() -> {
            lamps.values().forEach(lamp -> lamp.setLit(false));
            Lamp lamp = lamps.get(Character.toUpperCase(letter));
            if (lamp != null) {
                lamp.setLit(true);
            }
        });
    }

    @Override
    public void lampOff() {
        SwingUtilities.invokeLater(() -> lamps.values().forEach(lamp -> lamp.setLit(false)));
    }

    private static final class Lamp extends JComponent {
        private final char letter;
        private boolean lit;

        Lamp(char letter) {
            this.letter = letter;
            setPreferredSize(new Dimension(42, 42));
        }

        void setLit(boolean lit) {
            if (this.lit != lit) {
                this.lit = lit;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(lit ? Theme.LAMP_ON : Theme.LAMP_OFF);
            g2.fillOval(x, y, size, size);
            g2.setColor(Theme.LAMP_RING);
            g2.drawOval(x, y, size, size);

            g2.setColor(lit ? Color.BLACK : Theme.TEXT_LIGHT);
            g2.setFont(getFont().deriveFont(Font.BOLD, size * 0.45f));
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(letter);
            int textX = x + (size - fm.stringWidth(text)) / 2;
            int textY = y + (size + fm.getAscent()) / 2 - 2;
            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }
}
