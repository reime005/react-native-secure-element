package com.rnsecureelement;

import androidx.annotation.NonNull;

import javax.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.secureelement.KeyGenParameters;
import com.secureelement.enums.BlockMode;
import com.secureelement.enums.EncryptionPadding;
import com.secureelement.enums.KeyAlgorithm;
import com.secureelement.enums.KeyGenPurpose;
import com.secureelement.enums.KeyProvider;
import com.secureelement.enums.Purpose;

/**
 * Helper class...
 */
class RNSecureElementUtils {

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

    static KeyGenParameters getDefaultParams() {
        return new KeyGenParameters(
                KeyAlgorithm.RSA,
                KeyProvider.AndroidKeyStore,
                BlockMode.ECB,
                EncryptionPadding.PKCS1Padding,
                false,
                false,
                0,
                new Purpose(new KeyGenPurpose[]{KeyGenPurpose.ENCRYPT, KeyGenPurpose.DECRYPT}),
                "defaultKeyName",
                "Default title",
                "Default description"
        );
    }

    @NonNull
    public static KeyGenParameters getKeyGenParametersFromDict(ReadableMap opts) {
        final KeyGenParameters params = getDefaultParams();

        if (opts.hasKey("keyPairGeneratorAlgorithm")) {
            params.setKeyAlgorithm(KeyAlgorithm.valueOf(opts.getString("keyPairGeneratorAlgorithm")));
        }

        if (opts.hasKey("keyPairGeneratorProvider")) {
            params.setKeyProvider(KeyProvider.valueOf(opts.getString("keyPairGeneratorProvider")));
        }

        if (opts.hasKey("keyGenBlockMode")) {
            params.setBlockMode(BlockMode.valueOf(opts.getString("keyGenBlockMode")));
        }

        if (opts.hasKey("keyGenEncryptionPadding")) {
            params.setPadding(EncryptionPadding.valueOf(opts.getString("keyGenEncryptionPadding")));
        }

        if (opts.hasKey("keyGenUserAuthenticationRequired")) {
            params.setKeyGenUserAuthenticationRequired(opts.getBoolean("keyGenUserAuthenticationRequired"));
        }

        if (opts.hasKey("keyGenInvalidatedByBiometricEnrollment")) {
            params.setKeyGenInvalidatedByBiometricEnrollment(opts.getBoolean("keyGenInvalidatedByBiometricEnrollment"));
        }

        if (opts.hasKey("userAuthenticationValidityDurationSeconds")) {
            params.setUserAuthenticationValidityDurationSeconds(opts.getInt("userAuthenticationValidityDurationSeconds"));
        }

        if (opts.hasKey("userAuthenticationValidityDurationSeconds")) {
            params.setUserAuthenticationValidityDurationSeconds(opts.getInt("userAuthenticationValidityDurationSeconds"));
        }

        if (opts.hasKey("purposes")) {
            final ReadableArray purposes = opts.getArray("purposes");

            if (purposes != null) {
                final KeyGenPurpose keyGenPurposes[] = new KeyGenPurpose[purposes.size()];

                for (int i = 0; i < purposes.size(); i++) {
                    keyGenPurposes[i] = KeyGenPurpose.valueOf(purposes.getString(i));
                }

                params.setPurposes(new Purpose(keyGenPurposes));
            }
        }

        return params;
    }
}
