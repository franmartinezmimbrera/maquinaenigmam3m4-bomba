package com.example.enigma.bombe;

import com.example.enigma.model.Alphabet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The "menu": the graph built by aligning a crib (known/guessed plaintext)
 * against a stretch of ciphertext of the same length, one link per letter
 * position. A menu with at least one closed loop is what lets the Bombe test
 * a rotor-position hypothesis without knowing the plugboard.
 */
public final class Menu {

    private final List<MenuLink> links;
    private final Map<Integer, List<MenuLink>> adjacency;
    private final int loopCount;

    private Menu(List<MenuLink> links) {
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
        this.adjacency = buildAdjacency(this.links);
        this.loopCount = countLoops(this.links);
    }

    public static Menu build(String crib, String cipherSegment) {
        String normalizedCrib = crib.toUpperCase();
        String normalizedCipher = cipherSegment.toUpperCase();
        if (normalizedCrib.length() != normalizedCipher.length()) {
            throw new IllegalArgumentException(
                    "El crib y el segmento de cifrado deben tener la misma longitud");
        }
        if (normalizedCrib.isEmpty()) {
            throw new IllegalArgumentException("El crib no puede estar vacío");
        }

        List<MenuLink> links = new ArrayList<>(normalizedCrib.length());
        for (int i = 0; i < normalizedCrib.length(); i++) {
            char plain = normalizedCrib.charAt(i);
            char cipher = normalizedCipher.charAt(i);
            if (!Alphabet.isLetter(plain) || !Alphabet.isLetter(cipher)) {
                throw new IllegalArgumentException("Crib y cifrado solo pueden contener letras A-Z");
            }
            if (plain == cipher) {
                throw new IllegalArgumentException(
                        "Alineación inválida en la posición " + i + ": Enigma nunca cifra "
                                + plain + " como ella misma (" + plain + "->" + cipher + ")");
            }
            links.add(new MenuLink(Alphabet.indexOf(plain), Alphabet.indexOf(cipher), i));
        }
        return new Menu(links);
    }

    /**
     * Crib-dragging: every offset within {@code cipherText} where the crib could
     * possibly be aligned, i.e. no position maps a letter to itself.
     */
    public static List<Integer> findValidOffsets(String cipherText, String crib) {
        String normalizedCipher = cipherText.toUpperCase();
        String normalizedCrib = crib.toUpperCase();
        List<Integer> offsets = new ArrayList<>();
        for (int offset = 0; offset + normalizedCrib.length() <= normalizedCipher.length(); offset++) {
            if (isValidOffset(normalizedCipher, normalizedCrib, offset)) {
                offsets.add(offset);
            }
        }
        return offsets;
    }

    private static boolean isValidOffset(String cipherText, String crib, int offset) {
        for (int i = 0; i < crib.length(); i++) {
            if (crib.charAt(i) == cipherText.charAt(offset + i)) {
                return false;
            }
        }
        return true;
    }

    public List<MenuLink> links() {
        return links;
    }

    public int length() {
        return links.size();
    }

    public Set<Integer> letters() {
        return adjacency.keySet();
    }

    public List<MenuLink> linksOf(int letter) {
        return adjacency.getOrDefault(letter, Collections.<MenuLink>emptyList());
    }

    public boolean hasLoop() {
        return loopCount > 0;
    }

    /**
     * The number of independent loops in the menu (its cyclomatic number:
     * edges - nodes + connected components). Each loop beyond the first
     * roughly divides the false-stop rate by another factor of 26, so a
     * menu with only one loop is usually far too weak to be useful — it is
     * what makes the real difference between a crib that is "good enough"
     * for a Bombe run and one that just wastes a run.
     */
    public int loopCount() {
        return loopCount;
    }

    /** The letter touched by the most links: the classic choice of "test register" letter. */
    public int bestTestLetter() {
        return letters().stream()
                .max((a, b) -> Integer.compare(linksOf(a).size(), linksOf(b).size()))
                .orElseThrow(() -> new IllegalStateException("El menu no tiene letras"));
    }

    private static Map<Integer, List<MenuLink>> buildAdjacency(List<MenuLink> links) {
        Map<Integer, List<MenuLink>> map = new LinkedHashMap<>();
        for (MenuLink link : links) {
            map.computeIfAbsent(link.from(), k -> new ArrayList<>()).add(link);
            map.computeIfAbsent(link.to(), k -> new ArrayList<>()).add(link);
        }
        return map;
    }

    private static int countLoops(List<MenuLink> links) {
        Map<Integer, Integer> parent = new LinkedHashMap<>();
        int loops = 0;
        for (MenuLink link : links) {
            int rootFrom = find(parent, link.from());
            int rootTo = find(parent, link.to());
            if (rootFrom == rootTo) {
                loops++;
            } else {
                parent.put(rootFrom, rootTo);
            }
        }
        return loops;
    }

    private static int find(Map<Integer, Integer> parent, int node) {
        int current = node;
        while (parent.getOrDefault(current, current) != current) {
            current = parent.get(current);
        }
        parent.put(node, current);
        return current;
    }

    public Set<Integer> connectedLettersOf(int startLetter) {
        Set<Integer> visited = new LinkedHashSet<>();
        visited.add(startLetter);
        List<Integer> queue = new ArrayList<>(Collections.singletonList(startLetter));
        while (!queue.isEmpty()) {
            int current = queue.remove(queue.size() - 1);
            for (MenuLink link : linksOf(current)) {
                int other = link.from() == current ? link.to() : link.from();
                if (visited.add(other)) {
                    queue.add(other);
                }
            }
        }
        return visited;
    }
}
