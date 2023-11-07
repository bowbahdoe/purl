package dev.mccue.purl;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * String helpers.
 *
 * @since 1.1.0
 */
final class MoreStrings
{
    private MoreStrings() {
        // empty
    }

    /**
     * Convert given value to lower-case.
     */
    static String lowerCase(final String value) {
        return value.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Convert given list of values to lower-case.
     */
    @Nullable
    static List<String> lowerCase(@Nullable final List<String> values) {
        if (values != null) {
            List<String> result = new ArrayList<>(values.size());
            for (String value : values) {
                result.add(lowerCase(value));
            }
            return result;
        }
        return null;
    }

    /**
     * Check if given string is {@code null}, empty {@literal ""} or only contains whitespace.
     *
     * @since 1.2.0
     */
    static boolean isBlank(@Nullable final String value) {
        return value != null && value.trim().isEmpty();
    }
}