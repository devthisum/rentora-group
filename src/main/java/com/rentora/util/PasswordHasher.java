package com.rentora.util;

import org.mindrot.jbcrypt.BCrypt;

/** Utility for secure password hashing and verification (BCrypt, cost factor 12). */
public final class PasswordHasher {

    private PasswordHasher() { }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // jBCrypt 0.4 only recognizes the "$2a$" hash prefix. Hashes generated
            // by other libraries (e.g. Python's bcrypt, which defaults to "$2b$")
            // will throw here instead of returning false. Treat that the same as
            // "password does not match" rather than letting it crash the request.
            return false;
        }
    }
}
