package com.example.enigma.model;

import java.util.ArrayList;
import java.util.List;

public final class EnigmaMachine {

    private final EnigmaSettings settings;
    private final Plugboard plugboard;
    private final Reflector reflector;
    private Rotor left;
    private Rotor middle;
    private Rotor right;
    private Rotor fourth;

    public EnigmaMachine(EnigmaSettings settings, Plugboard plugboard) {
        this.settings = settings;
        this.plugboard = plugboard;
        this.reflector = new Reflector(settings.reflector());
        reset();
    }

    public void reset() {
        left = new Rotor(settings.left().type(), settings.left().ringSetting(), settings.left().initialPosition());
        middle = new Rotor(settings.middle().type(), settings.middle().ringSetting(), settings.middle().initialPosition());
        right = new Rotor(settings.right().type(), settings.right().ringSetting(), settings.right().initialPosition());
        fourth = settings.fourthRotor()
                .map(rs -> new Rotor(rs.type(), rs.ringSetting(), rs.initialPosition()))
                .orElse(null);
    }

    public Rotor leftRotor() {
        return left;
    }

    public Rotor middleRotor() {
        return middle;
    }

    public Rotor rightRotor() {
        return right;
    }

    /** The non-stepping M4 wheel (Beta/Gamma), next to the thin reflector. Empty in M3 mode. */
    public java.util.Optional<Rotor> fourthRotor() {
        return java.util.Optional.ofNullable(fourth);
    }

    public void advanceOnce() {
        stepRotors();
    }

    public int[] currentPermutationWithoutPlugboard() {
        int[] table = new int[Alphabet.SIZE];
        for (int i = 0; i < Alphabet.SIZE; i++) {
            int signal = right.forward(i);
            signal = middle.forward(signal);
            signal = left.forward(signal);
            if (fourth != null) {
                signal = fourth.forward(signal);
            }
            signal = reflector.reflect(signal);
            if (fourth != null) {
                signal = fourth.backward(signal);
            }
            signal = left.backward(signal);
            signal = middle.backward(signal);
            signal = right.backward(signal);
            table[i] = signal;
        }
        return table;
    }

    private void stepRotors() {
        // Mirrors enigma.py's generic cascading-notch stepping exactly
        // (including replicating it as far left as the 4th wheel): real M4
        // hardware never turns Beta/Gamma, but the reference script's
        // generalised N-rotor mechanism does let it advance whenever the
        // "left" rotor hits its own notch, so that is what we reproduce here.
        if (fourth != null && left.isAtNotch()) {
            fourth.advance();
            left.advance();
        }
        boolean middleAtNotch = middle.isAtNotch();
        boolean rightAtNotch = right.isAtNotch();
        if (middleAtNotch) {
            middle.advance();
            left.advance();
        } else if (rightAtNotch) {
            middle.advance();
        }
        right.advance();
    }

    public char pressKey(char letter) {
        return pressKeyWithTrace(letter).lamp();
    }

    public EncryptionTrace pressKeyWithTrace(char letter) {
        if (!Alphabet.isLetter(letter)) {
            throw new IllegalArgumentException("No es una letra A-Z: " + letter);
        }
        stepRotors();

        List<EncryptionStep> steps = new ArrayList<>(11);
        int signal = Alphabet.indexOf(letter);

        signal = recordStep(steps, "Teclado", signal, signal);
        signal = recordStep(steps, "Plugboard", signal, Alphabet.indexOf(plugboard.swap(Alphabet.letterAt(signal))));
        signal = recordStep(steps, "Rotor 3", signal, right.forward(signal));
        signal = recordStep(steps, "Rotor 2", signal, middle.forward(signal));
        signal = recordStep(steps, "Rotor 1", signal, left.forward(signal));
        if (fourth != null) {
            signal = recordStep(steps, "Rotor 4 (Beta/Gamma)", signal, fourth.forward(signal));
        }
        signal = recordStep(steps, "Reflector", signal, reflector.reflect(signal));
        if (fourth != null) {
            signal = recordStep(steps, "Rotor 4 (Beta/Gamma)", signal, fourth.backward(signal));
        }
        signal = recordStep(steps, "Rotor 1", signal, left.backward(signal));
        signal = recordStep(steps, "Rotor 2", signal, middle.backward(signal));
        signal = recordStep(steps, "Rotor 3", signal, right.backward(signal));
        signal = recordStep(steps, "Plugboard", signal, Alphabet.indexOf(plugboard.swap(Alphabet.letterAt(signal))));
        signal = recordStep(steps, "Lámpara", signal, signal);

        return new EncryptionTrace(letter, Alphabet.letterAt(signal), steps);
    }

    private static int recordStep(List<EncryptionStep> steps, String stage, int input, int output) {
        steps.add(new EncryptionStep(stage, Alphabet.letterAt(input), Alphabet.letterAt(output)));
        return output;
    }

    public String process(String text) {
        return processDetailed(text).text();
    }

    public EncryptionResult processDetailed(String text) {
        reset();
        StringBuilder result = new StringBuilder(text.length());
        List<EncryptionTrace> traces = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            if (Alphabet.isLetter(c)) {
                EncryptionTrace trace = pressKeyWithTrace(c);
                traces.add(trace);
                result.append(trace.lamp());
            } else {
                result.append(c);
            }
        }
        return new EncryptionResult(result.toString(), traces);
    }
}
