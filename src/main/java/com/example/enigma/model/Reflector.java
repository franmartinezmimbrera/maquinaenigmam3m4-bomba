package com.example.enigma.model;

public final class Reflector {

    private final ReflectorType type;

    public Reflector(ReflectorType type) {
        this.type = type;
    }

    public ReflectorType type() {
        return type;
    }

    public int reflect(int input) {
        return type.reflect(input);
    }
}
