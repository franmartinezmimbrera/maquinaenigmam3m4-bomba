package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;
import com.example.enigma.model.Alphabet;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

final class ConfigurationIO {

    private ConfigurationIO() {
    }

    static void save(Component parent, EnigmaController controller) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar configuración");
        chooser.setFileFilter(new FileNameExtensionFilter("Configuración Enigma (*.properties)", "properties"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".properties")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".properties");
        }

        Properties properties = new Properties();
        EnigmaSettings settings = controller.settings();
        properties.setProperty("reflector", settings.reflector().name());
        putRotor(properties, "rotor1", settings.left());
        putRotor(properties, "rotor2", settings.middle());
        putRotor(properties, "rotor3", settings.right());
        settings.fourthRotor().ifPresent(fourth -> putRotor(properties, "rotor4", fourth));
        properties.setProperty("plugboard.pairs", String.join(",", controller.plugboard().pairs()));

        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Configuración Enigma M3-M4");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo guardar: " + ex.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void load(Component parent, EnigmaController controller) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Cargar configuración");
        chooser.setFileFilter(new FileNameExtensionFilter("Configuración Enigma (*.properties)", "properties"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(chooser.getSelectedFile())) {
            properties.load(in);

            ReflectorType reflector = ReflectorType.valueOf(properties.getProperty("reflector"));
            RotorSetting left = readRotor(properties, "rotor1");
            RotorSetting middle = readRotor(properties, "rotor2");
            RotorSetting right = readRotor(properties, "rotor3");
            EnigmaSettings settings = properties.containsKey("rotor4.type")
                    ? new EnigmaSettings(reflector, left, middle, right, readRotor(properties, "rotor4"))
                    : new EnigmaSettings(reflector, left, middle, right);

            controller.applySettings(settings);
            controller.clearPlugboard();
            String pairs = properties.getProperty("plugboard.pairs", "");
            if (!pairs.trim().isEmpty()) {
                for (String pair : pairs.split(",")) {
                    if (pair.length() == 2) {
                        controller.connectPlug(pair.charAt(0), pair.charAt(1));
                    }
                }
            }
        } catch (IOException | IllegalArgumentException | NullPointerException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo cargar la configuración: " + ex.getMessage(),
                    "Error al cargar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void putRotor(Properties properties, String prefix, RotorSetting rotor) {
        properties.setProperty(prefix + ".type", rotor.type().name());
        properties.setProperty(prefix + ".ring", String.valueOf(Alphabet.letterAt(rotor.ringSetting())));
        properties.setProperty(prefix + ".position", String.valueOf(Alphabet.letterAt(rotor.initialPosition())));
    }

    private static RotorSetting readRotor(Properties properties, String prefix) {
        RotorType type = RotorType.valueOf(properties.getProperty(prefix + ".type"));
        char ring = properties.getProperty(prefix + ".ring").charAt(0);
        char position = properties.getProperty(prefix + ".position").charAt(0);
        return new RotorSetting(type, Alphabet.indexOf(ring), Alphabet.indexOf(position));
    }
}
