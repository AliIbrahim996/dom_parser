package com.aliibrahim.domparser.core.security;

/** Thrown when XML encryption or decryption fails, for example when the key is wrong. */
public final class EncryptionException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message a description people can read
     * @param cause   the underlying failure
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
