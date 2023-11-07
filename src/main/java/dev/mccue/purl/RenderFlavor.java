package dev.mccue.purl;

/**
 * How to render the Package-URL.
 *
 * @since 1.1.0
 */
public enum RenderFlavor
{
    /**
     * Render with {@link PackageUrl#SCHEME} (default).
     */
    SCHEME,

    /**
     * Render with-out {@link PackageUrl#SCHEME}.
     */
    SCHEMELESS;

    // TODO: Make a scoped value
    private static final RenderFlavor _default = SCHEME;

    public static RenderFlavor getDefault() {
        return _default;
    }
}
