package com.example.enigma.bombe;

import java.util.Objects;

/**
 * One wire of the "menu": at crib position {@code position} the keyboard letter
 * {@code from} and the keyboard letter {@code to} are connected by the
 * rotor-stack permutation of that position (reflector included, plugboard not).
 */
public final class MenuLink {

    private final int from;
    private final int to;
    private final int position;

    public MenuLink(int from, int to, int position) {
        this.from = from;
        this.to = to;
        this.position = position;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int position() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MenuLink)) {
            return false;
        }
        MenuLink other = (MenuLink) o;
        return from == other.from && to == other.to && position == other.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, position);
    }

    @Override
    public String toString() {
        return "MenuLink[from=" + from + ", to=" + to + ", position=" + position + "]";
    }
}
