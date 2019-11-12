//  Created by react-native-create-bridge

package com.nect.app.secureelement;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
//import org.bouncycastle.cert.X509CertificateHolder;
//import org.bouncycastle.cert.X509v3CertificateBuilder;
//import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.IEKeySpec;
//import org.bouncycastle.jce.spec.IESParameterSpec;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.OperatorCreationException;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

//import java.io.IOException;
//import java.math.BigInteger;
//import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;

import static android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED;

import android.os.Looper;
import android.os.Handler;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Arguments;

public class SecureElementModule extends ReactContextBaseJavaModule {
    public static final String TAG = "SecureElement";
    private static ReactApplicationContext reactContext = null;
//    private static IEKeySpec keySpec = null;
    private KeyStore _ks;
    private String TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding";
    private static int ConfirmRequestId = 1;

    private String decryptionKey = null;
    private String decryptionMessage = null;
    private String decryptionAuthTitle = null;
    private String decryptionAuthDescription  = null;
    private Promise decryptionPromise = null;


    private String encryptionKey = null;
    private String encryptionMessage = null;
    private Promise encryptionPromise = null;

    private int counter;

    private final FingerprintManagerCompat mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private Promise mPromise;
    private boolean mIsAuthenticating = false;

    private static final int AUTHENTICATION_TYPE_FINGERPRINT = 1;

    private final FingerprintManagerCompat.AuthenticationCallback mAuthenticationCallback =
      new FingerprintManagerCompat.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
          mIsAuthenticating = false;
          WritableMap successResult = Arguments.createMap();
          successResult.putBoolean("success", true);
          safeResolve(successResult);
        }

        @Override
        public void onAuthenticationFailed() {
          mIsAuthenticating = false;
          WritableMap failResult = Arguments.createMap();
          failResult.putBoolean("success", false);
          failResult.putString("error", "authentication_failed");
          safeResolve(failResult);
          // Failed authentication doesn't stop the authentication process, stop it anyway so it works
          // with the promise API.
          safeCancel();
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
          mIsAuthenticating = false;
          WritableMap errorResult = Arguments.createMap();
          errorResult.putBoolean("success", false);
          errorResult.putString("error", convertErrorCode(errMsgId));
          errorResult.putString("message", errString.toString());
          safeResolve(errorResult);
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
          mIsAuthenticating = false;
          WritableMap helpResult = Arguments.createMap();
          helpResult.putBoolean("success", false);
          helpResult.putString("error", convertHelpCode(helpMsgId));
          helpResult.putString("message", helpString.toString());
          safeResolve(helpResult);
          // Help doesn't stop the authentication process, stop it anyway so it works with the
          // promise API.
          safeCancel();
        }
      };

//      private void showBiometricPrompt(Signature signature) {
//        BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();
//        BiometricPrompt mBiometricPrompt = new BiometricPrompt((FragmentActivity) this.getCurrentActivity(), getMainThreadExecutor(), authenticationCallback);
//
//        // Set prompt info
//        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setDescription("Description")
//                .setTitle("Title")
//                .setSubtitle("Subtitle")
//                .setNegativeButtonText("Cancel")
//                .build();
//
//        // Show biometric prompt
//        if (signature != null) {
//            Log.i(TAG, "Show biometric prompt");
//            mBiometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(signature));
//        }
//    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (requestCode == ConfirmRequestId) {
                Log.d(TAG, "onActivityResult: " + resultCode);
                if(decryptionKey != null && decryptionMessage != null && decryptionAuthTitle != null && decryptionAuthDescription != null && decryptionPromise != null){
                    // Credentials entered successfully!
                    if (resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Activity.RESULT_OK");
                        // retry decryption
                        decryptWithKeyPair(decryptionKey, decryptionMessage, decryptionAuthTitle, decryptionAuthDescription, decryptionPromise);
                    } else {
                        Log.d(TAG, "onActivityResult: authentication dismissed");
                        // reject the promise...
                        decryptionPromise.reject("authentication dismissed", new Error("authentication dismissed"));
                    }

                    resetDecryptionPromise();
                }

                if(encryptionKey != null && encryptionMessage != null && encryptionPromise != null){
                    // Credentials entered successfully!
                    if (resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: encryptionKey Activity.RESULT_OK");
                        // retry decryption
                        encryptWithKeyPair(encryptionKey, encryptionMessage, encryptionPromise);
                    } else {
                        Log.d(TAG, "onActivityResult: encryptionKey authentication dismissed");
                        // reject the promise...
                        encryptionPromise.reject("authentication dismissed", new Error("authentication dismissed"));
                    }

                    resetEncryptionPromise();
                }
            }
        }
    };

    private void resetDecryptionPromise() {
        if (decryptionPromise != null) {
            decryptionPromise.reject("decryption failed", "decryptionPromise was not null");
        }

        decryptionKey = null;
        decryptionMessage = null;
        decryptionAuthTitle = null;
        decryptionAuthDescription = null;
        decryptionPromise = null;
    }

    private void resetEncryptionPromise() {
        if (encryptionPromise != null) {
            encryptionPromise.reject("encryption failed", "encryptionPromise was not null");
        }

        encryptionKey = null;
        encryptionMessage = null;
        encryptionPromise = null;
    }

    public SecureElementModule(ReactApplicationContext context) {
        // Pass in the context to the constructor and save it so you can emit events
        // https://facebook.github.io/react-native/docs/native-modules-android.html#the-toast-module
        super(context);

        //Security.addProvider(new BouncyCastleProvider());
        reactContext = context;

        reactContext.addActivityEventListener(mActivityEventListener);
        mFingerprintManager = FingerprintManagerCompat.from(context);
    }

    @Override
    public String getName() {
        // Tell React the name of the module
        // https://facebook.github.io/react-native/docs/native-modules-android.html#the-toast-module
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

    // https://proandroiddev.com/secure-data-in-android-encryption-in-android-part-2-991a89e55a23
    private KeyPair createAndroidKeyStoreAsymmetricKey(String identifier) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

        Log.d(TAG, "createAndroidKeyStoreAsymmetricKey for identifier: " + identifier);
        initGeneratorWithKeyGenParameterSpec(generator, identifier);
        Log.d(TAG, "createAndroidKeyStoreAsymmetricKey for identifier: " + identifier);

        // Generates Key with given spec and saves it to the KeyStore
        return generator.generateKeyPair();
    }

    private void initGeneratorWithKeyGenParameterSpec(KeyPairGenerator generator, String alias) throws Exception {
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                // no additional authentication will be required for key extraction
                .setUserAuthenticationRequired(false);

        if (android.os.Build.VERSION.SDK_INT >= 24){
            // Prevent Deletion of Keys if new Fingerprint is enrolled
            builder.setInvalidatedByBiometricEnrollment(false);
        }

        generator.initialize(builder.build());
    }

    private KeyPair getAndroidKeyStoreAsymmetricKeyPair(String identifier) throws Exception {
        KeyStore keyStore = createAndroidKeyStore();

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(identifier, null);
        Certificate cert = keyStore.getCertificate(identifier);

        if (privateKey != null && cert != null && cert.getPublicKey() != null) {
            return new KeyPair(cert.getPublicKey(), privateKey);
        } else {
            return null;
        }
    }

    private void removeAndroidKeyStoreKey(String identifier) throws Exception {
        KeyStore keyStore = createAndroidKeyStore();
        keyStore.deleteEntry(identifier);
    }

    private KeyStore createAndroidKeyStore() throws Exception {
        if (this._ks == null) {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            this._ks = keyStore;
        }

        return this._ks;
    }

    private KeyPair getKeyPair(String keyIdentifier) throws Exception {
        createAndroidKeyStore();
        KeyPair keyPair = null;

        try{
            keyPair = getAndroidKeyStoreAsymmetricKeyPair(keyIdentifier);

            if (keyPair == null) {
                Log.d(TAG, "getKeyPair: keyPair was null");
                keyPair = createAndroidKeyStoreAsymmetricKey(keyIdentifier);
            }
        } catch (UnrecoverableKeyException e){
            e.printStackTrace();
            // key seems to have been present in the past, but was deleted
            keyPair = createAndroidKeyStoreAsymmetricKey(keyIdentifier);
        }

        return keyPair;
    }

    public void cancelAuthenticate(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("UI thread", "I am the UI thread");

                safeCancel();
            }
        });
    }

    private void safeCancel() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    private void safeResolve(Object result) {
        if (mPromise != null) {
            mPromise.resolve(result);
            mPromise = null;
        }
    }

    private static String convertErrorCode(int code) {
        switch (code) {
          case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
            return "user_cancel";
          case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
            return "not_available";
          case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
            return "lockout";
          case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
            return "no_space";
          case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
            return "timeout";
          case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
            return "unable_to_process";
          default:
            return "unknown";
        }
      }

      private static String convertHelpCode(int code) {
        switch (code) {
          case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
            return "imager_dirty";
          case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
            return "insufficient";
          case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
            return "partial";
          case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
            return "too_fast";
          case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
            return "too_slow";
          default:
            return "unknown";
        }
      }

    @ReactMethod
    public void authenticate(String reason, String fallbackLabel, Promise promise) {
                Log.d("UI thread", "I am the UI thread");

                if (mIsAuthenticating) {
                    WritableMap cancelResult = Arguments.createMap();
                    cancelResult.putBoolean("success", false);
                    cancelResult.putString("error", "app_cancel");
                    safeResolve(cancelResult);
                    mPromise = promise;
                    return;
                  }

                  mIsAuthenticating = true;
                  mPromise = promise;
                  mCancellationSignal = new CancellationSignal();
        showUserAuthenticationScreen();

                  //mFingerprintManager.authenticate(null, 0, mCancellationSignal, mAuthenticationCallback, null);
    }

    @ReactMethod
    public void encryptWithKeyPair(String keyIdentifier, String message, Promise promise) {
        try {

            Log.d(TAG, "encryptWithKeyPair keyIdentifier: " + keyIdentifier);
            // if deviceIsSecure?
            // else...

            KeyPair keyPair;
            if ("app".equals(keyIdentifier)) {
                keyPair = getKeyPair("nect.app.encryption");
            } else {
                keyPair = getKeyPair("nect.transport.encryption");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC);

            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] bytes = cipher.doFinal(message.getBytes());
            String encryptedMessage = Base64.encodeToString(bytes, Base64.DEFAULT);

            promise.resolve(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- encryptWithKey() --- " + e.getClass().getName() + ": " + e.getMessage());
            promise.reject("encryption failed", e);
        }
    }

//    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
//        // Callback for biometric authentication result
//        return new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                Log.i(TAG, "onAuthenticationSucceeded");
//                super.onAuthenticationSucceeded(result);
//                if (result.getCryptoObject() != null &&
//                        result.getCryptoObject().getSignature() != null) {
//                    try {
//                        Signature signature = result.getCryptoObject().getSignature();
//                        signature.update("foo".getBytes());
//                        String signatureString = Base64.encodeToString(signature.sign(), Base64.URL_SAFE);
//                        // Normally, ToBeSignedMessage and Signature are sent to the server and then verified
//                        Log.i(TAG, "Signature (Base64 EncodeD): " + signatureString);
//                        Toast.makeText(reactContext, signatureString, Toast.LENGTH_SHORT).show();
//                    } catch (SignatureException e) {
//                        throw new RuntimeException();
//                    }
//                } else {
//                    // Error
//                    Toast.makeText(reactContext, "Something wrong", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//            }
//        };
//    }

    private void showUserAuthenticationScreen() {
        BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();
        BiometricPrompt mBiometricPrompt = new BiometricPrompt((FragmentActivity) this.getCurrentActivity(), getMainThreadExecutor(), authenticationCallback);

        // Set prompt info
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setDescription("Description")
                .setTitle("Title")
                .setSubtitle("Subtitle")
                .setNegativeButtonText("Cancel")
                .build();

        // Show biometric prompt
            Log.i(TAG, "Show biometric prompt");
            mBiometricPrompt.authenticate(promptInfo);

    }

    private Executor getMainThreadExecutor() {
        return new MainThreadExecutor();
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable r) {
            handler.post(r);
        }
    }

    private void showLegacyPrompt() {
        KeyguardManager keyguardManager = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);

        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(decryptionAuthTitle, decryptionAuthDescription);
        Activity currentActivity = getCurrentActivity();

        if (keyguardManager.isKeyguardLocked() && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && currentActivity != null) {
            Log.d(TAG, "keyguardManager.requestDismissKeyguard()");
            keyguardManager.requestDismissKeyguard(currentActivity, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissError() {
                    super.onDismissError();
                    Log.d(TAG, "onDismissError");
                    showLegacyPrompt();
                }

                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    Log.d(TAG, "onDismissSucceeded");
                    if(decryptionKey != null && decryptionMessage != null && decryptionAuthTitle != null && decryptionAuthDescription != null && decryptionPromise != null) {
                        decryptWithKeyPair(decryptionKey, decryptionMessage, decryptionAuthTitle, decryptionAuthDescription, decryptionPromise);
                        resetDecryptionPromise();
                    }
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                    Log.d(TAG, "onDismissCancelled");
                    showLegacyPrompt();
                }
            });
        }

        if (intent != null && currentActivity != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            currentActivity.startActivityForResult(intent, ConfirmRequestId, null);
        } else if (decryptionPromise != null) {
            resetDecryptionPromise();
        }
    }

    @ReactMethod
    public void decryptWithKeyPair (String keyIdentifier, String encryptedMessage, String userAuthenticationTitle, String userAuthenticationDescription, Promise promise) {


        try{
            KeyPair keyPair;
            Log.d(TAG, "decryptWithKeyPair keyIdentifier: " + keyIdentifier);
            if("app".equals(keyIdentifier)){
                keyPair = getKeyPair("nect.app.encryption");
            } else {
                keyPair = getKeyPair("nect.transport.encryption");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC);

            Log.d(TAG, "cipher initializes...");
            //Log.d(TAG, "keyPair public key: " + Arrays.toString(keyPair.getPublic().getEncoded()));
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            Log.d(TAG, "cipher initialized");

            byte[] encryptedData = Base64.decode(encryptedMessage, Base64.DEFAULT);
            Log.d(TAG, "cipher.doFinal");
            byte[] decryptedData = cipher.doFinal(encryptedData);
            String decryptedMessage = new String(decryptedData);
            Log.d(TAG, "decryptedMessage done");

            this.counter = 0;
            promise.resolve(decryptedMessage);
        } catch(UserNotAuthenticatedException e){
            e.printStackTrace();

            // 'fix' problem that after two failed authentications, the app gets stuck
             this.counter++;

             if (this.counter > 1) {
                 this.counter = 0;

                //  try {
                //      _ks = null;

                //      KeyPair anotherPair = null;

                //      anotherPair = getKeyPair("hack");

                //      Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                //      c.init(Cipher.DECRYPT_MODE, anotherPair.getPrivate());

                //      Log.d(TAG, "AES/CBC/PKCS5PADDING works");
                //  } catch (Exception e2) {
                //      e2.printStackTrace();
                //  }

                 promise.reject("decryption failed", e);
                 return;
            }

            decryptionKey = keyIdentifier;
            decryptionMessage = encryptedMessage;
            decryptionAuthTitle = userAuthenticationTitle;
            decryptionAuthDescription = userAuthenticationDescription;
            decryptionPromise = promise;

            showUserAuthenticationScreen();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- decryptWithKey() --- " + e.getClass().getName() + ": " + e.getMessage());
            promise.reject("decryption failed", e);
        }
    }

    @ReactMethod
    public void signWithKeyPair (String keyIdentifier, String message, Promise promise) {
        try{
//            Log.d("ReactNative", "---- Try Keygeneration ----");
//            KeyPair keyPair;
//            if(keyIdentifier == "app"){
//                keyPair = getKeyPair("nect.app.encryption");
//            } else {
//                keyPair = getKeyPair("nect.transport.encryption");
//            }
//
//            Signature s = Signature.getInstance("ECIES", new BouncyCastleProvider());
//            s.initSign(key);
            //s.update(message);
            //byte[] signature = s.sign();

            promise.resolve("c3s7+");
        } catch (Exception e) {
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- signWithKey() --- " + e.getClass().getName() + ": " + e.getMessage());
            promise.reject("signing failed", new Error());
        }
    }

    @ReactMethod
    public void hasSecureElement (Promise promise) {
        try{
            KeyPair keyPair = getAndroidKeyStoreAsymmetricKeyPair("nect.secureElementTest.encryption");
            KeyFactory factory = KeyFactory.getInstance(keyPair.getPrivate().getAlgorithm(), "AndroidKeyStore");
            KeyInfo keyInfo = factory.getKeySpec(keyPair.getPrivate(), KeyInfo.class);

            Log.d("ReactNative", "---- KEY is hardware-backed: " + String.valueOf(keyInfo.isInsideSecureHardware()));
            Log.d("ReactNative", "---- KEY Authentication is forced: " + String.valueOf(keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware()));
            promise.resolve(keyInfo.isInsideSecureHardware());

            removeAndroidKeyStoreKey("nect.secureElementTest.encryption");
        } catch (Exception e) {
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- hasSecureElement() --- " + e.getClass().getName() + ": " + e.getMessage());
            promise.reject("test hasSecureElement failed", e);
        }
    }

    @ReactMethod
    public void deleteKeyPairs(Promise promise){
        Log.d("ReactNative", "---- delete KeyPairs ");
        try{
            removeAndroidKeyStoreKey("nect.app.encryption");
            Log.d("ReactNative", "--- successfully deleted nect.app.encryption");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- deleteKeyPairs() --- " + e.getClass().getName() + ": " + e.getMessage());
            // this may happen when the key is not yet present
            // ...
            // we have to assume that all keys have been cleared by though
        }

        try{
            removeAndroidKeyStoreKey("nect.transport.encryption");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ReactNative", "--- EXCEPTION CAUGHT -- deleteKeyPairs() --- " + e.getClass().getName() + ": " + e.getMessage());
            // this may happen when the key is not yet present
            // ...
            // we have to assume that all keys have been cleared by though
        }

        promise.resolve(true);
    }

    private CancellationSignal getCancellationSignal() {
        // With this cancel signal, we can cancel biometric prompt operation
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                //handle cancel result
                Log.i(TAG, "Canceled");
            }
        });
        return cancellationSignal;
    }

    private boolean isDeviceSecure(){
        KeyguardManager km = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
        // isKeyGuardSecure (SIM-Lock-State?)
        return km.isDeviceSecure();
    }

    private boolean isDeviceLocked(){
        KeyguardManager km = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
        // isKeyGuardSecure (SIM-Lock-State?)
        return km.isDeviceLocked();
    }

    @ReactMethod
    public void getIsDeviceSecure(Promise promise) {
        promise.resolve(isDeviceSecure());
    }

    @ReactMethod
    public void getIsDeviceLocked(Promise promise) {
        promise.resolve(isDeviceLocked());
    }

    //https://developer.android.com/training/articles/keystore.html#UserAuthentication
    private boolean isFingerprintAuthAvailable() {
        FingerprintManager mFingerprintManager = (FingerprintManager) reactContext.getSystemService(reactContext.FINGERPRINT_SERVICE);
        //FingerprintManager mFingerprintManager;
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }


    private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
        // A method for emitting from the native side to JS
        // https://facebook.github.io/react-native/docs/native-modules-android.html#sending-events-to-javascript
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
    }
}
