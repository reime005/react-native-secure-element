package com.android.secureelement.enums;

public enum EncryptionPadding {
    NoPadding("NoPadding"),
    PKCS7Padding("PKCS7Padding"),
    PKCS1Padding("PKCS1Padding"),
    OAEPPadding("OAEPPadding");

    private final String val;

    EncryptionPadding(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
