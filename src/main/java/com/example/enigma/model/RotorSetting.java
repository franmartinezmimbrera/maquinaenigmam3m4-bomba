package com.example.enigma.model;

import java.util.Objects;

public final class RotorSetting {

    private final RotorType type;
    private final int ringSetting;
    private final int initialPosition;

    public RotorSetting(RotorType type, int ringSetting, int initialPosition) {
        this.type = type;
        this.ringSetting = Alphabet.mod(ringSetting);
        this.initialPosition = Alphabet.mod(initialPosition);
    }

    public RotorType type() {
        return type;
    }

    public int ringSetting() {
        return ringSetting;
    }

    public int initialPosition() {
        return initialPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RotorSetting)) {
            return false;
        }
        RotorSetting other = (RotorSetting) o;
        return type == other.type && ringSetting == other.ringSetting && initialPosition == other.initialPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, ringSetting, initialPosition);
    }

    @Override
    public String toString() {
        return "RotorSetting[type=" + type + ", ringSetting=" + ringSetting
                + ", initialPosition=" + initialPosition + "]";
    }
}
