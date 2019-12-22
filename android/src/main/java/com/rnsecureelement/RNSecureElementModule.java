package com.rnsecureelement;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.security.keystore.UserNotAuthenticatedException;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.secureelement.ISecureElement;
import com.secureelement.KeyGenParameters;
import com.secureelement.SecureElement;
import com.secureelement.enums.KeyProvider;

import java.util.HashMap;
import java.util.Map;

@ReactModule(name = RNSecureElementModule.TAG)
public final class RNSecureElementModule extends ReactContextBaseJavaModule {
    public static final String TAG = "RNSecureElementModule";
    private final ISecureElement secureElement;
    private static final int ConfirmRequestId = 123;


    public RNSecureElementModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        secureElement = new SecureElement(reactContext);
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
    public void clearElement(final String keyAlias, final String providerName, final Callback callback) {
        WritableMap error = null;

        try {
            secureElement.clearElement(keyAlias, KeyProvider.valueOf(providerName));
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementUtils.getError(keyAlias, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke();
        }
    }

    @ReactMethod
    public void clearAll(final String providerName, final Callback callback) {
        WritableMap error = null;

        try {
            secureElement.clearAll(KeyProvider.valueOf(providerName));
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementUtils.getError(null, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
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
            result = secureElement.encrypt(value, RNSecureElementUtils.getKeyGenParametersFromDict(opts));
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementUtils.getError(key, e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke(null, result);
        }
    }

    @ReactMethod
    public synchronized void decrypt(final String key, final String value, final ReadableMap opts, final Callback callback) {
        WritableMap error = null;
        String decryptedMessage = null;

        final KeyGenParameters params = RNSecureElementUtils.getKeyGenParametersFromDict(opts);

        try {
            decryptedMessage = secureElement.decrypt(value, params);
        } catch (UserNotAuthenticatedException e) {
            e.printStackTrace();
            try {
                showUserPrompt(value, params, callback);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = RNSecureElementUtils.getError(key, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
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
            error = RNSecureElementUtils.getError(null, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (error != null) {
            callback.invoke(error);
        } else {
            callback.invoke(null, features);
        }
    }

    private boolean hasFingerPrint() {
        try {
            return secureElement.deviceHasFingerPrint();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private ActivityEventListener mActivityEventListener;

    private void showUserPrompt(final String value, final KeyGenParameters params, final Callback callback) throws Exception {
        final Intent intent = secureElement.getUserPrompt(getReactApplicationContext(), params.getPromptTitle(), params.getPromptDescription());

        final Activity currentActivity = getCurrentActivity();

        if (intent != null && currentActivity != null) {
            if (mActivityEventListener != null) {
                getReactApplicationContext().removeActivityEventListener(mActivityEventListener);
            }

            mActivityEventListener = new BaseActivityEventListener() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == ConfirmRequestId) {
                        try {
                            if (resultCode == Activity.RESULT_OK) {
                                final String result = secureElement.decrypt(value, params);
                                callback.invoke(null, result);
                            } else {
                                throw new Exception("Result code: " + resultCode);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.invoke(RNSecureElementUtils.getError(null, e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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

    @ReactMethod
    public void authenticate(Callback callback) {
        //NOT YET IMPLEMENTED
    }
}
