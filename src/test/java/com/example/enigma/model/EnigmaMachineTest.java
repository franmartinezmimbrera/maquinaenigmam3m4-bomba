package com.example.enigma.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnigmaMachineTest {

    private static EnigmaSettings defaultSettings() {
        return new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.II, 0, 0),
                new RotorSetting(RotorType.III, 0, 0));
    }

    @Test
    void referenceVectorAaaaaEncodesToBdzgo() {
        EnigmaMachine machine = new EnigmaMachine(defaultSettings(), new Plugboard());
        assertEquals("BDZGO", machine.process("AAAAA"));
    }

    @Test
    void currentPermutationWithoutPlugboardMatchesPressKeyWhenPlugboardEmpty() {
        EnigmaMachine machine = new EnigmaMachine(defaultSettings(), new Plugboard());
        machine.advanceOnce();
        machine.advanceOnce();
        machine.advanceOnce();

        int[] permutation = machine.currentPermutationWithoutPlugboard();

        // currentPermutationWithoutPlugboard() must be a pure read, not advance further:
        // a fresh machine stepped twice and then pressed once reaches the very same
        // "3 steps" state that pressKey uses to encrypt, so the two must agree.
        for (int i = 0; i < Alphabet.SIZE; i++) {
            char letter = Alphabet.letterAt(i);
            EnigmaMachine reference = new EnigmaMachine(defaultSettings(), new Plugboard());
            reference.advanceOnce();
            reference.advanceOnce();
            char output = reference.pressKey(letter);
            assertEquals(Alphabet.letterAt(permutation[i]), output);
        }

        // and reading it twice in a row must give the exact same table (no hidden stepping)
        assertEquals(permutation.length, machine.currentPermutationWithoutPlugboard().length);
        for (int i = 0; i < Alphabet.SIZE; i++) {
            assertEquals(permutation[i], machine.currentPermutationWithoutPlugboard()[i]);
        }
    }

    @Test
    void traceHasElevenStagesAndMatchesEncodedLetter() {
        EnigmaMachine machine = new EnigmaMachine(defaultSettings(), new Plugboard());

        EncryptionTrace trace = machine.pressKeyWithTrace('A');

        assertEquals('A', trace.keyPressed());
        assertEquals('B', trace.lamp());
        assertEquals(11, trace.steps().size());
        assertEquals('A', trace.steps().get(0).input());
        assertEquals(trace.lamp(), trace.steps().get(trace.steps().size() - 1).output());

        for (int i = 1; i < trace.steps().size(); i++) {
            assertEquals(trace.steps().get(i - 1).output(), trace.steps().get(i).input());
        }
    }

    @Test
    void traceAndPressKeyAgreeOnResult() {
        EnigmaMachine viaTrace = new EnigmaMachine(defaultSettings(), new Plugboard());
        EnigmaMachine viaPressKey = new EnigmaMachine(defaultSettings(), new Plugboard());

        for (char letter : "AAAAA".toCharArray()) {
            char expected = viaPressKey.pressKey(letter);
            char fromTrace = viaTrace.pressKeyWithTrace(letter).lamp();
            assertEquals(expected, fromTrace);
        }
    }

    @Test
    void encryptingTwiceWithSamePositionReturnsOriginalText() {
        Random random = new Random(42);
        for (int attempt = 0; attempt < 20; attempt++) {
            EnigmaSettings settings = randomSettings(random);
            Plugboard plugboard = randomPlugboard(random);
            EnigmaMachine machine = new EnigmaMachine(settings, plugboard);

            String plainText = randomText(random, 30);
            String cipherText = machine.process(plainText);
            String decoded = machine.process(cipherText);

            assertEquals(plainText, decoded);
        }
    }

    @Test
    void noLetterEverEncodesToItself() {
        Random random = new Random(7);
        EnigmaMachine machine = new EnigmaMachine(randomSettings(random), randomPlugboard(random));
        for (int i = 0; i < 10_000; i++) {
            char letter = Alphabet.letterAt(random.nextInt(Alphabet.SIZE));
            char encoded = machine.pressKey(letter);
            assertNotEquals(letter, encoded);
        }
    }

    @Test
    void differentRingSettingsChangeResultButKeepReciprocity() {
        EnigmaSettings withRings = new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, Alphabet.indexOf('B'), 0),
                new RotorSetting(RotorType.II, Alphabet.indexOf('F'), 0),
                new RotorSetting(RotorType.III, Alphabet.indexOf('K'), 0));
        EnigmaMachine machine = new EnigmaMachine(withRings, new Plugboard());

        String cipherText = machine.process("AAAAA");
        assertNotEquals("BDZGO", cipherText);

        String decoded = machine.process(cipherText);
        assertEquals("AAAAA", decoded);
    }

    @Test
    void nonAlphabeticCharactersAreKeptAndDoNotAdvanceRotors() {
        EnigmaMachine machine = new EnigmaMachine(defaultSettings(), new Plugboard());
        String result = machine.process("AA 123 AA");

        assertEquals(' ', result.charAt(2));
        assertEquals("123", result.substring(3, 6));

        EnigmaMachine reference = new EnigmaMachine(defaultSettings(), new Plugboard());
        String expectedFirstTwo = reference.process("AA");
        assertEquals(expectedFirstTwo, result.substring(0, 2));
    }

    @Test
    void settingsRejectDuplicateRotors() {
        assertThrows(IllegalArgumentException.class, () -> new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.I, 0, 0),
                new RotorSetting(RotorType.III, 0, 0)));
    }

    private static final RotorType[] NORMAL_ROTOR_TYPES =
            {RotorType.I, RotorType.II, RotorType.III, RotorType.IV, RotorType.V,
                    RotorType.VI, RotorType.VII, RotorType.VIII};

    private static EnigmaSettings randomSettings(Random random) {
        RotorType[] types = NORMAL_ROTOR_TYPES;
        RotorType first = types[random.nextInt(types.length)];
        RotorType second;
        do {
            second = types[random.nextInt(types.length)];
        } while (second == first);
        RotorType third;
        do {
            third = types[random.nextInt(types.length)];
        } while (third == first || third == second);

        ReflectorType reflector = random.nextBoolean() ? ReflectorType.UKW_B : ReflectorType.UKW_C;

        return new EnigmaSettings(
                reflector,
                new RotorSetting(first, random.nextInt(Alphabet.SIZE), random.nextInt(Alphabet.SIZE)),
                new RotorSetting(second, random.nextInt(Alphabet.SIZE), random.nextInt(Alphabet.SIZE)),
                new RotorSetting(third, random.nextInt(Alphabet.SIZE), random.nextInt(Alphabet.SIZE)));
    }

    private static Plugboard randomPlugboard(Random random) {
        Plugboard plugboard = new Plugboard();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        java.util.List<Character> pool = new java.util.ArrayList<>();
        for (char c : letters) {
            pool.add(c);
        }
        java.util.Collections.shuffle(pool, random);
        int pairs = random.nextInt(6);
        for (int i = 0; i < pairs; i++) {
            plugboard.connect(pool.get(i * 2), pool.get(i * 2 + 1));
        }
        return plugboard;
    }

    private static String randomText(Random random, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Alphabet.letterAt(random.nextInt(Alphabet.SIZE)));
        }
        return sb.toString();
    }
}
