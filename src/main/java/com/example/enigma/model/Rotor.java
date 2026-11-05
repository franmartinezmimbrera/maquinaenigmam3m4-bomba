package com.example.enigma.model;

public final class Rotor {

    private final RotorType type;
    private final int ringSetting;
    private int position;

    public Rotor(RotorType type, int ringSetting, int initialPosition) {
        this.type = type;
        this.ringSetting = Alphabet.mod(ringSetting);
        this.position = Alphabet.mod(initialPosition);
    }

    public RotorType type() {
        return type;
    }

    public int ringSetting() {
        return ringSetting;
    }

    public int position() {
        return position;
    }

    public void setPosition(int position) {
        this.position = Alphabet.mod(position);
    }

    public void advance() {
        position = Alphabet.mod(position + 1);
    }

    public boolean isAtNotch() {
        return type.hasNotchAt(Alphabet.letterAt(position));
    }

    public int forward(int input) {
        int shift = position - ringSetting;
        int entered = Alphabet.mod(input + shift);
        int mapped = type.forward(entered);
        return Alphabet.mod(mapped - shift);
    }

    public int backward(int input) {
        int shift = position - ringSetting;
        int entered = Alphabet.mod(input + shift);
        int mapped = type.backward(entered);
        return Alphabet.mod(mapped - shift);
    }
}
