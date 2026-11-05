package com.example.enigma;

import com.example.enigma.ui.BombeWindow;

import javax.swing.SwingUtilities;

public final class BombeApp {

    private BombeApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BombeWindow window = new BombeWindow();
            // Aplicación independiente: al cerrar su ventana debe terminar el proceso.
            window.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            window.setVisible(true);
        });
    }
}
