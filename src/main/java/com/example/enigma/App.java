package com.example.enigma;

import com.example.enigma.controller.EnigmaController;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.Plugboard;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;
import com.example.enigma.ui.MainWindow;

import javax.swing.SwingUtilities;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowWindow);
    }

    private static void createAndShowWindow() {
        EnigmaSettings defaultSettings = new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0));
        EnigmaController controller = new EnigmaController(defaultSettings, new Plugboard());

        MainWindow window = new MainWindow(controller);
        window.setVisible(true);
    }
}
