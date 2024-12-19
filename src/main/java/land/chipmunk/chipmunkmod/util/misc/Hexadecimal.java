package land.chipmunk.chipmunkmod.util.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;

public interface Hexadecimal {
    /** Hexadecimal digits lookup table for faster conversion */
    char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * Encodes a single byte to a two-character hexadecimal string.
     * @param b The byte to encode
     * @return A two-character hexadecimal string
     */
    static @NotNull String encode(byte b) {
        return new String(new char[] {
                HEX_CHARS[(b >> 4) & 0xF],
                HEX_CHARS[b & 0xF]
        });
    }

    /**
     * Encodes a byte array to a hexadecimal string.
     * @param array The byte array to encode
     * @return The hexadecimal string representation
     * @throws IllegalArgumentException if array is null
     */
    static @NotNull Object encode(byte @NotNull [] array) {
        char[] hexChars = new char[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            int v = array[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0xF];
        }
        return new String(hexChars);
    }

    /**
     * Encodes a string to hexadecimal using UTF-8 encoding.
     * @param text The string to encode
     * @return The hexadecimal representation of the string
     * @throws IllegalArgumentException if text is null
     */
    static @NotNull String encodeString(@NotNull String text) {
        return (String) encode(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a hexadecimal string to a byte array.
     * @param hex The hexadecimal string to decode
     * @return The decoded byte array, or null if the input is invalid
     */
    static byte @Nullable [] decode(@Nullable String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }

        // Remove any spaces or non-hex characters
        hex = hex.replaceAll("[^0-9A-Fa-f]", "");

        // Check if we have a valid hex string (must be even length)
        if (hex.length() % 2 != 0) {
            return null;
        }

        byte[] result = new byte[hex.length() / 2];
        try {
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) Integer.parseInt(
                        hex.substring(i * 2, i * 2 + 2),
                        16
                );
            }
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Decodes a hexadecimal string to a UTF-8 string.
     * @param hex The hexadecimal string to decode
     * @return The decoded string, or null if the input is invalid
     */
    static @Nullable String decodeString(@Nullable String hex) {
        byte[] bytes = decode(hex);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Checks if a string is a valid hexadecimal string.
     * @param hex The string to check
     * @return true if the string is valid hexadecimal, false otherwise
     */
    static boolean isValid(@Nullable String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }
        return hex.matches("^[0-9A-Fa-f]+$") && hex.length() % 2 == 0;
    }

    /**
     * Formats a hexadecimal string with specified separator every n characters.
     * @param hex The hexadecimal string to format
     * @param every Number of characters between separators
     * @param separator The separator to use
     * @return The formatted string, or null if input is invalid
     */
    static @Nullable String format(@Nullable String hex, int every, char separator) {
        if (!isValid(hex) || every <= 0) {
            return null;
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < hex.length(); i++) {
            if (i > 0 && i % every == 0) {
                formatted.append(separator);
            }
            formatted.append(hex.charAt(i));
        }
        return formatted.toString();
    }
}
