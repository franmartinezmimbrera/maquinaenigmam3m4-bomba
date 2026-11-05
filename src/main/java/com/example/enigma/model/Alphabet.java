package com.example.enigma.model;

public final class Alphabet {

    public static final int SIZE = 26;

    private Alphabet() {
    }

    public static int indexOf(char letter) {
        if (!isLetter(letter)) {
            throw new IllegalArgumentException("No es una letra A-Z: " + letter);
        }
        return Character.toUpperCase(letter) - 'A';
    }

    public static char letterAt(int index) {
        return (char) ('A' + mod(index));
    }

    public static boolean isLetter(char c) {
        char upper = Character.toUpperCase(c);
        return upper >= 'A' && upper <= 'Z';
    }

    public static int mod(int value) {
        return ((value % SIZE) + SIZE) % SIZE;
    }
}
