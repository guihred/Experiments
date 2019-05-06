package others;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class UnZip {
    public static final String ZIPPED_FILE_FOLDER = ResourceFXUtils.getOutFile().getParent();
	private static final Logger LOGGER = HasLogging.log();

	private UnZip() {
	}

	public static void extractZippedFiles(File jap) {
		if (jap.isDirectory()) {
			File[] listFiles = jap.listFiles();
			File output = new File(jap, "out");
			if (!output.exists()) {
				output.mkdir();
			}
			for (File file : listFiles) {
				if (file.getName().endsWith("zip")) {
					extractZip(output, file);
				}
			}
		}
	}

	public static void extractZippedFiles(String jap) {
		extractZippedFiles(new File(jap));
	}

	public static void main(String[] args) {

		File jap = new File(ZIPPED_FILE_FOLDER);
		extractZippedFiles(jap);

	}

	private static void extractZip(File saida, File file) {

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
			final int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
            for (ZipEntry ze = zipInputStream.getNextEntry(); ze != null; ze = zipInputStream.getNextEntry()) {
				String fileName = ze.getName().replaceAll(" ", "");
				File newFile = new File(saida, fileName);
				LOGGER.info("file unzip : {}", newFile.getAbsoluteFile());
				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				// new File(newFile.getParent()).mkdirs()
				if (ze.isDirectory()) {
					newFile.mkdirs();
				} else {
					writeNewFile(zipInputStream, buffer, newFile);
				}
			}
			zipInputStream.closeEntry();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	private static void writeNewFile(ZipInputStream zipInputStream, byte[] buffer, File newFile) {
		try (FileOutputStream fos = new FileOutputStream(newFile)) {

			int len;
			while ((len = zipInputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

}
