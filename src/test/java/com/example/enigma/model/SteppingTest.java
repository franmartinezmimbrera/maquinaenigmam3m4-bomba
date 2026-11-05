package com.example.enigma.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SteppingTest {

    private static EnigmaMachine machineAt(String positions) {
        EnigmaSettings settings = new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, 0, Alphabet.indexOf(positions.charAt(0))),
                new RotorSetting(RotorType.II, 0, Alphabet.indexOf(positions.charAt(1))),
                new RotorSetting(RotorType.III, 0, Alphabet.indexOf(positions.charAt(2))));
        return new EnigmaMachine(settings, new Plugboard());
    }

    private static String positionsOf(EnigmaMachine machine) {
        return "" + Alphabet.letterAt(machine.leftRotor().position())
                + Alphabet.letterAt(machine.middleRotor().position())
                + Alphabet.letterAt(machine.rightRotor().position());
    }

    @Test
    void doublePepStepping() {
        EnigmaMachine machine = machineAt("ADU");

        machine.pressKey('A');
        assertEquals("ADV", positionsOf(machine));

        machine.pressKey('A');
        assertEquals("AEW", positionsOf(machine));

        machine.pressKey('A');
        assertEquals("BFX", positionsOf(machine));
    }

    @Test
    void singleStepFromAAA() {
        EnigmaMachine machine = machineAt("AAA");
        machine.pressKey('A');
        assertEquals("AAB", positionsOf(machine));
    }

    @Test
    void singleStepWhenRightRotorAtNotch() {
        EnigmaMachine machine = machineAt("AAV");
        machine.pressKey('A');
        assertEquals("ABW", positionsOf(machine));
    }
}
