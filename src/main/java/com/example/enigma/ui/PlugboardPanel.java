package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;
import com.example.enigma.model.MachineListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlugboardPanel extends JPanel implements MachineListener {

    private static final String[] ROWS = {"QWERTZUIO", "ASDFGHJK", "PYXCVBNML"};

    private final EnigmaController controller;
    private final Map<Character, Jack> jacks = new LinkedHashMap<>();
    private final JLabel pairsLabel = new JLabel(" ");
    private Character pendingSelection;

    public PlugboardPanel(EnigmaController controller) {
        super(new BorderLayout(6, 6));
        this.controller = controller;
        setBorder(new TitledBorder("Clavijero"));

        JPanel keysPanel = new JPanel(new GridLayout(ROWS.length, 1, 0, 4));
        keysPanel.setBackground(Theme.PANEL_BACKGROUND);
        for (String row : ROWS) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
            rowPanel.setOpaque(false);
            for (char letter : row.toCharArray()) {
                Jack jack = new Jack(letter);
                jacks.put(letter, jack);
                rowPanel.add(jack);
            }
            keysPanel.add(rowPanel);
        }
        add(keysPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setBorder(new EmptyBorder(4, 8, 8, 8));

        pairsLabel.setFont(pairsLabel.getFont().deriveFont(Font.BOLD, 14f));
        bottom.add(pairsLabel, BorderLayout.CENTER);

        JButton clearButton = new JButton("Quitar todos los cables");
        clearButton.addActionListener(e -> {
            controller.clearPlugboard();
            pendingSelection = null;
            refresh();
        });
        bottom.add(clearButton, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        refresh();
        controller.addListener(this);
    }

    private void onJackClicked(char letter) {
        if (controller.plugboard().connectedLetters().contains(letter)) {
            controller.disconnectPlug(letter);
            pendingSelection = null;
        } else if (pendingSelection == null) {
            pendingSelection = letter;
        } else if (pendingSelection == letter) {
            pendingSelection = null;
        } else {
            try {
                controller.connectPlug(pendingSelection, letter);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede conectar", JOptionPane.WARNING_MESSAGE);
            }
            pendingSelection = null;
        }
        refresh();
    }

    @Override
    public void plugboardChanged() {
        SwingUtilities.invokeLater(this::refresh);
    }

    private void refresh() {
        List<String> pairs = controller.plugboard().pairs();
        for (Map.Entry<Character, Jack> entry : jacks.entrySet()) {
            char letter = entry.getKey();
            Color color = colorForLetter(letter, pairs);
            boolean selected = pendingSelection != null && pendingSelection == letter;
            entry.getValue().updateState(color, selected);
        }

        if (pairs.isEmpty()) {
            pairsLabel.setText("Sin cables conectados");
        } else {
            int total = controller.plugboard().pairCount();
            pairsLabel.setText(String.join("  ", pairs) + "   (" + total + "/13 pares)");
        }
    }

    private static Color colorForLetter(char letter, List<String> pairs) {
        for (int i = 0; i < pairs.size(); i++) {
            if (pairs.get(i).indexOf(letter) >= 0) {
                return paletteColor(i);
            }
        }
        return null;
    }

    private static Color paletteColor(int index) {
        float hue = (index * 0.37f) % 1f;
        return Color.getHSBColor(hue, 0.65f, 0.85f);
    }

    private final class Jack extends JComponent {
        private final char letter;
        private Color plugColor;
        private boolean selected;

        Jack(char letter) {
            this.letter = letter;
            setPreferredSize(new Dimension(42, 42));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    onJackClicked(letter);
                }
            });
        }

        void updateState(Color plugColor, boolean selected) {
            this.plugColor = plugColor;
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            Color fill = plugColor != null ? plugColor : Theme.KEY_IDLE;
            g2.setColor(fill);
            g2.fillOval(x, y, size, size);

            g2.setColor(selected ? Color.WHITE : Theme.KEY_RING);
            g2.setStroke(new java.awt.BasicStroke(selected ? 3f : 1f));
            g2.drawOval(x, y, size, size);

            g2.setColor(plugColor != null ? Color.BLACK : Theme.TEXT_LIGHT);
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
