package extract;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.RarException.RarExceptionType;
import com.github.junrar.rarfile.FileHeader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import utils.HasLogging;

public final class UnRar {
	public static final String SRC_DIRECTORY = new File("").getAbsolutePath();

	private static final Logger LOGGER = HasLogging.log();

	private static List<String> successfulFiles = new ArrayList<>();
	private static List<String> errorFiles = new ArrayList<>();
	private static List<String> unsupportedFiles = new ArrayList<>();

	private UnRar() {
	}

	public static void extractRarFiles(File file) {
        File output = new File(file.getParentFile(), file.getName().replaceAll("\\.rar", ""));

		if (file.exists()) {
            if (!output.exists()) {
                output.mkdir();
            }
			if (file.isDirectory()) {
				recurseDirectory(file, output);
			} else {
				testFile(file, output);
			}
		}
		printSummary();
	}

	public static void extractRarFiles(String file) {
		extractRarFiles(new File(file));
	}

	public static void main(String[] args) {
		File file = new File(SRC_DIRECTORY);
		extractRarFiles(file);
	}

	private static boolean doesNotExist(File file) {
		return file == null || !file.exists();
	}

	private static void printSummary() {
        LOGGER.info("\nSuccessfully tested archives:\n");
		for (String sf : successfulFiles) {
            LOGGER.info(sf);
		}
        LOGGER.info("Unsupported archives:\n");
		for (String uf : unsupportedFiles) {
            LOGGER.info(uf);
		}
        LOGGER.info("Failed archives:");
		for (String ff : errorFiles) {
            LOGGER.info(ff);
		}
        LOGGER.info("\nSummary\n");
        LOGGER.info("tested:\t\t{}", successfulFiles.size() + unsupportedFiles.size() + errorFiles.size());
        LOGGER.info("successful:\t{}", successfulFiles.size());
        LOGGER.info("unsupported:\t{}", unsupportedFiles.size());
        LOGGER.info("failed:\t\t{}", errorFiles.size());
	}

	private static void recurseDirectory(File file, File output) {
		if (doesNotExist(file)) {
			return;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files == null) {
				return;
			}
			for (File f : files) {
				recurseDirectory(f, output);
			}
		} else {
			testFile(file, output);
		}
	}

	private static void testFile(File file, File output) {
		if (doesNotExist(file)) {
            LOGGER.info("error file {} does not exist", file);
			return;
		}
		String s = file.toString();

		s = s.substring(s.length() - 3);
		if ("rar".equalsIgnoreCase(s)) {
            LOGGER.info("{}", file);
			try (FileInputStream fileInputStream = new FileInputStream(file);
					Archive arc = new Archive(fileInputStream)) {
				if (arc.isEncrypted()) {
                    LOGGER.info("archive is encrypted cannot extreact");
					unsupportedFiles.add(file.toString());
					return;
				}
				List<FileHeader> files = arc.getFileHeaders();
				for (FileHeader fh : files) {
					if (fh.isEncrypted()) {
                        LOGGER.info("file is encrypted cannot extract: {}", fh.getFileNameString());
						unsupportedFiles.add(file.toString());
						return;
					}
                    LOGGER.info("extracting file: {}", fh.getFileNameString());
					if (fh.isFileHeader() && fh.isUnicode()) {
                        LOGGER.info("unicode name: {}", fh.getFileNameW());
					}
                    LOGGER.info("start: {}", LocalTime.now());
					File file2 = new File(output, fh.getFileNameString());
					if (!file2.exists()) {
						boolean createNewFile = file2.createNewFile();
                        LOGGER.info("file {} created {}", file2, createNewFile);
					}
					if (!tryExtractFile(file2, fh, arc, file)) {
						return;
					}
                    LOGGER.info("end: {}", LocalTime.now());
				}
                LOGGER.info("successfully tested archive: {}", file);
				successfulFiles.add(file.toString());
			} catch (Exception e) {
                LOGGER.info("file: {} extraction error - does the file exist? {}", file, e);
				errorFiles.add(file.toString());
			}

		}
	}

	private static boolean tryExtractFile(File file2, FileHeader fh, Archive arc, File file) throws IOException {
		try (FileOutputStream os = new FileOutputStream(file2)) {
			arc.extractFile(fh, os);
		} catch (RarException e) {
			if (e.getType() == RarExceptionType.notImplementedYet) {
                LOGGER.info("error extracting unsupported file: {}", fh.getFileNameString() + e);
				unsupportedFiles.add(file.toString());
				return false;
			}
            LOGGER.info("error extracting file: {}", fh.getFileNameString() + e);
			errorFiles.add(file.toString());
			return false;
		}
		return true;

	}
}
