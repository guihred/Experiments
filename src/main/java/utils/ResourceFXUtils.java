package utils;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javax.swing.filechooser.FileSystemView;
import org.slf4j.Logger;

/**
 * @author Note
 *
 *         Easy Methods to get a resource from the resources directory.
 *
 */
public final class ResourceFXUtils {

    private static final Logger LOGGER = HasLogging.log();
    private static final List<String> JAVA_KEYWORDS = Arrays.asList("abstract", "continue", "for", "new", "switch",
            "assert", "default", "false", "true", "goto", "package", "synchronized", "boolean", "do", "if", "private",
            "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws",
            "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char",
            "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const",
            "float", "native", "super", "while");

    private ResourceFXUtils() {
    }

    public static double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }

    public static int clamp(int value, int min, int max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }

    public static BasicFileAttributes computeAttributes(File v) {
        return SupplierEx.get(() -> Files.readAttributes(v.toPath(), BasicFileAttributes.class));
    }

    public static URL convertToURL(File arquivo) {
        return SupplierEx.get(() -> arquivo.toURI().toURL());
    }

    public static Image createImage(double size1, float[][] noise) {
        int width = (int) size1;
        int height = (int) size1;

        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float value = noise[x][y];
                double gray = normalizeValue(value, -.5, .5, 0., 1.);
                gray = clamp(gray, 0, 1);
                Color color = Color.RED.interpolate(Color.YELLOW, gray);
                pw.setColor(x, y, color);
            }
        }
        return wr;
    }

    public static List<Path> getFirstFileMatch(File dir, PredicateEx<Path> other) {
        return SupplierEx.get(() -> {
            if (!dir.exists()) {
                return Collections.emptyList();
            }
            try (Stream<Path> walk = Files.walk(dir.toPath(), 20)) {
                return walk.filter(PredicateEx.makeTest(other)).collect(Collectors.toList());
            }
        }, Collections.emptyList());
    }

    public static Path getFirstPathByExtension(File dir, String... other) {
        return getPathByExtension(dir, other).stream().findFirst().orElse(null);
    }

    public static List<String> getJavaKeywords() {
        return JAVA_KEYWORDS;
    }

    public static File getOutFile() {
        File parentFile = toFile("alice.txt").getParentFile();
        File file = new File(parentFile, "out");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getOutFile(String out) {
        File parentFile = toFile("alice.txt").getParentFile();
        File file = new File(parentFile, "out");
        if (!file.exists()) {
            file.mkdir();
        }
        return new File(file, out);
    }

    public static List<Path> getPathByExtension(File dir, String... other) {
        return SupplierEx.get(() -> {
            if (!dir.exists()) {
                return Collections.emptyList();
            }
            try (Stream<Path> walk = walkDepth(walkReadable(dir.toPath()))) {
                return walk
                        .filter(PredicateEx.makeTest(e -> Stream.of(other).anyMatch(ex -> e.toString().endsWith(ex))))
                        .collect(Collectors.toList());
            }
        }, Collections.emptyList());
    }

    public static Path getRandomPathByExtension(File dir, String... other) {
        List<Path> pathByExtension = getPathByExtension(dir, other);
        if (pathByExtension.isEmpty()) {
            return null;
        }

        return pathByExtension.get(new Random().nextInt(pathByExtension.size()));
    }

    public static File getUserFolder(String dir) {
        String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
        return new File(new File(path).getParentFile(), dir);
    }

    public static int getYearCreation(Path path) {
        try {
            BasicFileAttributes readAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            Instant l = readAttributes.creationTime().toInstant();
            return ZonedDateTime.ofInstant(l, ZoneId.systemDefault()).getYear();
        } catch (IOException e) {
            LOGGER.trace("", e);
            return ZonedDateTime.now().getYear();
        }
    }

    public static Mesh importStlMesh(File file) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
    }

    public static Mesh importStlMesh(String arquivo) {
        File file = new File(arquivo);
        return importStlMesh(file);
    }

    public static Mesh importStlMesh(URL file) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
    }

    public static void initializeFX() {
        Platform.setImplicitExit(false);
        new JFXPanel().toString();
    }

    public static void runOnFiles(File userFolder, ConsumerEx<File> run) {

        try (Stream<Path> s = Files.list(userFolder.toPath())) {
            s.forEach(ConsumerEx.makeConsumer(e -> run.accept(e.toFile())));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static String toExternalForm(String arquivo) {
        return SupplierEx.remap(() -> getClassLoader().getResource(arquivo).toExternalForm(),
                "ERRO FILE \"" + arquivo + "\"");
    }

    public static File toFile(String arquivo) {
        return new File(toFullPath(arquivo));
    }

    public static String toFullPath(String arquivo) {
        try {
            return URLDecoder.decode(getClassLoader().getResource(arquivo).getFile(), "UTF-8");
        } catch (Exception e) {
            LOGGER.trace("File Error:" + arquivo, e);
            return SupplierEx.get(() -> URLDecoder.decode(new File(arquivo).getAbsolutePath(), "UTF-8"));
        }
    }

    public static Path toPath(String arquivo) {
        return new File(getClassLoader().getResource(arquivo).getFile()).toPath();
    }

    public static InputStream toStream(String arquivo) {
        return getClassLoader().getResourceAsStream(arquivo);
    }

    public static URI toURI(String arquivo) {
        return new File(getClassLoader().getResource(arquivo).getFile()).toURI();
    }

    public static URL toURL(String arquivo) {
        return getClassLoader().getResource(arquivo);
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    private static Stream<Path> walkDepth(Stream<Path> walk) {
        return walkDepth(walk, 5);
    }

    private static Stream<Path> walkDepth(Stream<Path> walk, int depth) {
        Stream<Path> walk0 = walk;
        for (int i = 0; i < depth; i++) {
            walk0 = walk0.flatMap(ResourceFXUtils::walkReadable);
        }
        return walk0.distinct();
    }

    private static Stream<Path> walkReadable(Path p) {
        if (!Files.isReadable(p)) {
            return Stream.empty();
        }
        Stream<Path> of = Stream.of(p);
        if (!p.toFile().isDirectory()) {
            return of;
        }
        return SupplierEx.get(() -> Files.list(p), of);
    }

}
