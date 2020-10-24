package fxsamples;

import static utils.ResourceFXUtils.getOutFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javafx.concurrent.Task;
import utils.ExtractUtils;
import utils.FileTreeWalker;

public final class SimpleCopyTask extends Task<Boolean> {
    private final int numFiles;

    private Random rnd = new Random();

    SimpleCopyTask(int numFiles) {
        this.numFiles = numFiles;
    }
    @Override
    public Boolean call() throws Exception {
        for (long i = 0; i < numFiles; i++) {
            long elapsedTime = System.currentTimeMillis();

            List<Path> pathByExtension2 = FileTreeWalker.getPathByExtension(getOutFile(), ".txt");
            File file = pathByExtension2.get(rnd.nextInt(pathByExtension2.size())).toFile();
            File outFile = getOutFile("txt/resultado2.txt");
            copyFile(file.getAbsolutePath(), outFile.getAbsolutePath());
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            String status = elapsedTime + " milliseconds";
            // queue up status
            updateMessage(status);
            updateProgress(i + 1, numFiles);
        }
        return true;
    }

    private void copyFile(String src, String dest) throws InterruptedException, IOException {
        // simulate a long time
        ExtractUtils.copy(src, dest);

        long millis = rnd.nextInt(1000);
        Thread.sleep(millis);
    }
}