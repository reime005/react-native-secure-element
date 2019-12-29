package com.android.secureelement;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.android.secureelement.enums.KeyProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

public class SecureElement implements ISecureElement {
    private KeyStore mKeyStore;
    private Context mContext;

    public SecureElement(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public String encrypt(String value, @NonNull KeyGenParameters params) throws Exception {
        final KeyPair keyPair = getOrCreateKeyPair(params);

        final Cipher cipher = Cipher.getInstance(params.getCipherTransformation());
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

        final byte[] encryptedBytes = cipher.doFinal(value.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    @Override
    public String decrypt(String value, @NonNull KeyGenParameters params) throws Exception {
        final KeyPair keyPair = getKeyPair(params);

        if (keyPair == null) {
            throw new Exception("KeyPair could not be found");
        }

        final Cipher cipher = Cipher.getInstance(params.getCipherTransformation());
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        final byte[] encryptedBytes = Base64.decode(value, Base64.DEFAULT);
        final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }

    @Override
    public void clearElement(String keyName, KeyProvider keyProvider) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(keyProvider.toString());
        keyStore.load(null);
        keyStore.deleteEntry(keyName);
    }

    @Override
    public void clearAll(KeyProvider keyProvider) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(keyProvider.toString());
        keyStore.load(null);

        String keyAlias;

        while (keyStore.aliases().hasMoreElements()) {
            keyAlias = keyStore.aliases().nextElement();
            keyStore.deleteEntry(keyAlias);
        }
    }

    @Override
    public boolean isSecureDevice() throws Exception {
        return false;
    }

    @Override
    public boolean deviceHasFingerPrint() throws Exception {
        return false;
    }

    private KeyPair getOrCreateKeyPair(final KeyGenParameters params) throws Exception {
        final KeyPair keyPair = getKeyPair(params);

        if (keyPair != null) {
            return keyPair;
        }

        return createKeyPair(params);
    }

    private KeyPair getKeyPair(final KeyGenParameters params) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(params.getKeyProvider().toString());
        keyStore.load(null);

        final PrivateKey privateKey = (PrivateKey) keyStore.getKey(params.getKeyAlias(), null);
        final Certificate cert = keyStore.getCertificate(params.getKeyAlias());

        if (privateKey != null && cert != null && cert.getPublicKey() != null) {
            return new KeyPair(cert.getPublicKey(), privateKey);
        } else {
            return null;
        }
    }

    private KeyPair createKeyPair(final KeyGenParameters params) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(params.getKeyAlgorithm().toString(), params.getKeyProvider().toString());

        final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(params.getKeyAlias(), Integer.valueOf(params.getPurposes().toString()))
                .setBlockModes(params.getBlockMode().toString())
                .setEncryptionPaddings(params.getPadding().toString())
                .setUserAuthenticationValidityDurationSeconds(params.getUserAuthenticationValidityDurationSeconds())
                .setUserAuthenticationRequired(params.isKeyGenUserAuthenticationRequired());

        if (android.os.Build.VERSION.SDK_INT >= 24){
            // Prevent Deletion of Keys if new Fingerprint is enrolled
            builder.setInvalidatedByBiometricEnrollment(params.isKeyGenInvalidatedByBiometricEnrollment());
        }

        generator.initialize(builder.build());

        return generator.generateKeyPair();
    }

    public Intent getUserPrompt(Context context, String title, String description) {
        final KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        final Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(title, description);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }
}
