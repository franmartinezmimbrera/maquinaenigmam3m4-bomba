package com.example.enigma.model;

import java.util.Objects;

public final class EncryptionStep {

    private final String stage;
    private final char input;
    private final char output;

    public EncryptionStep(String stage, char input, char output) {
        this.stage = stage;
        this.input = input;
        this.output = output;
    }

    public String stage() {
        return stage;
    }

    public char input() {
        return input;
    }

    public char output() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EncryptionStep)) {
            return false;
        }
        EncryptionStep other = (EncryptionStep) o;
        return input == other.input && output == other.output && Objects.equals(stage, other.stage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stage, input, output);
    }

    @Override
    public String toString() {
        return "EncryptionStep[stage=" + stage + ", input=" + input + ", output=" + output + "]";
    }
}
