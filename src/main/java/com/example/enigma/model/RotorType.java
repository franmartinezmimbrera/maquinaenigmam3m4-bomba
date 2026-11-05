package com.example.enigma.model;

/**
 * Wirings and notches taken from the reference script {@code enigma.py} used
 * in the course, so results match exactly (including the one deliberate
 * point of fidelity to that script rather than to real M4 hardware: the
 * 4th rotor's stepping behaviour).
 */
public enum RotorType {

    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ", "Q"),
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE", "E"),
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO", "V"),
    IV("ESOVPZJAYQUIRHXLNFTGKDCMWB", "J"),
    V("VZBRGITYUPSDNHLXAWMJQOFECK", "Z"),
    VI("JPGVOUMFYQBENHZRDKASXLICTW", "ZM"),
    VII("NZJHGRCXMYSWBOUFAIVLPEKQDT", "ZM"),
    VIII("FKQHTLXOCBJSPDZRAMEWNIUYGV", "ZM"),
    // Beta/Gamma (M4, 4th wheel): enigma.py defines no notch for these (the
    // wiring string carries no ":notch" suffix), so it falls back to "A"
    // with a warning instead of leaving them notch-less. Replicated here.
    BETA("LEYJVCNIXWPBQMDRTAKZGFUHOS", "A"),
    GAMMA("FSOKANUERHMBTIYCWLQPZXVGJD", "A");

    private final String wiring;
    private final String notches;
    private final int[] forward;
    private final int[] backward;

    /** Beta y Gamma solo sirven como 4º rotor de la M4: no pueden ir en las tres posiciones normales. */
    public boolean isFourthRotorOnly() {
        return this == BETA || this == GAMMA;
    }

    RotorType(String wiring, String notches) {
        this.wiring = wiring;
        this.notches = notches;
        this.forward = new int[Alphabet.SIZE];
        this.backward = new int[Alphabet.SIZE];
        for (int i = 0; i < Alphabet.SIZE; i++) {
            int mapped = wiring.charAt(i) - 'A';
            forward[i] = mapped;
            backward[mapped] = i;
        }
    }

    public String wiring() {
        return wiring;
    }

    public String notches() {
        return notches;
    }

    public boolean hasNotchAt(char letter) {
        return notches.indexOf(letter) >= 0;
    }

    public int forward(int index) {
        return forward[index];
    }

    public int backward(int index) {
        return backward[index];
    }
}
