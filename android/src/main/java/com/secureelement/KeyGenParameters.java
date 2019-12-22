package com.secureelement;

import com.secureelement.enums.BlockMode;
import com.secureelement.enums.EncryptionPadding;
import com.secureelement.enums.KeyAlgorithm;
import com.secureelement.enums.KeyProvider;
import com.secureelement.enums.Purpose;

public class KeyGenParameters {
    private KeyAlgorithm keyAlgorithm;
    private KeyProvider keyProvider;
    private BlockMode blockMode;
    private EncryptionPadding padding;
    private boolean keyGenUserAuthenticationRequired;
    private boolean keyGenInvalidatedByBiometricEnrollment;
    private int userAuthenticationValidityDurationSeconds;
    private Purpose purposes;
    private String keyAlias;
    private String promptTitle;
    private String promptDescription;

    //TODO: build-pattern...
    public KeyGenParameters(KeyAlgorithm keyAlgorithm, KeyProvider keyProvider, BlockMode blockMode, EncryptionPadding padding, boolean keyGenUserAuthenticationRequired, boolean keyGenInvalidatedByBiometricEnrollment, int userAuthenticationValidityDurationSeconds, Purpose purposes, String keyAlias, String promptTitle, String promptDescription) {
        this.keyAlgorithm = keyAlgorithm;
        this.keyProvider = keyProvider;
        this.blockMode = blockMode;
        this.padding = padding;
        this.keyGenUserAuthenticationRequired = keyGenUserAuthenticationRequired;
        this.keyGenInvalidatedByBiometricEnrollment = keyGenInvalidatedByBiometricEnrollment;
        this.userAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
        this.purposes = purposes;
        this.keyAlias = keyAlias;
        this.promptTitle = promptTitle;
        this.promptDescription = promptDescription;
    }

    public void setKeyAlgorithm(KeyAlgorithm keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public void setKeyProvider(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public void setBlockMode(BlockMode blockMode) {
        this.blockMode = blockMode;
    }

    public void setPadding(EncryptionPadding padding) {
        this.padding = padding;
    }

    public void setKeyGenUserAuthenticationRequired(boolean keyGenUserAuthenticationRequired) {
        this.keyGenUserAuthenticationRequired = keyGenUserAuthenticationRequired;
    }

    public void setKeyGenInvalidatedByBiometricEnrollment(boolean keyGenInvalidatedByBiometricEnrollment) {
        this.keyGenInvalidatedByBiometricEnrollment = keyGenInvalidatedByBiometricEnrollment;
    }

    public void setUserAuthenticationValidityDurationSeconds(int userAuthenticationValidityDurationSeconds) {
        this.userAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
    }

    public void setPurposes(Purpose purposes) {
        this.purposes = purposes;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public void setPromptTitle(String promptTitle) {
        this.promptTitle = promptTitle;
    }

    public void setPromptDescription(String promptDescription) {
        this.promptDescription = promptDescription;
    }

    public KeyAlgorithm getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public KeyProvider getKeyProvider() {
        return keyProvider;
    }

    public BlockMode getBlockMode() {
        return blockMode;
    }

    public EncryptionPadding getPadding() {
        return padding;
    }

    public boolean isKeyGenUserAuthenticationRequired() {
        return keyGenUserAuthenticationRequired;
    }

    public boolean isKeyGenInvalidatedByBiometricEnrollment() {
        return keyGenInvalidatedByBiometricEnrollment;
    }

    public int getUserAuthenticationValidityDurationSeconds() {
        return userAuthenticationValidityDurationSeconds;
    }

    public Purpose getPurposes() {
        return purposes;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getPromptTitle() {
        return promptTitle;
    }

    public String getPromptDescription() {
        return promptDescription;
    }

    public String getCipherTransformation() {
        return keyAlgorithm.toString() + "/" +
                blockMode.toString() + "/" +
                padding.toString();
    }
}
