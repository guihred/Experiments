package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javafx.collections.ObservableList;

final class FileTreeWalker implements FileVisitor<Path> {
    private final List<Path> pathList;
    private final String[] other;

    FileTreeWalker(List<Path> pathList, String[] other) {
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
        if (ResourceFXUtils.hasExtension(file, other)) {
            if (pathList instanceof ObservableList) {
                RunnableEx.runInPlatform(()->pathList.add(file));
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

    static void walk(File start, List<Path> pathList, String... other) throws IOException {
        Files.walkFileTree(start.toPath(), new FileTreeWalker(pathList, other));
    }
}