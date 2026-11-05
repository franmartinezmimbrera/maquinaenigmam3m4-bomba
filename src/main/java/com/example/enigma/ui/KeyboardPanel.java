package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class KeyboardPanel extends JPanel {

    public interface KeyPressListener {
        void keyPressed(char letter, char output);
    }

    private static final String[] ROWS = {"QWERTZUIO", "ASDFGHJK", "PYXCVBNML"};

    private final EnigmaController controller;
    private final KeyPressListener listener;
    private final Map<Character, Key> keys = new LinkedHashMap<>();
    private final Set<Character> physicallyPressed = ConcurrentHashMap.newKeySet();

    public KeyboardPanel(EnigmaController controller, KeyPressListener listener) {
        super(new GridLayout(ROWS.length, 1, 0, 4));
        this.controller = controller;
        this.listener = listener;
        setBackground(Theme.PANEL_BACKGROUND);

        for (String row : ROWS) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
            rowPanel.setOpaque(false);
            for (char letter : row.toCharArray()) {
                Key key = new Key(letter);
                keys.put(letter, key);
                rowPanel.add(key);
            }
            add(rowPanel);
        }

        registerPhysicalKeyboard();
    }

    private void registerPhysicalKeyboard() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextArea) {
                return false;
            }
            char letter = Character.toUpperCase(e.getKeyChar());
            if (!keys.containsKey(letter)) {
                return false;
            }
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (physicallyPressed.add(letter)) {
                    press(letter);
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                physicallyPressed.remove(letter);
                release(letter);
            }
            return false;
        });
    }

    private void press(char letter) {
        char output = controller.pressKey(letter);
        Key key = keys.get(letter);
        if (key != null) {
            key.setPressed(true);
        }
        listener.keyPressed(letter, output);
    }

    private void release(char letter) {
        controller.releaseKey();
        Key key = keys.get(letter);
        if (key != null) {
            key.setPressed(false);
        }
    }

    private final class Key extends JComponent {
        private final char letter;
        private boolean pressed;

        Key(char letter) {
            this.letter = letter;
            setPreferredSize(new Dimension(46, 46));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    press(letter);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    release(letter);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (pressed) {
                        release(letter);
                    }
                }
            });
        }

        void setPressed(boolean pressed) {
            if (this.pressed != pressed) {
                this.pressed = pressed;
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

            g2.setColor(pressed ? Theme.KEY_PRESSED : Theme.KEY_IDLE);
            g2.fillOval(x, y, size, size);
            g2.setColor(Theme.KEY_RING);
            g2.drawOval(x, y, size, size);

            g2.setColor(Theme.TEXT_LIGHT);
            g2.setFont(getFont().deriveFont(Font.BOLD, size * 0.4f));
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(letter);
            int textX = x + (size - fm.stringWidth(text)) / 2;
            int textY = y + (size + fm.getAscent()) / 2 - 2;
            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }
}
