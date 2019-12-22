package com.mariusreimer.rnsecureelement;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

@ReactModule(name = RNSecureElementModule.TAG)
public final class RNSecureElementModule extends ReactContextBaseJavaModule {
    public static final String TAG = "RNSecureElementModule";
    public static final int ConfirmRequestId = 123;

    private KeyStore keystore;

    public RNSecureElementModule(final ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public Map<String, Object> getConstants() {
        // Export any constants to be used in your native module
        // https://facebook.github.io/react-native/docs/native-modules-android.html#the-toast-module
        final Map<String, Object> constants = new HashMap<>();
        //constants.put("EXAMPLE_CONSTANT", "example");

        return constants;
    }

    @ReactMethod
    public void configure(@NonNull final ReadableMap opts, final Callback callback) {
        WritableMap error = null;

        if (opts.hasKey("keystoreType") && ReadableType.String.equals(opts.getType("keystoreType"))) {
            final String keystoreType = opts.getString("keystoreType");

            try {
                keystore = KeyStore.getInstance(keystoreType);
                keystore.load(null); // prevent issue
            } catch (Exception e) {
                e.printStackTrace();
                error = RNSecureElementErrorUtil.getError(null, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke();
        }
    }

    @ReactMethod
    public void clearElement(final String keyAlias, final Callback callback) {
        WritableMap error = null;

        try {
            keystore.deleteEntry(keyAlias);
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementErrorUtil.getError(keyAlias, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke();
        }
    }

    @ReactMethod
    public void clearAll(final Callback callback) {
        WritableMap error = null;
        String keyAlias = null;

        try {
            while (keystore.aliases().hasMoreElements()) {
                keyAlias = keystore.aliases().nextElement();
                keystore.deleteEntry(keyAlias);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementErrorUtil.getError(keyAlias, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke();
        }
    }

    @ReactMethod
    public void encrypt(final String key, final String value, final ReadableMap opts, final Callback callback) {
        WritableMap error = null;
        String result = null;

        try {
            final KeyPair keyPair = getOrCreateKeyPair(key, opts);

            final Cipher cipher = Cipher.getInstance(getCipherTransformation(opts));
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

            final byte[] encryptedBytes = cipher.doFinal(value.getBytes());
            result = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementErrorUtil.getError(key, e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke(null, result);
        }
    }

    private String getCipherTransformation(ReadableMap opts) {
        return opts.getString("keyPairGeneratorAlgorithm") + "/" +
                opts.getString("keyGenBlockMode") + "/" +
                opts.getString("keyGenEncryptionPadding");
    }

    @ReactMethod
    public synchronized void decrypt(final String key, final String value, final ReadableMap opts, final Callback callback) {
        WritableMap error = null;
        String decryptedMessage = null;

        try {
            final KeyPair keyPair = getOrCreateKeyPair(key, opts);

            final Cipher cipher = Cipher.getInstance(getCipherTransformation(opts));
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

            final byte[] encryptedBytes = Base64.decode(value, Base64.DEFAULT);
            final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            decryptedMessage = new String(decryptedBytes);
        } catch (UserNotAuthenticatedException e) {
            e.printStackTrace();

            try {
                showUserPrompt(key, value, opts, callback);
                return;
            } catch (Exception e2) {
                e2.printStackTrace();
                error = RNSecureElementErrorUtil.getError(key, e2.getCause() != null ? e2.getCause().getMessage() : e2.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementErrorUtil.getError(key, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else if (decryptedMessage != null) {
            callback.invoke(null, decryptedMessage);
        }
    }

    @ReactMethod
    public void isSecureDevice(Callback callback) {
        final KeyguardManager km = (KeyguardManager) getReactApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        callback.invoke(null, km.isDeviceSecure());
    }

    @ReactMethod
    public void getDeviceFeatures(Callback callback) {
        final WritableArray features = Arguments.createArray();
        WritableMap error = null;

        try {
            if (hasFingerPrint()) {
                features.pushString("ANDROID_FINGERPRINT");
            }

            final KeyguardManager km = (KeyguardManager) getReactApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);

            if (km.isDeviceSecure()) {
                features.pushString("ANDROID_DEVICE_SECURE");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementErrorUtil.getError(null, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke(null, features);
        }
    }

    private boolean hasFingerPrint() {
        if (getReactApplicationContext().checkSelfPermission(Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {
            final FingerprintManager mFingerprintManager = (FingerprintManager) getReactApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);

            return mFingerprintManager.isHardwareDetected()
                    && mFingerprintManager.hasEnrolledFingerprints();
        }

        return false;
    }

    private ActivityEventListener mActivityEventListener;

    private void showUserPrompt(final String key, final String value, final ReadableMap opts, final Callback callback) throws Exception {
        final KeyguardManager keyguardManager = (KeyguardManager) getReactApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);

        final Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(opts.getString("userPromptTitle"), opts.getString("userPromptDescription"));

        final Activity currentActivity = getCurrentActivity();

        if (intent != null && currentActivity != null) {
            if (mActivityEventListener != null) {
                getReactApplicationContext().removeActivityEventListener(mActivityEventListener);
            }

            mActivityEventListener = new BaseActivityEventListener() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == ConfirmRequestId) {
                        WritableMap error = null;

                        try {
                            if (resultCode == Activity.RESULT_OK) {
                                decrypt(key, value, opts, callback);
                                return;
                            } else {
                                error = RNSecureElementErrorUtil.getError(key, "Result code: " + resultCode);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (error != null) {
                            callback.invoke(error);
                        }
                    }
                }
            };

            getReactApplicationContext().addActivityEventListener(mActivityEventListener);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            currentActivity.startActivityForResult(intent, ConfirmRequestId);
        } else {
            throw new Exception("User prompt could not be started");
        }
    }

    private KeyPair getOrCreateKeyPair(String key, ReadableMap opts) throws Exception {
        KeyPair keyPair = getKeyPair(key);

        if (keyPair != null) {
            return keyPair;
        }

        return createKeyPair(key, opts);
    }

    private KeyPair getKeyPair(String alias) throws Exception {
        if (keystore == null) {
            return null;
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, null);
        Certificate cert = keystore.getCertificate(alias);

        if (privateKey != null && cert != null && cert.getPublicKey() != null) {
            return new KeyPair(cert.getPublicKey(), privateKey);
        } else {
            return null;
        }
    }

    private KeyPair createKeyPair(String key, ReadableMap opts) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        final String keyPairGeneratorAlgorithm = opts.getString("keyPairGeneratorAlgorithm");
        final String keyPairGeneratorProvider = opts.getString("keyPairGeneratorProvider");
        final String keyGenBlockMode = opts.getString("keyGenBlockMode");
        final String keyGenEncryptionPadding = opts.getString("keyGenEncryptionPadding");
        final int userAuthenticationValidityDurationSeconds = opts.getInt("userAuthenticationValidityDurationSeconds");
        final boolean keyGenUserAuthenticationRequired = opts.getBoolean("keyGenUserAuthenticationRequired");
        final boolean keyGenInvalidatedByBiometricEnrollment = opts.getBoolean("keyGenInvalidatedByBiometricEnrollment");
        final ArrayList<Object> purposes = opts.getArray("purposes").toArrayList();

        int purposesFlag = 0;

        if (purposes.contains("ENCRYPT")) {
            purposesFlag |= KeyProperties.PURPOSE_ENCRYPT;
        }

        if (purposes.contains("DECRYPT")) {
            purposesFlag |= KeyProperties.PURPOSE_DECRYPT;
        }

        if (purposes.contains("SIGN")) {
            purposesFlag |= KeyProperties.PURPOSE_SIGN;
        }

        if (purposes.contains("VERIFY")) {
            purposesFlag |= KeyProperties.PURPOSE_VERIFY;
        }

        final KeyPairGenerator generator = KeyPairGenerator.getInstance(keyPairGeneratorAlgorithm, keyPairGeneratorProvider);

        final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(key, purposesFlag)
                .setBlockModes(keyGenBlockMode)
                .setEncryptionPaddings(keyGenEncryptionPadding)
                .setUserAuthenticationValidityDurationSeconds(userAuthenticationValidityDurationSeconds)
                .setUserAuthenticationRequired(keyGenUserAuthenticationRequired);

        if (android.os.Build.VERSION.SDK_INT >= 24){
            // Prevent Deletion of Keys if new Fingerprint is enrolled
            builder.setInvalidatedByBiometricEnrollment(keyGenInvalidatedByBiometricEnrollment);
        }

        generator.initialize(builder.build());

        return generator.generateKeyPair();
    }

    @ReactMethod
    public void authenticate(Callback callback) {
        //NOT YET IMPLEMENTED
    }
}
