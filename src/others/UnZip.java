package others;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZip {

	public static void main(String[] args) throws IOException {

		File jap = new File("C:\\Users\\salete\\Pictures");
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

					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					while ((len = zipInputStream.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
				}

				ze = zipInputStream.getNextEntry();
			}
			zipInputStream.closeEntry();
			zipInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
