package com.secureelement;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.secureelement.enums.KeyProvider;

public interface ISecureElement {
    String encrypt(String value, @NonNull KeyGenParameters params) throws Exception;
    String decrypt(String value, @NonNull KeyGenParameters params) throws Exception;

    void clearElement(String keyName, KeyProvider provider) throws Exception;
    void clearAll(KeyProvider provider) throws Exception;

    boolean isSecureDevice() throws Exception;
    boolean deviceHasFingerPrint() throws Exception;

    Intent getUserPrompt(Context context, String title, String description);
}
