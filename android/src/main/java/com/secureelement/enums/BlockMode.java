package com.secureelement.enums;

public enum BlockMode {
    ECB("ECB"),
    CBC("CBC"),
    CTR("CTR"),
    GCM("GCM");

    private final String val;

    BlockMode(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
