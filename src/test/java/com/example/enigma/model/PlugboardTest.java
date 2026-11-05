package com.example.enigma.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlugboardTest {

    @Test
    void unconnectedLetterPassesThrough() {
        Plugboard plugboard = new Plugboard();
        assertEquals('A', plugboard.swap('A'));
    }

    @Test
    void connectedPairSwapsSymmetrically() {
        Plugboard plugboard = new Plugboard();
        plugboard.connect('A', 'B');
        assertEquals('B', plugboard.swap('A'));
        assertEquals('A', plugboard.swap('B'));
    }

    @Test
    void cannotConnectLetterToItself() {
        Plugboard plugboard = new Plugboard();
        assertThrows(IllegalArgumentException.class, () -> plugboard.connect('A', 'A'));
    }

    @Test
    void cannotConnectAlreadyConnectedLetter() {
        Plugboard plugboard = new Plugboard();
        plugboard.connect('A', 'B');
        assertThrows(IllegalArgumentException.class, () -> plugboard.connect('A', 'C'));
        assertThrows(IllegalArgumentException.class, () -> plugboard.connect('C', 'B'));
    }

    @Test
    void disconnectRemovesPair() {
        Plugboard plugboard = new Plugboard();
        plugboard.connect('A', 'B');
        plugboard.disconnect('A');
        assertEquals('A', plugboard.swap('A'));
        assertEquals('B', plugboard.swap('B'));
    }

    @Test
    void canReachMaximumOfThirteenPairs() {
        Plugboard plugboard = new Plugboard();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < Plugboard.MAX_PAIRS; i++) {
            plugboard.connect(letters[i * 2], letters[i * 2 + 1]);
        }
        assertEquals(Plugboard.MAX_PAIRS, plugboard.pairCount());
    }

    @Test
    void pairsListsEachConnectionOnce() {
        Plugboard plugboard = new Plugboard();
        plugboard.connect('A', 'B');
        plugboard.connect('C', 'D');

        List<String> pairs = plugboard.pairs();

        assertEquals(2, pairs.size());
        assertTrue(pairs.contains("AB"));
        assertTrue(pairs.contains("CD"));
    }
}
