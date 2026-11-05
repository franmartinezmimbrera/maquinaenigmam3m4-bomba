package com.example.enigma.model;

public interface MachineListener {

    default void rotorPositionsChanged(char left, char middle, char right) {
    }

    default void lampLit(char letter) {
    }

    default void lampOff() {
    }

    default void plugboardChanged() {
    }
}
