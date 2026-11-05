package com.example.enigma.model;

public enum ReflectorType {

    UKW_B("YRUHQSLDPXNGOKMIEBFZCWVJAT", false),
    UKW_C("FVPJIAOYEDRZXWGCTKUQSBNMHL", false),
    // "Thin" reflectors (M4): mounted together with the non-stepping 4th
    // rotor (Beta/Gamma), never used without one.
    UKW_B_THIN("ENKQAUYWJICOPBLMDXZVFTHRGS", true),
    UKW_C_THIN("RDOBJNTKVEHMLFCWZAXGYIPSUQ", true);

    private final String wiring;
    private final boolean thin;
    private final int[] mapping;

    ReflectorType(String wiring, boolean thin) {
        this.wiring = wiring;
        this.thin = thin;
        this.mapping = new int[Alphabet.SIZE];
        for (int i = 0; i < Alphabet.SIZE; i++) {
            mapping[i] = wiring.charAt(i) - 'A';
        }
    }

    public String wiring() {
        return wiring;
    }

    public boolean isThin() {
        return thin;
    }

    public int reflect(int index) {
        return mapping[index];
    }
}
