package com.example.enigma.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ground-truth vectors for these were produced by actually running the
 * course's reference script {@code enigma.py} (not hand-derived), so these
 * tests check byte-for-byte agreement with it, quirks included: the 4th
 * rotor can advance here even though it never does on real M4 hardware.
 */
class EnigmaMachineM4Test {

    @Test
    void fourthRotorAtNeutralPositionIsEquivalentToM3() {
        // python: maquinaEnigma("AAAA", "ReflBT", ["Beta","I","II","III"], [], "AAAA")
        // -> encryptString("AAAAA") == "BDZGO", same as the plain 3-rotor machine:
        // this is the historical reason M4 traffic with Beta/Gamma left at 'A'
        // could still be read by M3 machines (thin reflector + wheel at 'A'
        // collapses back to the non-thin reflector).
        EnigmaSettings settings = new EnigmaSettings(
                ReflectorType.UKW_B_THIN,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0),
                new RotorSetting(RotorType.BETA, 0, 0));
        EnigmaMachine machine = new EnigmaMachine(settings, new Plugboard());

        assertEquals("BDZGO", machine.process("AAAAA"));
    }

    @Test
    void fourthWheelAdvancesWhenLeftRotorIsAtItsOwnNotch() {
        // python: maquinaEnigma("AQAA", "ReflBT", ["Beta","I","II","III"], [], "AAAA")
        // -> encryptString("AAAAA") == "KEKEB"
        EnigmaSettings settings = new EnigmaSettings(
                ReflectorType.UKW_B_THIN,
                new RotorSetting(RotorType.I, 0, Alphabet.indexOf('Q')),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0),
                new RotorSetting(RotorType.BETA, 0, 0));
        EnigmaMachine machine = new EnigmaMachine(settings, new Plugboard());

        assertEquals("KEKEB", machine.process("AAAAA"));
    }

    @Test
    void realisticFourRotorConfigurationMatchesReference() {
        // python: maquinaEnigma("CQDJ", "ReflCT", ["Gamma","IV","VI","VIII"], [], "BXQA")
        // -> encryptString("HELLOWORLDTEST") == "JKVHMSWODHYMDW"
        EnigmaSettings settings = new EnigmaSettings(
                ReflectorType.UKW_C_THIN,
                new RotorSetting(RotorType.IV, Alphabet.indexOf('X'), Alphabet.indexOf('Q')),
                new RotorSetting(RotorType.VI, Alphabet.indexOf('Q'), Alphabet.indexOf('D')),
                new RotorSetting(RotorType.VIII, Alphabet.indexOf('A'), Alphabet.indexOf('J')),
                new RotorSetting(RotorType.GAMMA, Alphabet.indexOf('B'), Alphabet.indexOf('C')));
        EnigmaMachine machine = new EnigmaMachine(settings, new Plugboard());

        assertEquals("JKVHMSWODHYMDW", machine.process("HELLOWORLDTEST"));
    }

    @Test
    void fourthRotorMustBeBetaOrGamma() {
        assertThrows(IllegalArgumentException.class, () -> new EnigmaSettings(
                ReflectorType.UKW_B_THIN,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0),
                new RotorSetting(RotorType.IV, 0, 0)));
    }

    @Test
    void thinReflectorRequiresAFourthRotor() {
        assertThrows(IllegalArgumentException.class, () -> new EnigmaSettings(
                ReflectorType.UKW_B_THIN,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0)));
    }

    @Test
    void fourthRotorRequiresAThinReflector() {
        assertThrows(IllegalArgumentException.class, () -> new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0),
                new RotorSetting(RotorType.BETA, 0, 0)));
    }

    @Test
    void betaAndGammaCannotBeUsedAsAnOrdinaryRotor() {
        assertThrows(IllegalArgumentException.class, () -> new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.BETA, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0)));
    }
}
