package com.secureelement.enums;

public enum KeyProvider {
    AndroidKeyStore("AndroidKeyStore");

    private final String val;

    KeyProvider(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
