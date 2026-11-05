package com.example.enigma.bombe;

import com.example.enigma.model.EnigmaMachine;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.Plugboard;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorSetting;
import com.example.enigma.model.RotorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cancelar la búsqueda (interrumpiendo el hilo, como hace la ventana al cerrarse) debe detenerla enseguida. */
class BombeCancelTest {

    private static final String PLAINTEXT = "THEQUICKBROWNFOXJUMPSOVERTHELAZYDOGANDRUNSAWAYWITHTHESPOON";

    private static Menu menuWithLoop() {
        EnigmaSettings settings = new EnigmaSettings(ReflectorType.UKW_B,
                new RotorSetting(RotorType.III, 1, 16),
                new RotorSetting(RotorType.I, 0, 4),
                new RotorSetting(RotorType.IV, 2, 17));
        String cipher = new EnigmaMachine(settings, new Plugboard()).process(PLAINTEXT);
        int length = 12;
        for (int offset = 0; offset + length <= PLAINTEXT.length(); offset++) {
            String crib = PLAINTEXT.substring(offset, offset + length);
            Menu menu = Menu.build(crib, cipher.substring(offset, offset + length));
            if (menu.hasLoop()) {
                return menu;
            }
        }
        throw new AssertionError("no se encontró un crib con bucle");
    }

    @Test
    void interruptingTheThreadStopsTheSearchQuickly() throws Exception {
        final Menu menu = menuWithLoop();
        final List<RotorType> five = Arrays.asList(RotorType.I, RotorType.II, RotorType.III, RotorType.IV, RotorType.V);
        Thread worker = new Thread(() -> BombeSearch.search(menu, five, ReflectorType.UKW_B, new int[] {0, 0, 0}, null));
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(150);
        assertTrue(worker.isAlive(), "la búsqueda completa con 5 rotores debería tardar más de 150 ms");
        worker.interrupt();
        worker.join(10_000);
        assertFalse(worker.isAlive(), "la búsqueda no se detuvo al interrumpir el hilo");
    }

    /**
     * Regresión: la ventana de la Bombe llegó a ofrecer Beta y Gamma (solo válidos como 4º rotor) y la búsqueda
     * fallaba al instante. Con todos los rotores que sí son válidos como rotores normales no debe haber ningún error.
     */
    @Test
    void searchWithEveryValidRotorDoesNotFail() throws Exception {
        final Menu menu = menuWithLoop();
        final List<RotorType> valid = new ArrayList<>();
        for (RotorType type : RotorType.values()) {
            if (!type.isFourthRotorOnly()) {
                valid.add(type);
            }
        }
        assertTrue(valid.size() == 8, "deberían ser los rotores I a VIII");
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                BombeSearch.search(menu, valid, ReflectorType.UKW_B, new int[] {0, 0, 0}, null);
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(400);
        worker.interrupt();
        worker.join(10_000);
        assertNull(failure.get(), "la búsqueda falló: " + failure.get());
    }
}
