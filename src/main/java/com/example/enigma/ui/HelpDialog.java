package com.example.enigma.ui;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

public final class HelpDialog extends JDialog {

    private static final String TEXT =
            "<html><body style='font-family: sans-serif; padding: 12px;'>\n" +
            "<h2>¿Cómo funciona la Enigma M3-M4?</h2>\n" +
            "<p>Cada letra que pulsas pasa por el <b>clavijero</b> (si esa letra está\n" +
            "conectada a otra, se intercambian), luego atraviesa los <b>tres rotores</b>\n" +
            "de derecha a izquierda, rebota en el <b>reflector</b> y vuelve a atravesar\n" +
            "los rotores de izquierda a derecha, para pasar de nuevo por el clavijero\n" +
            "antes de encender la lámpara de salida.</p>\n" +
            "<p>Antes de cifrar cada letra, los rotores giran: el rotor derecho avanza\n" +
            "siempre una posición; cuando un rotor llega a su <b>muesca</b>, hace avanzar\n" +
            "también al siguiente rotor hacia la izquierda (el famoso \"doble paso\").</p>\n" +
            "<p>Por eso la misma letra casi nunca se cifra igual dos veces seguidas, y\n" +
            "por eso Enigma es <b>simétrica</b>: con la misma configuración inicial,\n" +
            "cifrar dos veces el mismo texto devuelve el texto original.</p>\n" +
            "<p>Usa \"Rotores y reflector...\" para elegir los rotores, sus anillos y su\n" +
            "posición inicial, y el clavijero para conectar pares de letras.</p>\n" +
            "</body></html>\n";

    public HelpDialog(Window owner) {
        super(owner, "Cómo funciona Enigma", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JEditorPane editorPane = new JEditorPane("text/html", TEXT);
        editorPane.setEditable(false);
        add(new JScrollPane(editorPane), BorderLayout.CENTER);

        setSize(new Dimension(560, 480));
        setLocationRelativeTo(owner);
    }
}
