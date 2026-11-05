package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public final class TextPanel extends JPanel {

    private final JTextArea plainTextArea = new JTextArea(6, 40);
    private final JTextArea cipherTextArea = new JTextArea(6, 40);

    public TextPanel(EnigmaController controller, Runnable onShowSteps) {
        super(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel areas = new JPanel(new GridLayout(1, 2, 8, 0));
        areas.add(labeled("Texto plano", plainTextArea));
        areas.add(labeled("Texto cifrado", cipherTextArea));
        add(areas, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton cifrar = new JButton("Cifrar");
        JButton descifrar = new JButton("Descifrar");
        JButton limpiar = new JButton("Limpiar");
        JButton pasos = new JButton("Mostrar pasos de cifrado");

        cifrar.setToolTipText("Cifra el texto plano con la configuración actual (Ctrl+Enter)");
        descifrar.setToolTipText("Descifra el texto cifrado con la configuración actual (Ctrl+D)");
        limpiar.setToolTipText("Borra los textos y la traza acumulada (Ctrl+L)");
        pasos.setToolTipText("Muestra el recorrido de cada letra del último cifrado/descifrado");

        buttons.add(cifrar);
        buttons.add(descifrar);
        buttons.add(limpiar);
        buttons.add(pasos);
        add(buttons, BorderLayout.SOUTH);

        cifrar.addActionListener(e -> cipherTextArea.setText(controller.encrypt(plainTextArea.getText())));
        descifrar.addActionListener(e -> plainTextArea.setText(controller.decrypt(cipherTextArea.getText())));
        limpiar.addActionListener(e -> {
            plainTextArea.setText("");
            cipherTextArea.setText("");
            controller.clearTraces();
        });
        pasos.addActionListener(e -> onShowSteps.run());

        registerShortcuts(cifrar, descifrar, limpiar);
    }

    private void registerShortcuts(JButton cifrar, JButton descifrar, JButton limpiar) {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "cifrar");
        actionMap.put("cifrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cifrar.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "descifrar");
        actionMap.put("descifrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                descifrar.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "limpiar");
        actionMap.put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar.doClick();
            }
        });
    }

    public void appendTypedLetter(char plainLetter, char cipherLetter) {
        plainTextArea.append(String.valueOf(plainLetter));
        cipherTextArea.append(String.valueOf(cipherLetter));
    }

    private static JPanel labeled(String title, JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(title));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }
}
