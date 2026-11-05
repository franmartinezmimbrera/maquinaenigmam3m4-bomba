package com.example.enigma.bombe;

import com.example.enigma.model.Alphabet;
import com.example.enigma.model.RotorType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A "stop": a rotor order + ring settings + start position for which at least
 * one hypothesis of the test letter's stecker partner propagated through the
 * whole menu without contradiction. Historically most stops are false and must
 * be checked by hand (or discarded by running a second menu) — this is no
 * different here.
 */
public final class Stop {

    private final List<RotorType> rotorOrder;
    private final int[] ringSettings;
    private final int[] startPositions;
    private final Map<Integer, Integer> stecker;

    public Stop(List<RotorType> rotorOrder, int[] ringSettings, int[] startPositions, Map<Integer, Integer> stecker) {
        this.rotorOrder = rotorOrder;
        this.ringSettings = ringSettings;
        this.startPositions = startPositions;
        this.stecker = stecker;
    }

    public List<RotorType> rotorOrder() {
        return rotorOrder;
    }

    public int[] ringSettings() {
        return ringSettings;
    }

    public int[] startPositions() {
        return startPositions;
    }

    public Map<Integer, Integer> stecker() {
        return stecker;
    }

    public String ringSettingsAsText() {
        return lettersOf(ringSettings);
    }

    public String startPositionsAsText() {
        return lettersOf(startPositions);
    }

    public Map<Character, Character> plugboardPairs() {
        Map<Character, Character> pairs = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : stecker.entrySet()) {
            char a = Alphabet.letterAt(entry.getKey());
            char b = Alphabet.letterAt(entry.getValue());
            if (a != b && !pairs.containsKey(b)) {
                pairs.put(a, b);
            }
        }
        return pairs;
    }

    private static String lettersOf(int[] indices) {
        StringBuilder sb = new StringBuilder(indices.length);
        for (int index : indices) {
            sb.append(Alphabet.letterAt(index));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stop)) {
            return false;
        }
        Stop other = (Stop) o;
        // Igual que el record original: los arrays se comparan por referencia.
        return Objects.equals(rotorOrder, other.rotorOrder) && ringSettings == other.ringSettings
                && startPositions == other.startPositions && Objects.equals(stecker, other.stecker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotorOrder, System.identityHashCode(ringSettings),
                System.identityHashCode(startPositions), stecker);
    }

    @Override
    public String toString() {
        return "Stop[rotorOrder=" + rotorOrder + ", ringSettings=" + java.util.Arrays.toString(ringSettings)
                + ", startPositions=" + java.util.Arrays.toString(startPositions) + ", stecker=" + stecker + "]";
    }
}
