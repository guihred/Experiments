package utils;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import javafx.scene.shape.Mesh;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.lang3.StringUtils;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

/**
 * @author Note Easy Methods to get a resource from the resources directory.
 */
public final class ResourceFXUtils {

    private ResourceFXUtils() {
    }

    public static BasicFileAttributes computeAttributes(File v) {
        return SupplierEx.get(() -> Files.readAttributes(v.toPath(), BasicFileAttributes.class));
    }

    public static URL convertToURL(File arquivo) {
        return SupplierEx.get(() -> arquivo.toURI().toURL());
    }

    public static File getOutFile() {
        File parentFile = toFile("alice.txt").getParentFile().getParentFile();
        File file = new File(parentFile, "out");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getOutFile(String out) {
        File file = getOutFile();
        String replaceAll = out.replaceAll("[:\\{\\}\"\n]+", "_");
        String extension = replaceAll.replaceAll(".+(\\.\\w+)$", "$1");
        String abbreviate = StringUtils.abbreviate(replaceAll.replace(extension, ""), 90);
        File file2 = new File(file, abbreviate + extension);
        if (out.contains("/") && !file2.getParentFile().exists()) {
            file2.getParentFile().mkdir();
        }
        return file2;
    }

    public static File getUserFolder(String dir) {
        String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
        return new File(new File(path).getParentFile(), dir);
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



    public static void runOnFiles(File userFolder, ConsumerEx<File> run) {
        RunnableEx.run(() -> {
            try (Stream<Path> s = Files.list(userFolder.toPath())) {
                s.forEach(ConsumerEx.makeConsumer(e -> run.accept(e.toFile())));
            }
        });
    }

    public static String toExternalForm(String arquivo) {
        return SupplierEx.remap(() -> getClassLoader().getResource(arquivo).toExternalForm(),
                "ERRO FILE \"" + arquivo + "\"");
    }

    public static File toFile(String arquivo) {
        return new File(toFullPath(arquivo));
    }

    public static String toFullPath(String arquivo) {
        return SupplierEx.getFirst(() -> URLDecoder.decode(getClassLoader().getResource(arquivo).getFile(), "UTF-8"),
                () -> URLDecoder.decode(new File(arquivo).getAbsolutePath(), "UTF-8"));
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

}
