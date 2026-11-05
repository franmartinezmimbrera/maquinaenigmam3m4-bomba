package com.example.enigma.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EncryptionResult {

    private final String text;
    private final List<EncryptionTrace> traces;

    public EncryptionResult(String text, List<EncryptionTrace> traces) {
        this.text = text;
        this.traces = Collections.unmodifiableList(new ArrayList<>(traces));
    }

    public String text() {
        return text;
    }

    public List<EncryptionTrace> traces() {
        return traces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EncryptionResult)) {
            return false;
        }
        EncryptionResult other = (EncryptionResult) o;
        return Objects.equals(text, other.text) && traces.equals(other.traces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, traces);
    }

    @Override
    public String toString() {
        return "EncryptionResult[text=" + text + ", traces=" + traces + "]";
    }
}
