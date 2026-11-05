package com.example.enigma.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RotorTest {

    @Test
    void notchDetectedAtCorrectLetter() {
        Rotor rotor = new Rotor(RotorType.I, 0, Alphabet.indexOf('Q'));
        assertTrue(rotor.isAtNotch());

        rotor.advance();
        assertFalse(rotor.isAtNotch());
    }

    @Test
    void forwardThenBackwardIsIdentityWithNoOffset() {
        Rotor rotor = new Rotor(RotorType.I, 0, 0);
        for (int i = 0; i < Alphabet.SIZE; i++) {
            assertEquals(i, rotor.backward(rotor.forward(i)));
        }
    }

    @Test
    void forwardThenBackwardIsIdentityWithRingAndPosition() {
        Rotor rotor = new Rotor(RotorType.III, Alphabet.indexOf('G'), Alphabet.indexOf('M'));
        for (int i = 0; i < Alphabet.SIZE; i++) {
            assertEquals(i, rotor.backward(rotor.forward(i)));
        }
    }

    @Test
    void advanceWrapsAroundAlphabet() {
        Rotor rotor = new Rotor(RotorType.I, 0, Alphabet.indexOf('Z'));
        rotor.advance();
        assertEquals(0, rotor.position());
    }

    @Test
    void onlyBetaAndGammaAreFourthRotorOnly() {
        for (RotorType type : RotorType.values()) {
            assertEquals(type == RotorType.BETA || type == RotorType.GAMMA, type.isFourthRotorOnly(), type.name());
        }
    }
}
