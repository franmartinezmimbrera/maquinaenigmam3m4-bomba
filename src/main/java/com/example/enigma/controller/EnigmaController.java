package com.example.enigma.controller;

import com.example.enigma.model.Alphabet;
import com.example.enigma.model.EncryptionResult;
import com.example.enigma.model.EncryptionTrace;
import com.example.enigma.model.EnigmaMachine;
import com.example.enigma.model.EnigmaSettings;
import com.example.enigma.model.MachineListener;
import com.example.enigma.model.Plugboard;
import com.example.enigma.model.Rotor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EnigmaController {

    private final List<MachineListener> listeners = new ArrayList<>();
    private final Plugboard plugboard;
    private EnigmaSettings settings;
    private EnigmaMachine machine;
    private final List<EncryptionTrace> traces = new ArrayList<>();

    public EnigmaController(EnigmaSettings settings, Plugboard plugboard) {
        this.settings = settings;
        this.plugboard = plugboard;
        this.machine = new EnigmaMachine(settings, plugboard);
    }

    public void addListener(MachineListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MachineListener listener) {
        listeners.remove(listener);
    }

    public EnigmaSettings settings() {
        return settings;
    }

    public Plugboard plugboard() {
        return plugboard;
    }

    public void applySettings(EnigmaSettings newSettings) {
        this.settings = newSettings;
        this.machine = new EnigmaMachine(settings, plugboard);
        notifyPositions();
    }

    public String encrypt(String plainText) {
        EncryptionResult result = machine.processDetailed(plainText);
        traces.clear();
        traces.addAll(result.traces());
        notifyPositions();
        return result.text();
    }

    public String decrypt(String cipherText) {
        EncryptionResult result = machine.processDetailed(cipherText);
        traces.clear();
        traces.addAll(result.traces());
        notifyPositions();
        return result.text();
    }

    public List<EncryptionTrace> lastTraces() {
        return Collections.unmodifiableList(new ArrayList<>(traces));
    }

    public void clearTraces() {
        traces.clear();
    }

    public char pressKey(char letter) {
        EncryptionTrace trace = machine.pressKeyWithTrace(letter);
        traces.add(trace);
        notifyPositions();
        for (MachineListener listener : listeners) {
            listener.lampLit(trace.lamp());
        }
        return trace.lamp();
    }

    public void releaseKey() {
        for (MachineListener listener : listeners) {
            listener.lampOff();
        }
    }

    public void connectPlug(char a, char b) {
        plugboard.connect(a, b);
        plugboardChanged();
    }

    public void disconnectPlug(char letter) {
        plugboard.disconnect(letter);
        plugboardChanged();
    }

    public void clearPlugboard() {
        plugboard.clear();
        plugboardChanged();
    }

    private void plugboardChanged() {
        for (MachineListener listener : listeners) {
            listener.plugboardChanged();
        }
    }

    public char rotorPosition(int index) {
        return Alphabet.letterAt(rotorAt(index).position());
    }

    public void rotateRotor(int index, int delta) {
        Rotor rotor = rotorAt(index);
        rotor.setPosition(rotor.position() + delta);
        notifyPositions();
    }

    public boolean hasFourthRotor() {
        return settings.fourthRotor().isPresent();
    }

    private Rotor rotorAt(int index) {
        switch (index) {
            case 0:
                return machine.leftRotor();
            case 1:
                return machine.middleRotor();
            case 2:
                return machine.rightRotor();
            case 3:
                return machine.fourthRotor()
                        .orElseThrow(() -> new IllegalStateException("No hay cuarto rotor configurado (modo M3)"));
            default:
                throw new IllegalArgumentException("Índice de rotor inválido: " + index);
        }
    }

    private void notifyPositions() {
        char left = Alphabet.letterAt(machine.leftRotor().position());
        char middle = Alphabet.letterAt(machine.middleRotor().position());
        char right = Alphabet.letterAt(machine.rightRotor().position());
        for (MachineListener listener : listeners) {
            listener.rotorPositionsChanged(left, middle, right);
        }
    }
}
