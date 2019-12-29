package com.android.secureelement;

import android.security.keystore.UserNotAuthenticatedException;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.android.secureelement.enums.KeyProvider;

public interface ISecureElement {
    /**
     * Encrypts the value for the given key parameters.
     * @param value String to encrypt
     * @param params Configuration parameters
     * @return Base64 encrypted value
     * @throws Exception Exception
     */
    String encrypt(String value, @NonNull KeyGenParameters params) throws Exception;

    /**
     * Decrypts the value for the given key parameters.
     * @param value String to decrypt (must be base64 encoded)
     * @param params Configuration parameters
     * @return Decrypted value
     * @throws UserNotAuthenticatedException if the key is locked by user authentication, then you must use getUserPrompt
     * @throws Exception (any other) if the key you were looking for does not exist, or it is the wrong key
     */
    String decrypt(String value, @NonNull KeyGenParameters params) throws Exception, UserNotAuthenticatedException;

    /**
     * Deletes the key entry from the given provider.
     * @param keyName Key entry name to delete
     * @param provider Key provider to use
     * @throws Exception Exception
     */
    void clearElement(String keyName, KeyProvider provider) throws Exception;

    /**
     * Deletes all keys from the given provider.
     * @param provider Key provider to use
     * @throws Exception Exception
     */
    void clearAll(KeyProvider provider) throws Exception;

    /**
     * Checks if the device has a passcode/fingerprint or similar set up
     * @return true if available, else false
     * @throws Exception Exception
     */
    boolean isSecureDevice() throws Exception;

    /**
     * Checks if the device has a fingerprint set up
     * @return true if available, else false
     * @throws Exception Exception
     */
    boolean deviceHasFingerPrint() throws Exception;

    /**
     * Creates a (biometric) user authentication prompt to be started if decryption fails with a UserNotAuthenticatedException
     * You have to start that intent and listen to its result via activity lifecycle listener
     * After success, you have to run the decryption again.
     * @param context Used creating the intent
     * @param title Used for the user prompt
     * @param description Used for the user prompt
     * @return Intent to be started for authentication
     */
    Intent getUserPrompt(Context context, String title, String description);
}
