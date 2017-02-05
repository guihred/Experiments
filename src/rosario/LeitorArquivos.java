package rosario;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeitorArquivos {
	public static final Logger logger = LoggerFactory.getLogger(LeitorArquivos.class);

	public static void main(String[] args) throws IOException {
		File file = new File("sngpc.pdf");
		System.out.println(file.exists());
		ObservableList<Medicamento> medicamentosSNGPCPDF = getMedicamentosSNGPCPDF(file);
		medicamentosSNGPCPDF.forEach(System.out::println);
	}

	public static ObservableList<Medicamento> getMedicamentosSNGPCPDF(File file) throws IOException {
		PDFTextStripper pdfStripper = new PDFTextStripper();

		try (RandomAccessFile source = new RandomAccessFile(file, "r");
				COSDocument cosDoc = parseAndGet(source);
				PDDocument pdDoc = new PDDocument(cosDoc);) {
			pdfStripper.setStartPage(1);
			String parsedText = pdfStripper.getText(pdDoc);
			cosDoc.close();
			String[] linhas = parsedText.split("\r\n");
			Medicamento medicamento = new Medicamento();
			ObservableList<Medicamento> arrayList = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				try {

					String s = linhas[i];
					String[] split = s.trim().split("\\s+");

					if (split[0].equals("Página:") || !StringUtil.isNumeric(split[split.length - 1])) {
						continue;
					}
					if (split.length == 4 || split.length == 3) {
						medicamento.setLote(!StringUtil.isNumeric(split[0]) ? split[0]
								: Integer.toString(Integer.valueOf(split[0])));
						medicamento.setRegistro(split[1].replaceAll("\\D+", ""));
						medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
						arrayList.add(medicamento);
						medicamento = medicamento.clonar();
					} else {
						medicamento.setCodigo(Integer.valueOf(split[0]));
						medicamento.setNome(
								Stream.of(split).skip(1).limit((long) split.length - 2)
										.collect(Collectors.joining(" ")));
						if (split.length > 3) {
							medicamento.setApresentacao(split[2]);
						}
						medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
					}
				} catch (Exception e) {
					System.out.println("ERRO LINHA =" + i);
					logger.error("", e);
				}

			}
			return arrayList;
		} catch (Exception e) {
			throw e;
		}

	}

	public static ObservableList<Medicamento> getMedicamentosSNGPC(File file) throws IOException {
		Document parse = Jsoup.parse(file, StandardCharsets.UTF_8.name());
		ObservableList<Medicamento> arrayList = FXCollections.observableArrayList();

		Medicamento medicamento = new Medicamento();
		Elements select = parse.body().select("TR");
		for (Element element : select) {
			Elements children = element.children();
			List<String> collect = children.stream().map(Element::text).map(String::trim)
					.filter(s -> !StringUtil.isBlank(s.trim()) && (s.length() > 1 || StringUtil.isNumeric(s)))
					.collect(Collectors.toList());
			if (collect.isEmpty()) {
				continue;
			}

			int size = collect.size();

			String text2 = collect.get(size - 1);

			if (size == 5 && StringUtil.isNumeric(text2) || size == 4 && !StringUtil.isNumeric(collect.get(1))
					|| size == 3 && !StringUtil.isNumeric(collect.get(1))) {
				String text = collect.get(0);
				if (StringUtil.isNumeric(text)) {
					medicamento.setCodigo(Integer.valueOf(collect.get(0)));
					medicamento.setNome(collect.get(1));
					if (!StringUtil.isNumeric(collect.get(2))) {
						medicamento.setApresentacao(collect.get(2));
					}

					medicamento.setQuantidade(Integer.valueOf(text2));

				}
			} else if (size == 4 && StringUtil.isNumeric(text2)) {
				medicamento.setLote(collect.get(0));
				medicamento.setRegistro(collect.get(1).replaceAll("\\D+", ""));
				medicamento.setQuantidade(Integer.valueOf(collect.get(3)));
				arrayList.add(medicamento);
				medicamento = medicamento.clonar();
			}
		}
		return arrayList;
	}

	public static ObservableList<Medicamento> getMedicamentosRosario(File file) throws IOException {

		PDFTextStripper pdfStripper = new PDFTextStripper();
		try (RandomAccessFile source = new RandomAccessFile(file, "r");
				COSDocument cosDoc = parseAndGet(source);
				PDDocument pdDoc = new PDDocument(cosDoc);) {
			pdfStripper.setStartPage(1);
			String parsedText = pdfStripper.getText(pdDoc);
			String[] linhas = parsedText.split("\r\n");
			ObservableList<Medicamento> arrayList = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				try {
					String s = linhas[i];
					String[] split = s.trim().split("\\s+");

					if (!s.endsWith(",00")) {
						continue;
					}
					if (split.length > 2) {
						Medicamento medicamento = new Medicamento();
						medicamento.setCodigo(Integer.valueOf(split[0]));
						medicamento.setNome(
								Stream.of(split).skip(1).limit((long) split.length - 2)
										.collect(Collectors.joining(" ")));
						medicamento.setQuantidade(
								Integer.valueOf(split[split.length - 1].replace(",00", "").replace(".", "")));
						arrayList.add(medicamento);
					}
				} catch (Exception e) {
					logger.error("", e);
				}

			}
			return arrayList;
		} catch (Exception e) {
			throw e;
		}

	}

	private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
		PDFParser parser = new PDFParser(source);
		parser.parse();
		COSDocument cosDoc = parser.getDocument();
		return cosDoc;
	}

	public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) throws IOException {

		try (FileInputStream fileInputStream = new FileInputStream(selectedFile);) {
			Workbook xssfWorkbook = new XSSFWorkbook(fileInputStream);
			Sheet sheetAt = xssfWorkbook.getSheetAt(0);
			Iterator<Row> iterator = sheetAt.iterator();
			// for (int i = 0; i < 1; i++) {
			// iterator.next();
			// }
			ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
			int i = 1;
			while (iterator.hasNext()) {
				i++;
				try {

					Row next = iterator.next();
					Medicamento medicamento = new Medicamento();
					Cell cell0 = next.getCell(0);
					if (cell0 == null) {
						break;
					}
					String registro;
					if (cell0.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						registro = Integer.toString((int) cell0.getNumericCellValue());
					} else {
						registro = cell0.getStringCellValue().replaceAll("\\D+", "");
					}
					medicamento.setRegistro(registro);
					medicamento.setNome(next.getCell(1).getStringCellValue());
					Cell cell = next.getCell(3);
					String lote = "";
					if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						lote = Integer.toString((int) cell.getNumericCellValue());
					} else if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
						lote = cell.getStringCellValue();
					}
					medicamento.setLote(lote);
					medicamento.setQuantidade((int) next.getCell(4).getNumericCellValue());
					medicamentos.add(medicamento);
				} catch (Exception e) {
					System.out.println("ERRO LINHA=" + i);
					logger.error("", e);

				}
			}
			return medicamentos;

		} catch (Exception e) {
			throw e;
		}
	}

	public static void exportarArquivo(ObservableList<Medicamento> medicamentosLoja,
			List<Medicamento> medicamentosSNGPC, ObservableList<Medicamento> medicamentosAnvisa) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet sheetLoja = wb.createSheet("Estoque Loja");
		criarAbaLoja(medicamentosLoja, wb, sheetLoja);
		Sheet createSheet = wb.createSheet("Estoque SNGPC");
		criarAba(medicamentosSNGPC, wb, createSheet);
		Sheet sheet = wb.createSheet("Estoque ANVISA");
		criarAba(medicamentosAnvisa, wb, sheet);

		File file2 = new File("resultado.xlsx");
		try (OutputStream file = new FileOutputStream(file2);) {
			wb.write(file);
			Desktop.getDesktop().open(file2);
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	private static void criarAba(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setFillBackgroundColor(new XSSFColor(new Color(1.0f, 0.2f, 0.2f)));
		style.setFillPattern(CellStyle.FINE_DOTS);
		int j = 0;
		for (int i = 0; i < medicamentos.size(); i++) {
			Medicamento medicamento = medicamentos.get(i);
			if (medicamento.isRegistroValido() && medicamento.isLoteValido() && medicamento.isQuantidadeValido()) {
				continue;
			}
			Row createRow = createSheet.createRow(j++);

			Cell createCell = createRow.createCell(0);

			createCell.setCellValue(medicamento.getRegistro());
			if (!medicamento.isRegistroValido()) {

				createCell.setCellStyle(style);
			}

			createRow.createCell(1).setCellValue(medicamento.getNome());
			Cell createCell2 = createRow.createCell(2);
			createCell2.setCellValue(medicamento.getLote());
			if (!medicamento.isLoteValido()) {
				createCell2.setCellStyle(style);
			}
			Cell createCell3 = createRow.createCell(3);
			createCell3.setCellValue(medicamento.getQuantidade());
			if (!medicamento.isQuantidadeValido()) {
				createCell3.setCellStyle(style);
			}
		}

		createSheet.autoSizeColumn(0);
		createSheet.autoSizeColumn(1);
		createSheet.autoSizeColumn(2);
		createSheet.autoSizeColumn(3);
		createSheet.autoSizeColumn(4);
	}

	private static void criarAbaLoja(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setFillBackgroundColor(new XSSFColor(new Color(1.0f, 0.2f, 0.2f)));
		style.setFillPattern(CellStyle.FINE_DOTS);
		int j = 0;
		for (int i = 0; i < medicamentos.size(); i++) {
			Medicamento medicamento = medicamentos.get(i);
			if (medicamento.isCodigoValido() && medicamento.isQuantidadeValido()) {
				continue;
			}
			Row createRow = createSheet.createRow(j++);

			Cell createCell = createRow.createCell(0);

			createCell.setCellValue(medicamento.getCodigo());
			if (!medicamento.isCodigoValido()) {
				createCell.setCellStyle(style);
			}

			createRow.createCell(1).setCellValue(medicamento.getNome());
			Cell createCell3 = createRow.createCell(2);
			createCell3.setCellValue(medicamento.getQuantidade());
			if (!medicamento.isQuantidadeValido()) {
				createCell3.setCellStyle(style);
			}
		}

		createSheet.autoSizeColumn(0);
		createSheet.autoSizeColumn(1);
		createSheet.autoSizeColumn(2);
	}

}