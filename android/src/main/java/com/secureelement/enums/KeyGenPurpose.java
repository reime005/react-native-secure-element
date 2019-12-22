package com.secureelement.enums;

public enum KeyGenPurpose {
    ENCRYPT("ENCRYPT"),
    DECRYPT("DECRYPT"),
    SIGN("SIGN"),
    VERIFY("VERIFY");

    private final String val;

    KeyGenPurpose(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
