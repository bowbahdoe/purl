package dev.mccue.purl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;
import static dev.mccue.purl.PackageUrlParser.parseNamespace;
import static dev.mccue.purl.PackageUrlParser.parseQualifiers;
import static dev.mccue.purl.PackageUrlParser.parseSubpath;
import static dev.mccue.purl.PackageUrlValidator.validateName;
import static dev.mccue.purl.PackageUrlValidator.validateNamespace;
import static dev.mccue.purl.PackageUrlValidator.validateQualifiers;
import static dev.mccue.purl.PackageUrlValidator.validateSubpath;
import static dev.mccue.purl.PackageUrlValidator.validateType;
import static dev.mccue.purl.PackageUrlValidator.validateVersion;

/**
 * {@link PackageUrl} builder.
 *
 * @since 1.1.0
 */
public final class PackageUrlBuilder
{
    private boolean typeSpecificTransformations = true;

    private String type;

    private List<String> namespace;

    private String name;

    private String version;

    private Map<String, String> qualifiers;

    private List<String> subpath;

    public PackageUrlBuilder from(final PackageUrl purl) {
        requireNonNull(purl);
        this.type = purl.getType();
        if (purl.getNamespace() != null) {
            this.namespace = new ArrayList<>(purl.getNamespace());
        }
        this.name = purl.getName();
        this.version = purl.getVersion();
        if (purl.getQualifiers() != null) {
            this.qualifiers = new LinkedHashMap<>(purl.getQualifiers());
        }
        if (purl.getSubpath() != null) {
            this.subpath = new ArrayList<>(purl.getSubpath());
        }
        return this;
    }

    /**
     * If enabled then the builder will make the changes defined in the Package URL spec to the namespace and name for specific types.
     *
     * By default this is enabled to maintain compliance with the spec.
     *
     * @since 1.2.0
     */
    public PackageUrlBuilder typeSpecificTransformations(boolean enable) {
        this.typeSpecificTransformations = enable;
        return this;
    }

    public PackageUrlBuilder type(final String type) {
        this.type = type;
        return this;
    }

    public PackageUrlBuilder namespace(final List<String> namespace) {
        this.namespace = namespace;
        return this;
    }

    public PackageUrlBuilder namespace(final String namespace) {
        return namespace(parseNamespace(namespace));
    }

    public PackageUrlBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public PackageUrlBuilder version(final String version) {
        this.version = version;
        return this;
    }

    private Map<String, String> getQualifiers() {
        if (qualifiers == null) {
            qualifiers = new LinkedHashMap<>();
        }
        return qualifiers;
    }

    public PackageUrlBuilder qualifiers(final Map<String, String> qualifiers) {
        if (qualifiers != null) {
            for (Entry<String, String> entry : qualifiers.entrySet()) {
                qualifier(entry.getKey(), entry.getValue());
            }
        }
        else {
            this.qualifiers = null;
        }
        return this;
    }

    public PackageUrlBuilder qualifiers(final String qualifiers) {
        return qualifiers(parseQualifiers(qualifiers));
    }

    public PackageUrlBuilder qualifier(final String key, final String value) {
        requireNonNull(key);
        requireNonNull(value);
        getQualifiers().put(key, value);
        return this;
    }

    public PackageUrlBuilder subpath(final List<String> subpath) {
        this.subpath = subpath;
        return this;
    }

    /**
     * Parse subpath from value.
     */
    public PackageUrlBuilder subpath(final String subpath) {
        return subpath(parseSubpath(subpath));
    }

    /**
     * Build {@link PackageUrl}.
     *
     * At minimal {@link #type} and {@link #name} must be specified.
     */
    public PackageUrl build() {
        return buildAndValidate(true);
    }

    /**
     * Build and optionally validate.
     *
     * Non-validate case is for parsed usage only.
     *
     * @since 1.0.1
     */
    PackageUrl buildAndValidate(final boolean validate) {
        if (validate) {
            validateType(type);
            validateNamespace(namespace);
            validateName(name);
            validateVersion(version);
            validateQualifiers(qualifiers);
            validateSubpath(subpath);
        }

        // FIXME: need to have some per-type transformation; which is unfortunate but spec requires some special handling per-type
        // FIXME: various type-specific transformation required by specification; very problematic
        // FIXME: https://github.com/package-url/purl-spec/issues/38

        List<String> correctedNamespace = namespace;
        String correctedName = name;
        if (typeSpecificTransformations) {
            switch (type) {
                case "github":
                case "bitbucket":
                    correctedNamespace = MoreStrings.lowerCase(namespace);
                    correctedName = MoreStrings.lowerCase(name);
                    break;

                case "pypi":
                    correctedName = name.replace('_', '-');
                    correctedName = MoreStrings.lowerCase(correctedName);
                    break;
            }
        }

        SortedMap<String, String> correctedQualifiers = null;
        if (qualifiers != null) {
            correctedQualifiers = new TreeMap<>();
            for (Entry<String, String> entry : qualifiers.entrySet()) {
                String key = MoreStrings.lowerCase(entry.getKey());
                String value = entry.getValue();
                if (MoreStrings.isBlank(value)) {
                    continue;
                }
                correctedQualifiers.put(key, value);
            }
            if (correctedQualifiers.isEmpty()) {
                correctedQualifiers = null;
            }
        }

        return new PackageUrl(type, correctedNamespace, correctedName, version, correctedQualifiers, subpath);
    }
}