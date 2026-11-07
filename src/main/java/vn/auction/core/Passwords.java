package vn.auction.core;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
    private Passwords() {}

    public static String hash(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            throw new AuctionException("Password must have 8-128 characters");
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + derive(password, salt);
    }

    private static String derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210000, 256);
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                                    .generateSecret(spec)
                                    .getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean matches(String password, String hash) {
        if (password == null || password.length() > 128) {
            return false;
        }
        String[] parts = hash.split(":");
        return MessageDigest.isEqual(
                derive(password, Base64.getDecoder().decode(parts[0]))
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8),
                parts[1].getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
