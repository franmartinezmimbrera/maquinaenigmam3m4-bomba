package com.example.enigma.bombe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuTest {

    @Test
    void rejectsAlignmentWhereALetterWouldEncodeAsItself() {
        assertThrows(IllegalArgumentException.class, () -> Menu.build("HELLO", "HXYZW"));
    }

    @Test
    void buildsLinksForEachPosition() {
        Menu menu = Menu.build("ABC", "XYZ");
        assertEquals(3, menu.links().size());
    }

    @Test
    void detectsLoopWhenTwoPositionsCloseACycle() {
        // A->B at position 0, B->A at position 1: closes a 2-letter loop.
        Menu menu = Menu.build("AB", "BA");
        assertTrue(menu.hasLoop());
    }

    @Test
    void noLoopWithoutRepeatedLetters() {
        Menu menu = Menu.build("ABCDE", "FGHIJ");
        assertFalse(menu.hasLoop());
        assertEquals(0, menu.loopCount());
    }

    @Test
    void loopCountGrowsWithEachExtraClosingEdge() {
        // Each extra A<->B link at a different position is a genuinely new
        // consistency check (different permutation), so it counts as one
        // more independent loop even though it is the same two letters.
        Menu menu = Menu.build("ABAB", "BABA");
        assertEquals(3, menu.loopCount());
    }

    @Test
    void findValidOffsetsSkipsPositionsWithSelfEncryption() {
        // crib "CAT" against "DOGCATSXX": offset 0 -> D,O,G vs C,A,T (all different, valid);
        // offset 3 -> C,A,T vs C,A,T (self-encryption at every position, invalid).
        List<Integer> offsets = Menu.findValidOffsets("DOGCATSXX", "CAT");
        assertTrue(offsets.contains(0));
        assertFalse(offsets.contains(3));
    }
}
