package com.android.secureelement.enums;

import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;

public class Purpose {
    private KeyGenPurpose[] purposes;

    public Purpose(KeyGenPurpose[] purposes) {
        this.purposes = purposes;
    }

    /**
     * Calculates purposes flags based on input.
     *
     * See {@link KeyProperties}.
     */
    @NonNull
    @Override
    public String toString() {
        int flags = 0;

        if (purposes != null) {
            for (int i = 0; i < purposes.length; i++) {
                switch (purposes[i].toString()) {
                    case "ENCRYPT":
                        flags |= KeyProperties.PURPOSE_ENCRYPT;
                        break;
                    case "DECRYPT":
                        flags |= KeyProperties.PURPOSE_DECRYPT;
                        break;
                    case "SIGN":
                        flags |= KeyProperties.PURPOSE_SIGN;
                        break;
                    case "VERIFY":
                        flags |= KeyProperties.PURPOSE_VERIFY;
                        break;
                }
            }
        }

        return String.valueOf(flags);
    }
}

