package com.android.secureelement.enums;

public enum KeyAlgorithm {
    RSA("RSA");

    private final String val;

    KeyAlgorithm(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
