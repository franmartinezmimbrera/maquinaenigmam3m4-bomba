package com.example.enigma.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

public final class EnigmaSettings {

    private final ReflectorType reflector;
    private final RotorSetting left;
    private final RotorSetting middle;
    private final RotorSetting right;
    private final Optional<RotorSetting> fourthRotor;

    public EnigmaSettings(
            ReflectorType reflector,
            RotorSetting left,
            RotorSetting middle,
            RotorSetting right,
            Optional<RotorSetting> fourthRotor) {
        EnumSet<RotorType> distinct = EnumSet.of(left.type(), middle.type(), right.type());
        if (distinct.size() != 3) {
            throw new IllegalArgumentException("Los tres rotores deben ser distintos");
        }
        for (RotorType type : new RotorType[] {left.type(), middle.type(), right.type()}) {
            if (type == RotorType.BETA || type == RotorType.GAMMA) {
                throw new IllegalArgumentException(
                        "Beta/Gamma solo pueden ir en la 4ª posición (M4), no como rotor normal");
            }
        }
        if (fourthRotor.isPresent()) {
            RotorType type = fourthRotor.get().type();
            if (type != RotorType.BETA && type != RotorType.GAMMA) {
                throw new IllegalArgumentException("El cuarto rotor (M4) debe ser Beta o Gamma");
            }
            if (!reflector.isThin()) {
                throw new IllegalArgumentException(
                        "Con cuarto rotor hace falta un reflector fino (UKW_B_THIN / UKW_C_THIN)");
            }
        } else if (reflector.isThin()) {
            throw new IllegalArgumentException(
                    "Un reflector fino (M4) requiere un cuarto rotor (Beta/Gamma)");
        }
        this.reflector = reflector;
        this.left = left;
        this.middle = middle;
        this.right = right;
        this.fourthRotor = fourthRotor;
    }

    /** M3 mode: three rotors, standard (non-thin) reflector. */
    public EnigmaSettings(ReflectorType reflector, RotorSetting left, RotorSetting middle, RotorSetting right) {
        this(reflector, left, middle, right, Optional.empty());
    }

    /** M4 mode: three normal rotors plus a non-stepping 4th rotor (Beta/Gamma) next to a thin reflector. */
    public EnigmaSettings(
            ReflectorType reflector, RotorSetting left, RotorSetting middle, RotorSetting right,
            RotorSetting fourthRotor) {
        this(reflector, left, middle, right, Optional.of(fourthRotor));
    }

    public ReflectorType reflector() {
        return reflector;
    }

    public RotorSetting left() {
        return left;
    }

    public RotorSetting middle() {
        return middle;
    }

    public RotorSetting right() {
        return right;
    }

    public Optional<RotorSetting> fourthRotor() {
        return fourthRotor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnigmaSettings)) {
            return false;
        }
        EnigmaSettings other = (EnigmaSettings) o;
        return reflector == other.reflector && left.equals(other.left) && middle.equals(other.middle)
                && right.equals(other.right) && fourthRotor.equals(other.fourthRotor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reflector, left, middle, right, fourthRotor);
    }

    @Override
    public String toString() {
        return "EnigmaSettings[reflector=" + reflector + ", left=" + left + ", middle=" + middle
                + ", right=" + right + ", fourthRotor=" + fourthRotor + "]";
    }
}
