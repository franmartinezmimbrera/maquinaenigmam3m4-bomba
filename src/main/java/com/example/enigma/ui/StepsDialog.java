package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;

import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

public final class StepsDialog extends JDialog {

    public StepsDialog(Window owner, EnigmaController controller) {
        super(owner, "Pasos de cifrado", ModalityType.MODELESS);
        setLayout(new BorderLayout());

        StepsPanel stepsPanel = new StepsPanel();
        stepsPanel.showTraces(controller.lastTraces());
        add(stepsPanel, BorderLayout.CENTER);

        setSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
    }
}
