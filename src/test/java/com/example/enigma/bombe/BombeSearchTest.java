package com.example.enigma.bombe;

import com.example.enigma.model.Alphabet;
import com.example.enigma.model.EnigmaMachine;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.Plugboard;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BombeSearchTest {

    private static final String PLAINTEXT =
            "THEQUICKBROWNFOXJUMPSOVERTHELAZYDOGANDRUNSAWAYWITHTHESPOON";

    @Test
    void recoversTheTrueRotorOrderAndStartPositionFromAKnownCiphertext() {
        List<RotorType> trueOrder = Arrays.asList(RotorType.III, RotorType.I, RotorType.IV);
        int[] ringSettings = {Alphabet.indexOf('B'), Alphabet.indexOf('A'), Alphabet.indexOf('C')};
        int[] trueStartAtMessageBegin = {Alphabet.indexOf('Q'), Alphabet.indexOf('E'), Alphabet.indexOf('R')};

        EnigmaSettings settings = new EnigmaSettings(
                ReflectorType.UKW_B,
                new RotorSetting(trueOrder.get(0), ringSettings[0], trueStartAtMessageBegin[0]),
                new RotorSetting(trueOrder.get(1), ringSettings[1], trueStartAtMessageBegin[1]),
                new RotorSetting(trueOrder.get(2), ringSettings[2], trueStartAtMessageBegin[2]));
        Plugboard plugboard = new Plugboard();
        plugboard.connect('A', 'B');
        plugboard.connect('C', 'D');
        plugboard.connect('E', 'F');

        EnigmaMachine machine = new EnigmaMachine(settings, plugboard);
        String cipher = machine.process(PLAINTEXT);

        int cribLength = 12;
        Menu menu = null;
        int chosenOffset = -1;
        for (int offset = 0; offset + cribLength <= PLAINTEXT.length(); offset++) {
            String crib = PLAINTEXT.substring(offset, offset + cribLength);
            String cipherSegment = cipher.substring(offset, offset + cribLength);
            boolean validAlignment = Menu.findValidOffsets(cipherSegment, crib).contains(0);
            if (!validAlignment) {
                continue;
            }
            Menu candidate = Menu.build(crib, cipherSegment);
            if (candidate.hasLoop()) {
                menu = candidate;
                chosenOffset = offset;
                break;
            }
        }

        assertTrue(menu != null, "No se encontró ningún crib con bucle en el mensaje de prueba");

        // True rotor positions at the start of the chosen crib window.
        EnigmaMachine primer = new EnigmaMachine(settings, new Plugboard());
        for (int i = 0; i < chosenOffset; i++) {
            primer.advanceOnce();
        }
        int[] expectedPositions = {
                primer.leftRotor().position(), primer.middleRotor().position(), primer.rightRotor().position()
        };

        List<Stop> stops = BombeSearch.search(menu, trueOrder, ReflectorType.UKW_B, ringSettings, null);

        boolean foundTrueStop = stops.stream().anyMatch(stop ->
                stop.rotorOrder().equals(trueOrder)
                        && stop.startPositions()[0] == expectedPositions[0]
                        && stop.startPositions()[1] == expectedPositions[1]
                        && stop.startPositions()[2] == expectedPositions[2]);

        assertTrue(foundTrueStop, "La Bombe no encontró la posición real entre los stops: " + stops.size()
                + " stops encontrados, esperada " + expectedPositions[0] + expectedPositions[1] + expectedPositions[2]);
    }

    @Test
    void refusesToSearchAMenuWithoutLoops() {
        Menu menu = Menu.build("ABCDE", "FGHIJ");
        assertFalse(menu.hasLoop());
        assertThrows(IllegalArgumentException.class, () ->
                BombeSearch.search(menu, Arrays.asList(RotorType.I, RotorType.II, RotorType.III),
                        ReflectorType.UKW_B, new int[] {0, 0, 0}, null));
    }
}
