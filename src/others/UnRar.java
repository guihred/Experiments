package others;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.RarException.RarExceptionType;
import com.github.junrar.rarfile.FileHeader;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UnRar {
	/**
	 * @param args
	 */
	private static List<String> successfulFiles = new ArrayList<>();
	private static List<String> errorFiles = new ArrayList<>();
	private static List<String> unsupportedFiles = new ArrayList<>();

	public static void main(String[] args) {
		File file = new File("C:\\Users\\Guilherme\\Videos\\FantasticBeasts\\Fantastic.Beasts.and.Where.to.Find.Them");
		File output = new File(file, "arquivos3");
		if (!output.exists()) {
			output.mkdir();
		}
		if (file.exists()) {
			if (file.isDirectory()) {
				recurseDirectory(file, output);
			} else {
				testFile(file, output);
			}
		}
		printSummary();
	}

	private static void printSummary() {
		System.out.println("\n\n\nSuccessfully tested archives:\n");
		for (String sf : successfulFiles) {
			System.out.println(sf);
		}
		System.out.println("");
		System.out.println("Unsupported archives:\n");
		for (String uf : unsupportedFiles) {
			System.out.println(uf);
		}
		System.out.println("");
		System.out.println("Failed archives:");
		for (String ff : errorFiles) {
			System.out.println(ff);
		}
		System.out.println("");
		System.out.println("\n\n\nSummary\n");
		System.out.println("tested:\t\t" + (successfulFiles.size() + unsupportedFiles.size() + errorFiles.size()));
		System.out.println("successful:\t" + successfulFiles.size());
		System.out.println("unsupported:\t" + unsupportedFiles.size());
		System.out.println("failed:\t\t" + errorFiles.size());
	}

	@SuppressWarnings("resource")
	private static void testFile(File file, File output) {
		if (file == null || !file.exists()) {
			System.out.println("error file " + file + " does not exist");
			return;
		}
		System.out.println(">>>>>> testing archive: " + file);
		String s = file.toString();

		s = s.substring(s.length() - 3);
		if ("rar".equalsIgnoreCase(s)) {
			System.out.println(file.toString());
			try {
				try (Archive arc = new Archive(file);) {
					if (arc.isEncrypted()) {
						System.out.println("archive is encrypted cannot extreact");
						unsupportedFiles.add(file.toString());
						return;
					}
					List<FileHeader> files = arc.getFileHeaders();
					for (FileHeader fh : files) {
						if (fh.isEncrypted()) {
							System.out.println("file is encrypted cannot extract: " + fh.getFileNameString());
							unsupportedFiles.add(file.toString());
							return;
						}
						System.out.println("extracting file: " + fh.getFileNameString());
						if (fh.isFileHeader() && fh.isUnicode()) {
							System.out.println("unicode name: " + fh.getFileNameW());
						}
						System.out.println("start: " + new Date());
						File file2 = new File(output, fh.getFileNameString());
						if (!file2.exists()) {
							file2.createNewFile();
						}
						FileOutputStream os = new FileOutputStream(file2);
						try {
							arc.extractFile(fh, os);
						} catch (RarException e) {
							if (e.getType().equals(RarExceptionType.notImplementedYet)) {
								System.out.println("error extracting unsupported file: " + fh.getFileNameString() + e);
								unsupportedFiles.add(file.toString());
								return;
							}
							System.out.println("error extracting file: " + fh.getFileNameString() + e);
							errorFiles.add(file.toString());
							return;
						} finally {
							os.close();
						}
						System.out.println("end: " + new Date());
					}
					System.out.println("successfully tested archive: " + file);
					successfulFiles.add(file.toString());
				} catch (RarException e) {
					System.out.println("archive consturctor error" + e);
					errorFiles.add(file.toString());
					return;
				}
			} catch (Exception e) {
				System.out.println("file: " + file + " extraction error - does the file exist?" + e);
				errorFiles.add(file.toString());
			}

		}
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
}
