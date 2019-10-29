package ethical.hacker.ssh;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.KeyPair;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.assertj.core.api.exception.RuntimeIOException;
import utils.SupplierEx;

final class CommonTestSupportUtils {
    /**
     * URL/URI scheme that refers to a file
     */
    private static final String FILE_URL_SCHEME = "file";
    /**
     * Prefix used in URL(s) that reference a file resource
     */
    private static final String FILE_URL_PREFIX = FILE_URL_SCHEME + ":";

    /**
     * Separator used in URL(s) that reference a resource inside a JAR to denote the
     * sub-path inside the JAR
     */
    private static final char RESOURCE_SUBPATH_SEPARATOR = '!';

    /**
     * URL/URI scheme that refers to a JAR
     */
    private static final String JAR_URL_SCHEME = "jar";

    /**
     * Prefix used in URL(s) that reference a resource inside a JAR
     */
    private static final String JAR_URL_PREFIX = JAR_URL_SCHEME + ":";

    /**
     * Suffix of compile Java class files
     */
    private static final String CLASS_FILE_SUFFIX = ".class";

    private static final List<String> TARGET_FOLDER_NAMES = // NOTE: order is important
        Collections.unmodifiableList(Arrays.asList("target" /* Maven */, "build" /* Gradle */));

    private static final String DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM = KeyUtils.RSA_ALGORITHM;
    // uses a cached instance to avoid re-creating the keys as it is a
    // time-consuming effort
    private static final AtomicReference<KeyPairProvider> KEYPAIR_PROVIDER_HOLDER = new AtomicReference<>();

    private CommonTestSupportUtils() {
    }

    public static KeyPairProvider createTestHostKeyProvider(Class<?> anchor) {
        KeyPairProvider provider = KEYPAIR_PROVIDER_HOLDER.get();
        if (provider != null) {
            return provider;
        }

        Path targetFolder = Objects.requireNonNull(CommonTestSupportUtils.detectTargetFolder(anchor),
            "Failed to detect target folder");
        Path file = targetFolder.resolve("hostkey." + DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM.toLowerCase());
        provider = createTestHostKeyProvider(file);

        KeyPairProvider prev = KEYPAIR_PROVIDER_HOLDER.getAndSet(provider);
        if (prev != null) { // check if somebody else beat us to it
            return prev;
        }
		return provider;
    }

    /**
     * @param path A URL path value - ignored if {@code null}/empty
     * @return The path after stripping any trailing '/' provided the path is not
     *         '/' itself
     */
    private static String adjustURLPathValue(String path) {
        final int pathLen = path.length();
        if (pathLen <= 1 || path.charAt(pathLen - 1) != '/') {
            return path;
        }
        return path.substring(0, pathLen - 1);
    }

    private static KeyPairProvider createTestHostKeyProvider(Path path) {
        SimpleGeneratorHostKeyProvider keyProvider = new SimpleGeneratorHostKeyProvider();
        keyProvider.setPath(Objects.requireNonNull(path, "No path"));
        keyProvider.setAlgorithm(DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM);
        return validateKeyPairProvider(keyProvider);
    }

    /**
     * @param anchor An anchor {@link Class} whose container we want to use as the
     *               starting point for the &quot;target&quot; folder lookup up the
     *               hierarchy
     * @return The &quot;target&quot; <U>folder</U> - {@code null} if not found
     * @see #detectTargetFolder(Path)
     */
    private static Path detectTargetFolder(Class<?> anchor) {
        return detectTargetFolder(getClassContainerLocationPath(anchor));
    }

    /**
     * @param anchorFile An anchor {@link Path} we want to use as the starting point
     *                   for the &quot;target&quot; or &quot;build&quot; folder
     *                   lookup up the hierarchy
     * @return The &quot;target&quot; <U>folder</U> - {@code null} if not found
     */
    private static Path detectTargetFolder(Path anchorFile) {
        for (Path file = anchorFile; file != null; file = file.getParent()) {
            if (!file.toFile().isDirectory()) {
                continue;
            }

            String name = Objects.toString(file.getFileName(), "");
            if (TARGET_FOLDER_NAMES.contains(name)) {
                return file;
            }
        }

        return null;
    }

    /**
     * @param clazz The request {@link Class}
     * @return A {@link URL} to the location of the <code>.class</code> file -
     *         {@code null} if location could not be resolved
     */
    private static URL getClassBytesURL(Class<?> clazz) {
        String className = clazz.getName();
        int sepPos = className.indexOf('$');
        // if this is an internal class, then need to use its parent as well
        if (sepPos > 0) {
            sepPos = className.lastIndexOf('.');
            if (sepPos > 0) {
                className = className.substring(sepPos + 1);
            }
        } else {
            className = clazz.getSimpleName();
        }

        return clazz.getResource(className + CLASS_FILE_SUFFIX);
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link Path} of the location of the class bytes container - e.g.,
     *         the root folder, the containing JAR, etc.. Returns {@code null} if
     *         location could not be resolved
     * @throws IllegalArgumentException If location is not a valid {@link Path}
     *                                  location
     * @see #getClassContainerLocationURI(Class)
     * @see #toPathSource(URI)
     */
    private static Path getClassContainerLocationPath(Class<?> clazz) {
        return SupplierEx.makeSupplier(() -> toPathSource(getClassContainerLocationURI(clazz)), e -> {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }).get();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container - e.g.,
     *         the root folder, the containing JAR, etc.. Returns {@code null} if
     *         location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    private static URI getClassContainerLocationURI(Class<?> clazz) throws URISyntaxException {
        URL url = getClassContainerLocationURL(clazz);
        return url == null ? null : url.toURI();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container - e.g.,
     *         the root folder, the containing JAR, etc.. Returns {@code null} if
     *         location could not be resolved
     */
    private static URL getClassContainerLocationURL(Class<?> clazz) {
        ProtectionDomain pd = clazz.getProtectionDomain();
        CodeSource cs = pd == null ? null : pd.getCodeSource();
        URL url = cs == null ? null : cs.getLocation();
        if (url == null) {
            url = getClassBytesURL(clazz);
            if (url == null) {
                return null;
            }

            String srcForm = getURLSource(url);
            if (GenericUtils.isEmpty(srcForm)) {
                return null;
            }

            url = SupplierEx.get(() -> new URL(srcForm));
        }

        return url;
    }

    /**
     * @param externalForm The {@link URL#toExternalForm()} string - ignored if
     *                     {@code null}/empty
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and any
     *         sub-resource are stripped
     */
    private static String getURLSource(String externalForm) {
        String url = externalForm;
        if (GenericUtils.isEmpty(url)) {
            return url;
        }

        url = stripJarURLPrefix(externalForm);
        if (GenericUtils.isEmpty(url)) {
            return url;
        }

        int sepPos = url.indexOf(RESOURCE_SUBPATH_SEPARATOR);
        return adjustURLPathValue(sepPos < 0 ? url : url.substring(0, sepPos));
    }

    /**
     * @param uri The {@link URI} value - ignored if {@code null}
     * @return The URI(s) source path where {@link #JAR_URL_PREFIX} and any
     *         sub-resource are stripped
     * @see #getURLSource(String)
     */
    private static String getURLSource(URI uri) {
        return getURLSource(uri.toString());
    }

    /**
     * @param url The {@link URL} value - ignored if {@code null}
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and any
     *         sub-resource are stripped
     * @see #getURLSource(String)
     */
    private static String getURLSource(URL url) {
        return getURLSource(url.toExternalForm());
    }

    private static String stripJarURLPrefix(String externalForm) {
        String url = externalForm;
        if (GenericUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith(JAR_URL_PREFIX)) {
            return url.substring(JAR_URL_PREFIX.length());
        }

        return url;
    }

    /**
     * Converts a {@link URI} that may refer to an internal resource to a
     * {@link Path} representing is &quot;source&quot; container (e.g., if it is a
     * resource in a JAR, then the result is the JAR's path)
     *
     * @param uri The {@link URI} - ignored if {@code null}
     * @return The matching {@link Path}
     * @throws MalformedURLException If source URI does not refer to a file location
     * @see #getURLSource(URI)
     */
    private static Path toPathSource(URI uri) {
        String src = getURLSource(uri);
        if (GenericUtils.isEmpty(src)) {
            return null;
        }

        if (!src.startsWith(FILE_URL_PREFIX)) {
            throw new RuntimeIOException("toFileSource(" + src + ") not a '" + FILE_URL_SCHEME + "' scheme");
        }
        return SupplierEx.remap(() -> Paths.get(new URI(src)),
            "toFileSource(" + src + ")" + " cannot convert to URI ");

    }

    private static <P extends KeyIdentityProvider> P validateKeyPairProvider(P provider) {
        Objects.requireNonNull(provider, "No provider");
        // get the I/O out of the way
        Iterable<KeyPair> keys = SupplierEx
            .get(() -> Objects.requireNonNull(provider.loadKeys(null), "No keys loaded"));

        if (keys instanceof Collection<?>) {
            ValidateUtils.checkNotNullAndNotEmpty((Collection<?>) keys, "Empty keys loaded");
        }

        return provider;
    }
}