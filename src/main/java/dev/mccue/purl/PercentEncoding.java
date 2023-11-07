package dev.mccue.purl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

/**
 * Percent encoding helper.
 *
 * Handles specific wrinkles related to Package URL specification.
 *
 * Specification indicates that {@code :} and {@code /} are "unambiguous unencoded everywhere".
 *
 * Specification; via https://en.wikipedia.org/wiki/Percent-encoding; indicates % encoding for space.
 *
 * @since 1.0.0
 */
final class PercentEncoding
{
    private PercentEncoding() {
        // empty
    }

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    //
    // Name has some wrinkles and non-clarity about the specification for encoding.  As the {@code /} is used
    // as a separator but its also "unambiguous unencoded everywhere".
    //
    // Bottom line: we do encode {@code /} when in a name, we do not when not in a name.
    //

    public static String encode(final String value) {
        String encoded = encodeName(value);
        encoded = simpleReplace(encoded, "%2F", "/");
        return encoded;
    }

    public static String encodeName(final String value) {
        requireNonNull(value);
        try {
            String encoded = URLEncoder.encode(value, UTF_8);
            encoded = simpleReplace(encoded, "+", "%20");
            // despite the fact that ":" is a reserved character in RFC 3986, we do not encode it for purl.
            encoded = simpleReplace(encoded, "%3A", ":");
            // "~" is an unreserved character in RFC 3986.
            encoded = simpleReplace(encoded, "%7E", "~");
            return encoded;
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String encodeVersion(final String value) {
        return encode(value);
    }

    public static String encodeSegment(final String value) {
        // TODO: this may need to have same treatment as name?
        return encode(value);
    }

    public static String encodeQualifierValue(final String value) {
        return encode(value);
    }

    public static String decode(final String value) {
        requireNonNull(value);
        try {
            return URLDecoder.decode(value, UTF_8);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A simpler version of String.replace() that does not involve regexp's and Patterns.
     *
     * Modeled after a similar method in org.codehaus.plexus's StrungUtils.
     * https://github.com/codehaus-plexus/plexus-utils/blob/master/src/main/java/org/codehaus/plexus/util/StringUtils.java#L848
     *
     * @since 1.2.0
     */
    // @VisibleForTesting
    static String simpleReplace(final String text, final String target, final String replacement) {
        int start = 0;
        int index = text.indexOf(target, start);
        if (index == -1) {
            return text;
        }

        StringBuilder stringBuilder = new StringBuilder(text.length());
        do {
            stringBuilder
                    .append(text, start, index)
                    .append(replacement);
            start = index + target.length();
            index = text.indexOf(target, start);
        } while (index != -1);
        stringBuilder.append(text, start, text.length());

        return stringBuilder.toString();
    }
}
