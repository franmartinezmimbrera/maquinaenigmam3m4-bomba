package com.example.enigma.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Plugboard {

    public static final int MAX_PAIRS = 13;

    private final Map<Character, Character> pairs = new LinkedHashMap<>();

    public void connect(char a, char b) {
        char ua = Character.toUpperCase(a);
        char ub = Character.toUpperCase(b);
        if (!Alphabet.isLetter(ua) || !Alphabet.isLetter(ub)) {
            throw new IllegalArgumentException("Las clavijas deben ser letras A-Z");
        }
        if (ua == ub) {
            throw new IllegalArgumentException("Una letra no puede conectarse consigo misma: " + ua);
        }
        if (pairs.containsKey(ua) || pairs.containsKey(ub)) {
            throw new IllegalArgumentException("La letra ya está conectada: " + (pairs.containsKey(ua) ? ua : ub));
        }
        if (pairs.size() / 2 >= MAX_PAIRS) {
            throw new IllegalStateException("Se ha alcanzado el máximo de " + MAX_PAIRS + " pares");
        }
        pairs.put(ua, ub);
        pairs.put(ub, ua);
    }

    public void disconnect(char letter) {
        char upper = Character.toUpperCase(letter);
        Character partner = pairs.remove(upper);
        if (partner != null) {
            pairs.remove(partner);
        }
    }

    public void clear() {
        pairs.clear();
    }

    public int pairCount() {
        return pairs.size() / 2;
    }

    public Set<Character> connectedLetters() {
        return pairs.keySet();
    }

    public char swap(char letter) {
        char upper = Character.toUpperCase(letter);
        return pairs.getOrDefault(upper, upper);
    }

    public int swap(int index) {
        char letter = Alphabet.letterAt(index);
        return Alphabet.indexOf(swap(letter));
    }

    public List<String> pairs() {
        List<String> result = new ArrayList<>();
        Set<Character> seen = new java.util.HashSet<>();
        for (Map.Entry<Character, Character> entry : pairs.entrySet()) {
            if (seen.add(entry.getKey())) {
                seen.add(entry.getValue());
                result.add("" + entry.getKey() + entry.getValue());
            }
        }
        return result;
    }
}
