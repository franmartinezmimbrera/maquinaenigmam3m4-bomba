package com.example.enigma.bombe;

import com.example.enigma.model.Alphabet;
import com.example.enigma.model.EnigmaMachine;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.Plugboard;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Bombe itself: for every rotor order and every start position it builds
 * the rotor-stack permutation of each crib position (ignoring the plugboard,
 * exactly as the real machine's rotor-and-reflector drums did) and uses the
 * menu's loops to test, for all 26 possible stecker partners of one chosen
 * "test register" letter, whether the hypothesis can be propagated around
 * every loop without contradiction — the software equivalent of the
 * diagonal board, which let the real Bombe test the 26 hypotheses for a
 * rotor position simultaneously instead of one at a time.
 *
 * Ring settings are not searched: historically they were handled separately
 * (the Bombe only varied rotor order and start position), and searching them
 * too would multiply the space by 26^3.
 */
public final class BombeSearch {

    private BombeSearch() {
    }

    public interface ProgressListener {
        void onProgress(long tried, long total);
    }

    public static List<Stop> search(
            Menu menu,
            List<RotorType> availableRotors,
            ReflectorType reflector,
            int[] ringSettings,
            ProgressListener listener) {

        if (!menu.hasLoop()) {
            throw new IllegalArgumentException(
                    "Este menu no tiene ningún bucle: sin un bucle la Bombe no puede "
                            + "descartar hipótesis del clavijero. Prueba con un crib más largo "
                            + "o una alineación distinta.");
        }

        List<List<RotorType>> rotorOrders = permutationsOfThree(availableRotors);
        long total = (long) rotorOrders.size() * 26L * 26L * 26L;
        long tried = 0;

        List<Stop> stops = new ArrayList<>();
        int testLetter = menu.bestTestLetter();
        int[] assignment = new int[Alphabet.SIZE];

        for (List<RotorType> rotorOrder : rotorOrders) {
            for (int leftPos = 0; leftPos < Alphabet.SIZE; leftPos++) {
                for (int middlePos = 0; middlePos < Alphabet.SIZE; middlePos++) {
                    for (int rightPos = 0; rightPos < Alphabet.SIZE; rightPos++) {
                        int[][] forward = new int[menu.length()][];
                        int[][] inverse = new int[menu.length()][];
                        buildPermutationTables(rotorOrder, reflector, ringSettings,
                                new int[] {leftPos, middlePos, rightPos}, menu.length(), forward, inverse);

                        for (int hypothesis = 0; hypothesis < Alphabet.SIZE; hypothesis++) {
                            if (propagate(menu, testLetter, hypothesis, forward, inverse, assignment)) {
                                stops.add(new Stop(
                                        rotorOrder,
                                        ringSettings.clone(),
                                        new int[] {leftPos, middlePos, rightPos},
                                        snapshot(menu, assignment)));
                            }
                        }

                        tried++;
                        if (tried % 2000 == 0) {
                            if (Thread.currentThread().isInterrupted()) {
                                // búsqueda cancelada (p. ej. se cerró la ventana): devolver lo encontrado hasta ahora
                                return stops;
                            }
                            if (listener != null) {
                                listener.onProgress(tried, total);
                            }
                        }
                    }
                }
            }
        }
        if (listener != null) {
            listener.onProgress(total, total);
        }
        return stops;
    }

    private static void buildPermutationTables(
            List<RotorType> rotorOrder,
            ReflectorType reflector,
            int[] ringSettings,
            int[] startPositions,
            int length,
            int[][] forwardOut,
            int[][] inverseOut) {

        EnigmaSettings settings = new EnigmaSettings(
                reflector,
                new RotorSetting(rotorOrder.get(0), ringSettings[0], startPositions[0]),
                new RotorSetting(rotorOrder.get(1), ringSettings[1], startPositions[1]),
                new RotorSetting(rotorOrder.get(2), ringSettings[2], startPositions[2]));
        EnigmaMachine machine = new EnigmaMachine(settings, new Plugboard());

        for (int i = 0; i < length; i++) {
            machine.advanceOnce();
            int[] forward = machine.currentPermutationWithoutPlugboard();
            int[] inverse = new int[Alphabet.SIZE];
            for (int x = 0; x < Alphabet.SIZE; x++) {
                inverse[forward[x]] = x;
            }
            forwardOut[i] = forward;
            inverseOut[i] = inverse;
        }
    }

    private static boolean propagate(
            Menu menu, int startLetter, int hypothesis, int[][] forward, int[][] inverse, int[] assignment) {
        java.util.Arrays.fill(assignment, -1);
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();

        if (!tryAssign(assignment, startLetter, hypothesis)) {
            return false;
        }
        queue.add(startLetter);
        if (hypothesis != startLetter) {
            queue.add(hypothesis);
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (MenuLink link : menu.linksOf(u)) {
                boolean uIsFrom = link.from() == u;
                int other = uIsFrom ? link.to() : link.from();
                int computed = uIsFrom ? forward[link.position()][assignment[u]] : inverse[link.position()][assignment[u]];

                boolean otherWasUnset = assignment[other] == -1;
                boolean partnerWasUnset = assignment[computed] == -1;
                if (!tryAssign(assignment, other, computed)) {
                    return false;
                }
                if (otherWasUnset) {
                    queue.add(other);
                }
                if (partnerWasUnset && computed != other) {
                    queue.add(computed);
                }
            }
        }
        return true;
    }

    private static boolean tryAssign(int[] assignment, int letter, int value) {
        if (assignment[letter] != -1) {
            return assignment[letter] == value;
        }
        if (assignment[value] != -1 && assignment[value] != letter) {
            return false;
        }
        assignment[letter] = value;
        assignment[value] = letter;
        return true;
    }

    private static Map<Integer, Integer> snapshot(Menu menu, int[] assignment) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (int letter : menu.letters()) {
            if (assignment[letter] != -1) {
                result.put(letter, assignment[letter]);
            }
        }
        return result;
    }

    private static List<List<RotorType>> permutationsOfThree(List<RotorType> available) {
        List<List<RotorType>> result = new ArrayList<>();
        for (RotorType first : available) {
            for (RotorType second : available) {
                if (second == first) {
                    continue;
                }
                for (RotorType third : available) {
                    if (third == first || third == second) {
                        continue;
                    }
                    result.add(Collections.unmodifiableList(Arrays.asList(first, second, third)));
                }
            }
        }
        return result;
    }
}
