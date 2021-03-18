package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import utils.ex.ConsumerEx;
import utils.ex.PredicateEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class FileTreeWalker implements FileVisitor<Path> {
    private final List<Path> pathList;
    private final String[] other;

    private FileTreeWalker(List<Path> pathList, String[] other) {
        this.pathList = pathList;
        this.other = other;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return exc != null ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return !Files.isReadable(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (hasExtension(file, other)) {
            if (pathList instanceof ObservableList) {
                CommonsFX.runInPlatform(() -> pathList.add(file));
            } else {
                pathList.add(file);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
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

    public static List<Path> getPathByExtension(File start, String... other) {
        return SupplierEx.get(() -> {
            if (!start.exists()) {
                return Collections.emptyList();
            }
            List<Path> pathList = new ArrayList<>();
            walk(start, pathList, other);
            return pathList;
        }, Collections.emptyList());
    }

    public static ObservableList<Path> getPathByExtensionAsync(File start, ConsumerEx<Path> onPathFound,
            String... other) {
        return SupplierEx.get(() -> {
            if (!start.exists()) {
                return FXCollections.emptyObservableList();
            }
            ObservableList<Path> pathList = FXCollections.observableArrayList();
            pathList.addListener((Change<? extends Path> c) -> {
                while (c.next()) {
                    for (Path path : c.getAddedSubList()) {
                        ConsumerEx.accept(onPathFound, path);
                    }
                }
            });
            RunnableEx.runNewThread(() -> walk(start, pathList, other));
            return pathList;
        }, FXCollections.emptyObservableList());
    }

    public static Path getRandomPathByExtension(File dir, String... other) {
        List<Path> pathByExtension = getPathByExtension(dir, other);
        if (pathByExtension.isEmpty()) {
            return null;
        }
        return pathByExtension.get(new Random().nextInt(pathByExtension.size()));
    }

    private static boolean hasExtension(Path e, String... other) {
        return Stream.of(other).anyMatch(ex -> StringUtils.endsWithIgnoreCase(e.toString(), ex));
    }

    private static void walk(File start, List<Path> pathList, String... other) throws IOException {
        Files.walkFileTree(start.toPath(), new FileTreeWalker(pathList, other));
    }
}