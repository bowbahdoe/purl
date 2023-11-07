package dev.mccue.purl;

/**
 * Throw when segmented property contains and illegal value.
 *
 * @since 1.1.0
 */
public final class IllegalSegmentContentException
        extends InvalidException
{
    IllegalSegmentContentException(final String content, final String value) {
        super("Illegal segment content: " + content + " in: " + value);
    }
}