package extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import utils.FileTreeWalker;
import utils.ex.HasLogging;

public final class HashVerifier {
    private static final Logger LOG = HasLogging.log();

    private HashVerifier() {
    }

    public static String getMD5Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(is);
        }
    }

    public static String getSha1Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.sha1Hex(is);
        }
    }

    public static String getSha256Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.sha256Hex(is);
        }
    }

    public static String getSha256Hash(String data) {
        return DigestUtils.sha256Hex(data);
    }


    public static List<Entry<Path, Path>> listNotRepeatedFiles(File file,File file2) {
        ObservableList<Entry<Path, Path>> notRepeatedEntries = FXCollections.observableArrayList();
        Map<String, Path> fileMap = new ConcurrentHashMap<>();
        
        FileTreeWalker.getPathByExtensionAsync(file, path -> addToNotRepeated(notRepeatedEntries, fileMap, path),
                ".mp3");
        FileTreeWalker.getPathByExtensionAsync(file2, path -> addToNotRepeated(notRepeatedEntries, fileMap, path),
                ".mp3");
        return notRepeatedEntries;
    }


    public static List<Entry<Path, Path>> listRepeatedFiles(File file) {
        List<Entry<Path, Path>> repeatedEntries = new ArrayList<>();
        Map<String, Path> fileMap = new LinkedHashMap<>();

        FileTreeWalker.getPathByExtensionAsync(file, path -> {
            String sha256Hash = getSha256Hash(path);
            Path put = fileMap.put(sha256Hash, path);
            if (put != null) {
                Entry<Path, Path> e = new AbstractMap.SimpleEntry<>(put, path);
                LOG.info("{}", e);
                repeatedEntries.add(e);
            }
        }, ".mp3");

        return repeatedEntries;
    }

    private static void addToNotRepeated(List<Entry<Path, Path>> notRepeatedEntries, Map<String, Path> fileMap,
            Path path)
            throws IOException {
        String sha256Hash = getSha256Hash(path);
        Path put = fileMap.put(sha256Hash, path);
        if (put == null) {
            String name = name(path);
            fileMap.values().stream().filter(e -> name.equals(name(e)) && !path.equals(e)).findFirst()
                    .ifPresent(orElse -> {
                Entry<Path, Path> e = new AbstractMap.SimpleEntry<>(orElse, path);
                LOG.info("{}", e);
                notRepeatedEntries.add(e);
            });
        }
    }

    private static String name(Path path) {
        return path.getName(path.getNameCount()-1).toString();
    }

}