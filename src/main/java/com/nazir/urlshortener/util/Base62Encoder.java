package com.nazir.urlshortener.util;

import java.security.SecureRandom;

/**
 * Base62 encoder for URL slug generation.
 * <p>
 * Alphabet: a-z, A-Z, 0-9 (62 characters).
 * 7 chars = 62^7 = 3.5 trillion combinations — practically collision-free.
 * </p>
 * <p>
 * Provides two strategies:
 * <ul>
 *   <li>{@link #encode(long)} — deterministic encoding of a numeric ID</li>
 *   <li>{@link #generateRandom(int)} — cryptographically random string</li>
 * </ul>
 */
public final class Base62Encoder {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length(); // 62
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Encoder() {
        // Utility class — no instantiation
    }

    /**
     * Encode a positive long number to a Base62 string.
     *
     * @param number non-negative number to encode
     * @return Base62 encoded string (e.g., 123456789 → "8M0kX")
     * @throws IllegalArgumentException if number is negative
     */
    public static String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative: " + number);
        }
        if (number == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(ALPHABET.charAt((int) (number % BASE)));
            number /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Decode a Base62 string back to a long number.
     *
     * @param encoded Base62 string
     * @return decoded long value
     * @throws IllegalArgumentException if string contains invalid characters
     */
    public static long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Encoded string must not be null or empty");
        }

        long result = 0;
        for (char c : encoded.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE + index;
        }
        return result;
    }

    /**
     * Generate a cryptographically random Base62 string of specified length.
     * <p>
     * Uses {@link SecureRandom} for unpredictable output.
     * </p>
     *
     * @param length desired string length (e.g., 7)
     * @return random Base62 string
     * @throws IllegalArgumentException if length is not positive
     */
    public static String generateRandom(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive: " + length);
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(BASE)));
        }
        return sb.toString();
    }

    /**
     * Check if a string contains only valid Base62 characters.
     *
     * @param input string to check
     * @return true if all characters are in the Base62 alphabet
     */
    public static boolean isValidBase62(String input) {
        if (input == null || input.isEmpty()) return false;
        for (char c : input.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) return false;
        }
        return true;
    }
}
