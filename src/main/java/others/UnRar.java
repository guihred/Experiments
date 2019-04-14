package others;

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
import utils.ResourceFXUtils;

public final class UnRar {
	public static final String SRC_DIRECTORY = new File("").getAbsolutePath();

	private static final Logger LOGGER = HasLogging.log();

	private static List<String> successfulFiles = new ArrayList<>();
	private static List<String> errorFiles = new ArrayList<>();
	private static List<String> unsupportedFiles = new ArrayList<>();

	private UnRar() {
	}

	public static void extractRarFiles(File file) {
		File output = ResourceFXUtils.getOutFile();
		if (file.exists()) {
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

	private static void printSummary() {
		LOGGER.trace("\n\n\nSuccessfully tested archives:\n");
		for (String sf : successfulFiles) {
			LOGGER.trace(sf);
		}
		LOGGER.trace("");
		LOGGER.trace("Unsupported archives:\n");
		for (String uf : unsupportedFiles) {
			LOGGER.trace(uf);
		}
		LOGGER.trace("");
		LOGGER.trace("Failed archives:");
		for (String ff : errorFiles) {
			LOGGER.trace(ff);
		}
		LOGGER.trace("");
		LOGGER.trace("\n\n\nSummary\n");
		LOGGER.trace("tested:\t\t{}", successfulFiles.size() + unsupportedFiles.size() + errorFiles.size());
		LOGGER.trace("successful:\t{}", successfulFiles.size());
		LOGGER.trace("unsupported:\t{}", unsupportedFiles.size());
		LOGGER.trace("failed:\t\t{}", errorFiles.size());
	}

	private static void recurseDirectory(File file, File output) {
		if (file == null || !file.exists()) {
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
		if (file == null || !file.exists()) {
			LOGGER.trace("error file {} does not exist", file);
			return;
		}
		LOGGER.trace(">>>>>> testing archive: {}", file);
		String s = file.toString();

		s = s.substring(s.length() - 3);
		if ("rar".equalsIgnoreCase(s)) {
			LOGGER.trace("{}", file);
			try (FileInputStream fileInputStream = new FileInputStream(file);
					Archive arc = new Archive(fileInputStream)) {
				if (arc.isEncrypted()) {
					LOGGER.trace("archive is encrypted cannot extreact");
					unsupportedFiles.add(file.toString());
					return;
				}
				List<FileHeader> files = arc.getFileHeaders();
				for (FileHeader fh : files) {
					if (fh.isEncrypted()) {
						LOGGER.trace("file is encrypted cannot extract: {}", fh.getFileNameString());
						unsupportedFiles.add(file.toString());
						return;
					}
					LOGGER.trace("extracting file: {}", fh.getFileNameString());
					if (fh.isFileHeader() && fh.isUnicode()) {
						LOGGER.trace("unicode name: {}", fh.getFileNameW());
					}
					LOGGER.trace("start: {}", LocalTime.now());
					File file2 = new File(output, fh.getFileNameString());
					if (!file2.exists()) {
						boolean createNewFile = file2.createNewFile();
						LOGGER.trace("file {} created {}", file2, createNewFile);
					}
					if (!tryExtractFile(file2, fh, arc, file)) {
						return;
					}
					LOGGER.trace("end: {}", LocalTime.now());
				}
				LOGGER.trace("successfully tested archive: {}", file);
				successfulFiles.add(file.toString());
			} catch (Exception e) {
				LOGGER.trace("file: {} extraction error - does the file exist?{}", file, e);
				errorFiles.add(file.toString());
			}

		}
	}

	private static boolean tryExtractFile(File file2, FileHeader fh, Archive arc, File file) throws IOException {
		try (FileOutputStream os = new FileOutputStream(file2)) {
			arc.extractFile(fh, os);
		} catch (RarException e) {
			if (e.getType() == RarExceptionType.notImplementedYet) {
				LOGGER.trace("error extracting unsupported file: {}", fh.getFileNameString() + e);
				unsupportedFiles.add(file.toString());
				return false;
			}
			LOGGER.trace("error extracting file: {}", fh.getFileNameString() + e);
			errorFiles.add(file.toString());
			return false;
		}
		return true;

	}
}
