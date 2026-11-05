package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;
import com.example.enigma.model.Alphabet;
import com.example.enigma.model.MachineListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class RotorWindowPanel extends JPanel implements MachineListener {

    private static final int MAX_ROTORS = 4;
    private static final String[] TITLES = {"1er rotor", "2º rotor", "3er rotor", "4º rotor (M4)"};

    private final EnigmaController controller;
    private final JPanel[] widgets = new JPanel[MAX_ROTORS];
    private final JLabel[] previousLabels = new JLabel[MAX_ROTORS];
    private final JLabel[] currentLabels = new JLabel[MAX_ROTORS];
    private final JLabel[] nextLabels = new JLabel[MAX_ROTORS];

    public RotorWindowPanel(EnigmaController controller, Runnable openSettings) {
        super(new FlowLayout(FlowLayout.CENTER, 12, 10));
        this.controller = controller;
        setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < MAX_ROTORS; i++) {
            JPanel widget = buildRotorWidget(i, openSettings);
            widgets[i] = widget;
            add(widget);
        }

        refresh();
        controller.addListener(this);
    }

    private JPanel buildRotorWidget(int index, Runnable openSettings) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new java.awt.Dimension(110, 110));
        panel.setBorder(new TitledBorder(TITLES[index]));

        JLabel previous = new JLabel("", SwingConstants.CENTER);
        JLabel current = new JLabel("", SwingConstants.CENTER);
        JLabel next = new JLabel("", SwingConstants.CENTER);

        previous.setFont(previous.getFont().deriveFont(16f));
        next.setFont(next.getFont().deriveFont(16f));
        current.setFont(current.getFont().deriveFont(Font.BOLD, 30f));

        previousLabels[index] = previous;
        currentLabels[index] = current;
        nextLabels[index] = next;

        previous.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        next.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        current.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        current.setToolTipText("Clic para abrir los ajustes");

        previous.addMouseListener(rotateOnClick(index, -1));
        next.addMouseListener(rotateOnClick(index, 1));
        current.addMouseListener(openSettingsOnClick(openSettings));

        panel.addMouseWheelListener(e -> controller.rotateRotor(index, e.getWheelRotation() > 0 ? 1 : -1));
        panel.addMouseListener(openSettingsOnClick(openSettings));

        panel.add(previous, BorderLayout.NORTH);
        panel.add(current, BorderLayout.CENTER);
        panel.add(next, BorderLayout.SOUTH);
        return panel;
    }

    private MouseAdapter rotateOnClick(int index, int delta) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controller.rotateRotor(index, delta);
            }
        };
    }

    private MouseAdapter openSettingsOnClick(Runnable openSettings) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openSettings.run();
            }
        };
    }

    @Override
    public void rotorPositionsChanged(char left, char middle, char right) {
        SwingUtilities.invokeLater(this::refresh);
    }

    private void refresh() {
        boolean hasFourth = controller.hasFourthRotor();
        int visibleCount = hasFourth ? MAX_ROTORS : 3;
        for (int i = 0; i < MAX_ROTORS; i++) {
            widgets[i].setVisible(i < visibleCount);
        }
        for (int i = 0; i < visibleCount; i++) {
            char letter = controller.rotorPosition(i);
            int index = Alphabet.indexOf(letter);
            previousLabels[i].setText(String.valueOf(Alphabet.letterAt(index - 1)));
            currentLabels[i].setText(String.valueOf(letter));
            nextLabels[i].setText(String.valueOf(Alphabet.letterAt(index + 1)));
        }
        revalidate();
        repaint();
    }
}
