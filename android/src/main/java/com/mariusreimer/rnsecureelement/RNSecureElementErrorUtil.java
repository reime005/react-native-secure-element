package com.mariusreimer.rnsecureelement;

import javax.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

/**
 * Helper class for authentication errors.
 */
class RNSecureElementErrorUtil {

    /**
     * Create Error object to be passed back to the JS callback.
     */
    static WritableMap getError(@Nullable String key, String errorMessage) {
        WritableMap errorMap = Arguments.createMap();
        errorMap.putString("message", errorMessage);
        if (key != null) {
            errorMap.putString("key", key);
        }
        return errorMap;
    }

    static WritableMap getAuthError(@Nullable String key) {
        return getError(key, "Authentication Error");
    }
}
