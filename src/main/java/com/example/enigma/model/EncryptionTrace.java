package com.example.enigma.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EncryptionTrace {

    private final char keyPressed;
    private final char lamp;
    private final List<EncryptionStep> steps;

    public EncryptionTrace(char keyPressed, char lamp, List<EncryptionStep> steps) {
        this.keyPressed = keyPressed;
        this.lamp = lamp;
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public char keyPressed() {
        return keyPressed;
    }

    public char lamp() {
        return lamp;
    }

    public List<EncryptionStep> steps() {
        return steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EncryptionTrace)) {
            return false;
        }
        EncryptionTrace other = (EncryptionTrace) o;
        return keyPressed == other.keyPressed && lamp == other.lamp && steps.equals(other.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyPressed, lamp, steps);
    }

    @Override
    public String toString() {
        return "EncryptionTrace[keyPressed=" + keyPressed + ", lamp=" + lamp + ", steps=" + steps + "]";
    }
}
