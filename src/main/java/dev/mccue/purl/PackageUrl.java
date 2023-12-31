package dev.mccue.purl;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;

import org.jspecify.annotations.Nullable;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.Objects.requireNonNull;
import static dev.mccue.purl.PercentEncoding.encodeName;
import static dev.mccue.purl.PercentEncoding.encodeQualifierValue;
import static dev.mccue.purl.PercentEncoding.encodeSegment;
import static dev.mccue.purl.PercentEncoding.encodeVersion;

/**
 * <a href="https://github.com/package-url/purl-spec">Package URL</a>.
 *
 * @see PackageUrlBuilder
 * @since 1.0.0
 */
public final class PackageUrl
        implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Standard URI scheme for Package-URL.
     *
     * @since 1.1.0
     */
    public static final String SCHEME = "pkg";

    private final String type;

    @Nullable
    private final List<String> namespace;

    private final String name;

    @Nullable
    private final String version;

    /**
     * Sorted map of qualifiers (lexicographically).
     *
     * See: https://github.com/package-url/purl-spec/issues/51
     */
    @Nullable
    private final SortedMap<String, String> qualifiers;

    @Nullable
    private final List<String> subpath;

    /**
     * Values should have already been validated via {@link PackageUrlBuilder} and {@link PackageUrlValidator}.
     */
    PackageUrl(final String type,
               @Nullable final List<String> namespace,
               final String name,
               @Nullable final String version,
               @Nullable final SortedMap<String, String> qualifiers,
               @Nullable final List<String> subpath)
    {
        this.type = requireNonNull(type);
        this.namespace = namespace != null ? unmodifiableList(namespace) : null;
        this.name = requireNonNull(name);
        this.version = version;
        this.qualifiers = qualifiers != null ? unmodifiableSortedMap(qualifiers) : null;
        this.subpath = subpath != null ? unmodifiableList(subpath) : null;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public List<String> getNamespace() {
        return namespace;
    }

    /**
     * Return {@link #namespace} as a string representation (unencoded segments).
     */
    @Nullable
    public String getNamespaceAsString() {
        if (namespace != null && !namespace.isEmpty()) {
            return renderSegments(new StringBuilder(), namespace, false).toString();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @Nullable
    public SortedMap<String, String> getQualifiers() {
        return qualifiers;
    }

    @Nullable
    public List<String> getSubpath() {
        return subpath;
    }

    /**
     * Return {@link #subpath} as a string representation (unencoded segments).
     */
    @Nullable
    public String getSubpathAsString() {
        if (subpath != null && !subpath.isEmpty()) {
            return renderSegments(new StringBuilder(), subpath, false).toString();
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageUrl that = (PackageUrl) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(namespace, that.namespace) &&
               Objects.equals(name, that.name) &&
               Objects.equals(version, that.version) &&
               Objects.equals(qualifiers, that.qualifiers) &&
               Objects.equals(subpath, that.subpath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, namespace, name, version, qualifiers, subpath);
    }

    /**
     * Returns a json-like representation of structure; Exposed for tests.
     */
    String explain() {
        return "{type='" + type + '\'' +
               ", namespace=" + namespace +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", qualifiers=" + qualifiers +
               ", subpath=" + subpath +
               '}';
    }

    /**
     * Convert to a builder for mutation.
     *
     * @see PackageUrlBuilder#from(PackageUrl)
     */
    public PackageUrlBuilder asBuilder() {
        return new PackageUrlBuilder().from(this);
    }

    /**
     * Convert to a URI.
     *
     * @since 1.1.0
     */
    public URI toUri() {
        return URI.create(toString());
    }

    /**
     * Convert to canonical string representation with {@link RenderFlavor#getDefault() default flavor}.
     */
    @Override
    public String toString() {
        return toString(RenderFlavor.getDefault());
    }

    /**
     * Convert to canonical string representation with given rendering flavor.
     */
    public String toString(final RenderFlavor flavor) {
        requireNonNull(flavor);

        StringBuilder buff = new StringBuilder();

        if (flavor == RenderFlavor.SCHEME) {
            buff.append(SCHEME).append(':');
        }

        buff.append(MoreStrings.lowerCase(type));

        if (flavor == RenderFlavor.SCHEME) {
            buff.append('/');
        }
        else {
            buff.append(':');
        }

        if (namespace != null && !namespace.isEmpty()) {
            renderSegments(buff, namespace, true);
            buff.append('/');
        }

        buff.append(encodeName(name));

        if (version != null) {
            buff.append('@').append(encodeVersion(version));
        }

        if (qualifiers != null && !qualifiers.isEmpty()) {
            buff.append('?');

            String separator = "";
            for (Entry<String, String> entry : qualifiers.entrySet()) {
                buff.append(separator).append(entry.getKey()).append('=').append(encodeQualifierValue(entry.getValue()));
                separator = "&";
            }
        }

        if (subpath != null && !subpath.isEmpty()) {
            buff.append('#');
            renderSegments(buff, subpath, true);
        }

        return buff.toString();
    }

    /**
     * Render segments to buffer.
     */
    private static StringBuilder renderSegments(final StringBuilder buff,
                                                final List<String> segments,
                                                final boolean encode)
    {
        Iterator<String> iter = segments.iterator();
        while (iter.hasNext()) {
            String segment = iter.next();
            if (encode) {
                segment = encodeSegment(segment);
            }
            buff.append(segment);
            if (iter.hasNext()) {
                buff.append('/');
            }
        }
        return buff;
    }

    /**
     * Parse package-url from given value.
     *
     * Shortcut to {@code parser().parse(value)}
     */
    public static PackageUrl parse(final String value) {
        return parser().parse(value);
    }

    /**
     * Helper to create a parser
     *
     * @since 1.2.0
     */
    public static PackageUrlParser parser() {
        return new PackageUrlParser();
    }

    /**
     * Helper to create a builder.
     */
    public static PackageUrlBuilder builder() {
        return new PackageUrlBuilder();
    }
}