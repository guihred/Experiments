package others;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnZip {
	public static final Logger LOGGER = LoggerFactory.getLogger(UnZip.class);

	private UnZip() {
	}

	public static void main(String[] args) {

		File jap = new File("C:\\Users\\Note\\Contacts");
		if (jap.isDirectory()) {
			File[] listFiles = jap.listFiles();
			File output = new File(jap, "arquivos");
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

	private static void extractZip(File saida, File file) {

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));) {
			ZipEntry ze = zipInputStream.getNextEntry();

			byte[] buffer = new byte[1024];
			while (ze != null) {
				String fileName = ze.getName().replaceAll(" ", "");
				File newFile = new File(saida, fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				// new File(newFile.getParent()).mkdirs();

				if (ze.isDirectory()) {
					newFile.mkdirs();
				} else {
					try (FileOutputStream fos = new FileOutputStream(newFile);) {

						int len;
						while ((len = zipInputStream.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}

						fos.close();
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}

				ze = zipInputStream.getNextEntry();
			}
			zipInputStream.closeEntry();
			zipInputStream.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

}
