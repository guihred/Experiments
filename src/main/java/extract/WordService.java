package extract;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

public class WordService {


	public static void getPowerPointImages(String arquivo) {
		try (XMLSlideShow a = new XMLSlideShow(new FileInputStream(arquivo));) {
			List<XSLFPictureData> pictureData = a.getPictureData();
			for (XSLFPictureData data : pictureData) {
				FileOutputStream fileOutputStream = new FileOutputStream(new File(data.getFileName()));
				InputStream inputStream = data.getInputStream();
				IOUtils.copy(inputStream, fileOutputStream);
				System.out.println();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		getPowerPointImages("Kit-Toilette-Prata-Adamascado.pptx");
	}
	public void getWord(Map<String, Object> mapaSubstituicao, String arquivo, OutputStream outStream) {
		InputStream resourceAsStream = getClass().getResourceAsStream("/../../resources/doc/" + arquivo);

		try (XWPFDocument document1 = new XWPFDocument(resourceAsStream);) {
			for (XWPFHeader p : document1.getHeaderList()) {
				List<XWPFParagraph> paragraphs = p.getParagraphs();
				for (XWPFParagraph paragraph : paragraphs) {
					String text = paragraph.getText();
					printDebug(text);
					if (mapaSubstituicao.containsKey(text) || mapaSubstituicao.containsKey(text.trim())) {
						Object object = getObject(mapaSubstituicao, text);
						substituirParagrafo(paragraph, Objects.toString(object, ""));
					}
				}
			}

			List<IBodyElement> bodyElements = document1.getBodyElements();
			bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.PARAGRAPH)
					.forEach((IBodyElement element) -> substituirParagrafo(mapaSubstituicao, (XWPFParagraph) element));
			bodyElements.stream().filter(e -> e.getElementType() == BodyElementType.TABLE)
					.forEach(tabela -> substituirTabela(tabela, mapaSubstituicao));
			document1.write(outStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void substituirParagrafo(Map<String, Object> mapaSubstituicao, XWPFParagraph paragraph) {
		String text = paragraph.getText();
		if (mapaSubstituicao.containsKey(text) || mapaSubstituicao.containsKey(text.trim())) {
			Object object = getObject(mapaSubstituicao, text);
			if (object instanceof String) {
				substituirParagrafo(paragraph, object.toString());
				if (object.toString().matches("mailto:.+")) {
					substituirParagrafo(paragraph, "Email: ");
					removerLinks(paragraph);
					String id = paragraph.getDocument().getPackagePart()
							.addExternalRelationship(object.toString(), XWPFRelation.HYPERLINK.getRelation()).getId();
					// Append the link and bind it to the relationship
					CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
					cLink.setId(id);
					CTText ctText = CTText.Factory.newInstance();
					ctText.setStringValue(object.toString().split(":")[1]);
					CTR ctr = CTR.Factory.newInstance();
					ctr.setTArray(new CTText[] { ctText });

					// Insert the linked text into the link
					cLink.setRArray(new CTR[] { ctr });

				}

			}

		}
	}

	@SuppressWarnings("deprecation")
	private static void removerLinks(XWPFParagraph paragraph) {
		int size = paragraph.getCTP().getHyperlinkArray().length;
		for (int i = 0; i < size; i++) {
			paragraph.getCTP().removeHyperlink(0);
		}
	}

	private static void substituirCell(XWPFTableCell cell, String string) {
		List<XWPFParagraph> paragraphs = cell.getParagraphs();

		int size = paragraphs.size();
		for (int l = 1; l < size; l++) {
			cell.removeParagraph(1);
		}
		XWPFParagraph xwpfParagraph = size > 0 ? cell.getParagraphs().get(0) : cell.addParagraph();
		substituirParagrafo(xwpfParagraph, string);
	}

	private static void substituirParagrafo(XWPFParagraph paragraph, String string) {
		paragraph.getRuns().get(0).setText(string, 0);
		int size = paragraph.getRuns().size();
		for (int j = 1; j < size; j++) {
			paragraph.removeRun(1);
		}
	}

	@SuppressWarnings("unchecked")
	public void substituirTabela(IBodyElement element, Map<String, Object> map) {
		XWPFTable tabela = (XWPFTable) element;
		int numberOfRows = tabela.getNumberOfRows();

		for (int i = 0; i < numberOfRows; i++) {
			XWPFTableRow row = tabela.getRow(i);
			List<XWPFTableCell> tableCells = row.getTableCells();
			for (int j = 0; j < tableCells.size(); j++) {
				XWPFTableCell cell = row.getCell(j);
				String cellText = cell.getText();
				printDebug(cellText);
				if (map.containsKey(cellText) || map.containsKey(cellText.trim())) {
					Object object = getObject(map, cellText);
					if (object instanceof String) {
						String string = object.toString();
						substituirCell(cell, string);
					}
				}

			}

		}
	}

	private void printDebug(String cellText) {
		// TODO Auto-generated method stub

	}

	private static Object getObject(Map<String, Object> map, String cellText) {
		return map.containsKey(cellText) ? map.get(cellText) : map.get(cellText.trim());
	}

}
