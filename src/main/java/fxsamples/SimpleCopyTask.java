package fxsamples;

import static utils.ResourceFXUtils.getOutFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.poi.util.IOUtils;
import utils.ResourceFXUtils;

public final class SimpleCopyTask extends Task<Boolean> {
    private final int numFiles;

    private SecureRandom rnd = new SecureRandom();

    SimpleCopyTask(int numFiles) {
        this.numFiles = numFiles;
    }
    @Override
    public Boolean call() throws Exception {
        for (long i = 0; i < numFiles; i++) {
            long elapsedTime = System.currentTimeMillis();

            List<Path> pathByExtension2 = ResourceFXUtils.getPathByExtension(getOutFile(), ".txt");
            File file = pathByExtension2.get(rnd.nextInt(pathByExtension2.size())).toFile();
            File outFile = getOutFile("resultado2.txt");
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
        IOUtils.copy(new FileInputStream(src), new FileOutputStream(dest));

        long millis = rnd.nextInt(1000);
        Thread.sleep(millis);
    }
}