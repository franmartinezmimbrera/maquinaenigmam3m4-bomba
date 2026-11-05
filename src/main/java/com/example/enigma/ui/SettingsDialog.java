package com.example.enigma.ui;

import com.example.enigma.controller.EnigmaController;
import com.example.enigma.model.Alphabet;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;

public final class SettingsDialog extends JDialog {

    private static final int ROTOR_COUNT = 3;
    private static final RotorType[] NORMAL_ROTOR_TYPES =
            {RotorType.I, RotorType.II, RotorType.III, RotorType.IV, RotorType.V,
                    RotorType.VI, RotorType.VII, RotorType.VIII};
    private static final RotorType[] FOURTH_ROTOR_TYPES = {RotorType.BETA, RotorType.GAMMA};
    private static final ReflectorType[] NORMAL_REFLECTORS = {ReflectorType.UKW_B, ReflectorType.UKW_C};
    private static final ReflectorType[] THIN_REFLECTORS =
            {ReflectorType.UKW_B_THIN, ReflectorType.UKW_C_THIN};

    private final EnigmaController controller;
    private final JComboBox<RotorType>[] rotorCombos;
    private final JComboBox<Character>[] ringCombos;
    private final JComboBox<Character>[] positionCombos;
    private final JComboBox<ReflectorType> reflectorCombo;
    private final JCheckBox m4Mode;
    private final JComboBox<RotorType> fourthRotorCombo = new JComboBox<>(FOURTH_ROTOR_TYPES);
    private final JComboBox<Character> fourthRingCombo = new JComboBox<>(letters());
    private final JComboBox<Character> fourthPositionCombo = new JComboBox<>(letters());

    @SuppressWarnings("unchecked")
    public SettingsDialog(Window owner, EnigmaController controller) {
        super(owner, "Ajustes de Enigma M3-M4", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.rotorCombos = new JComboBox[ROTOR_COUNT];
        this.ringCombos = new JComboBox[ROTOR_COUNT];
        this.positionCombos = new JComboBox[ROTOR_COUNT];

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Reflector:"));
        reflectorCombo = new JComboBox<>(NORMAL_REFLECTORS);
        topPanel.add(reflectorCombo);
        m4Mode = new JCheckBox("Modo M4 (4º rotor Beta/Gamma)");
        m4Mode.addActionListener(e -> toggleM4Mode());
        topPanel.add(m4Mode);
        add(topPanel, BorderLayout.NORTH);

        add(buildGrid(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadFrom(controller.settings());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(4, ROTOR_COUNT + 2, 8, 8));

        grid.add(new JLabel(""));
        grid.add(new JLabel("1er rotor", SwingConstants.CENTER));
        grid.add(new JLabel("2º rotor", SwingConstants.CENTER));
        grid.add(new JLabel("3er rotor", SwingConstants.CENTER));
        grid.add(new JLabel("4º rotor (M4)", SwingConstants.CENTER));

        grid.add(new JLabel("Rotor"));
        for (int i = 0; i < ROTOR_COUNT; i++) {
            rotorCombos[i] = new JComboBox<>(NORMAL_ROTOR_TYPES);
            grid.add(rotorCombos[i]);
        }
        grid.add(fourthRotorCombo);

        grid.add(new JLabel("Ajuste de anillo"));
        for (int i = 0; i < ROTOR_COUNT; i++) {
            ringCombos[i] = new JComboBox<>(letters());
            grid.add(ringCombos[i]);
        }
        grid.add(fourthRingCombo);

        grid.add(new JLabel("Posición inicial"));
        for (int i = 0; i < ROTOR_COUNT; i++) {
            positionCombos[i] = new JComboBox<>(letters());
            grid.add(positionCombos[i]);
        }
        grid.add(fourthPositionCombo);

        return grid;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel();
        JButton cancelar = new JButton("Cancelar");
        JButton aplicar = new JButton("Aplicar ajustes");
        buttons.add(cancelar);
        buttons.add(aplicar);

        cancelar.addActionListener(e -> dispose());
        aplicar.addActionListener(e -> applySettings());
        return buttons;
    }

    private void toggleM4Mode() {
        boolean active = m4Mode.isSelected();
        fourthRotorCombo.setEnabled(active);
        fourthRingCombo.setEnabled(active);
        fourthPositionCombo.setEnabled(active);

        ReflectorType previousSelection = (ReflectorType) reflectorCombo.getSelectedItem();
        reflectorCombo.setModel(new javax.swing.DefaultComboBoxModel<>(active ? THIN_REFLECTORS : NORMAL_REFLECTORS));
        boolean sameKind = previousSelection != null && previousSelection.isThin() == active;
        reflectorCombo.setSelectedIndex(sameKind ? indexOf(active ? THIN_REFLECTORS : NORMAL_REFLECTORS,
                previousSelection) : 0);
    }

    private static int indexOf(ReflectorType[] values, ReflectorType value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return 0;
    }

    private static Character[] letters() {
        Character[] result = new Character[Alphabet.SIZE];
        for (int i = 0; i < Alphabet.SIZE; i++) {
            result[i] = Alphabet.letterAt(i);
        }
        return result;
    }

    private void loadFrom(EnigmaSettings settings) {
        RotorSetting[] rotors = {settings.left(), settings.middle(), settings.right()};
        for (int i = 0; i < ROTOR_COUNT; i++) {
            rotorCombos[i].setSelectedItem(rotors[i].type());
            ringCombos[i].setSelectedItem(Alphabet.letterAt(rotors[i].ringSetting()));
            positionCombos[i].setSelectedItem(Alphabet.letterAt(rotors[i].initialPosition()));
        }

        boolean hasFourth = settings.fourthRotor().isPresent();
        m4Mode.setSelected(hasFourth);
        reflectorCombo.setModel(new javax.swing.DefaultComboBoxModel<>(hasFourth ? THIN_REFLECTORS : NORMAL_REFLECTORS));
        reflectorCombo.setSelectedItem(settings.reflector());
        fourthRotorCombo.setEnabled(hasFourth);
        fourthRingCombo.setEnabled(hasFourth);
        fourthPositionCombo.setEnabled(hasFourth);
        settings.fourthRotor().ifPresent(fourth -> {
            fourthRotorCombo.setSelectedItem(fourth.type());
            fourthRingCombo.setSelectedItem(Alphabet.letterAt(fourth.ringSetting()));
            fourthPositionCombo.setSelectedItem(Alphabet.letterAt(fourth.initialPosition()));
        });
    }

    private void applySettings() {
        try {
            RotorSetting left = readRotorSetting(rotorCombos[0], ringCombos[0], positionCombos[0]);
            RotorSetting middle = readRotorSetting(rotorCombos[1], ringCombos[1], positionCombos[1]);
            RotorSetting right = readRotorSetting(rotorCombos[2], ringCombos[2], positionCombos[2]);
            ReflectorType reflector = (ReflectorType) reflectorCombo.getSelectedItem();

            EnigmaSettings newSettings = m4Mode.isSelected()
                    ? new EnigmaSettings(reflector, left, middle, right,
                            readRotorSetting(fourthRotorCombo, fourthRingCombo, fourthPositionCombo))
                    : new EnigmaSettings(reflector, left, middle, right);

            controller.applySettings(newSettings);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ajustes no válidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RotorSetting readRotorSetting(
            JComboBox<RotorType> rotorCombo, JComboBox<Character> ringCombo, JComboBox<Character> positionCombo) {
        RotorType type = (RotorType) rotorCombo.getSelectedItem();
        char ring = (Character) ringCombo.getSelectedItem();
        char position = (Character) positionCombo.getSelectedItem();
        return new RotorSetting(type, Alphabet.indexOf(ring), Alphabet.indexOf(position));
    }
}
