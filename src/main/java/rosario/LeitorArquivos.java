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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

import simplebuilder.ResourceFXUtils;

public final class LeitorArquivos {
	private static final Logger LOGGER = LoggerFactory.getLogger(LeitorArquivos.class);

	private LeitorArquivos() {
	}

	public static void main(String[] args) {
		File file = ResourceFXUtils.toFile("sngpc.pdf");
		System.out.println(file.exists());
		try {
			ObservableList<Medicamento> medicamentosSNGPCPDF;
			medicamentosSNGPCPDF = getMedicamentosSNGPCPDF(file);
			medicamentosSNGPCPDF.forEach(System.out::println);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
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
			ObservableList<Medicamento> listaMedicamentos = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				medicamento = tryReadSNGPCLine(linhas, medicamento, listaMedicamentos, i);

			}
			return listaMedicamentos;
		} catch (Exception e) {
			throw e;
		}

	}

	private static Medicamento tryReadSNGPCLine(String[] linhas, Medicamento m,
			ObservableList<Medicamento> arrayList, int i) {
		Medicamento medicamento = m;
		try {

			String s = linhas[i];
			String[] split = s.trim().split("\\s+");

			if ("Página:".equals(split[0]) || !StringUtil.isNumeric(split[split.length - 1])) {
				return medicamento;
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
			LOGGER.error("", e);
		}
		return medicamento;
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
			ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				tryReadRosarioLine(linhas, medicamentos, i);

			}
			return medicamentos;
		} catch (Exception e) {
			throw e;
		}

	}

	private static void tryReadRosarioLine(String[] linhas, ObservableList<Medicamento> arrayList, int i) {
		try {
			String s = linhas[i];
			if (!s.endsWith(",00")) {
				return;
			}
			String[] split = s.trim().split("\\s+");
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
			LOGGER.error("", e);
		}
	}

	private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
		PDFParser parser = new PDFParser(source);
		parser.parse();
		return parser.getDocument();
	}

	public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) throws IOException {
		if (selectedFile.getName().endsWith(".pdf")) {
			return getMedicamentosSNGPCPDF(selectedFile);
		}

		try (FileInputStream fileInputStream = new FileInputStream(selectedFile);) {
			Workbook xssfWorkbook = selectedFile.getName().endsWith(".xls") ? new HSSFWorkbook(fileInputStream)
					: new XSSFWorkbook(fileInputStream);
			Sheet sheetAt = xssfWorkbook.getSheetAt(0);
			Iterator<Row> iterator = sheetAt.iterator();
			ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
			int i = 1;
			while (iterator.hasNext()) {
				i++;
				if (!tryReadAnvisaLine(iterator, medicamentos, i)) {
					break;
				}
			}
			return medicamentos;

		} catch (Exception e) {
			throw e;
		}
	}

	private static boolean tryReadAnvisaLine(Iterator<Row> iterator, ObservableList<Medicamento> medicamentos, int i) {
		try {

			Row next = iterator.next();
			Cell cell0 = next.getCell(0);
			if (cell0 == null) {
				return false;
			}
			Medicamento medicamento = new Medicamento();
			medicamento.setRegistro(getRegistro(cell0));
			medicamento.setNome(next.getCell(1).getStringCellValue());
			medicamento.setLote(getLote(next));
			medicamento.setQuantidade((int) next.getCell(next.getLastCellNum() - 1).getNumericCellValue());
			medicamentos.add(medicamento);
		} catch (Exception e) {
			System.out.println("ERRO LINHA=" + i);
			LOGGER.error("", e);
		}
		return true;
	}

	private static String getRegistro(Cell cell0) {
		String registro;
		if (cell0.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			registro = Integer.toString((int) cell0.getNumericCellValue());
		} else {
			registro = cell0.getStringCellValue().replaceAll("\\D+", "");
		}
		return registro;
	}

	private static String getLote(Row row) {
		Cell cell = row.getCell(row.getLastCellNum() - 2);
		String lote = "";
		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			lote = Integer.toString((int) cell.getNumericCellValue());
		} else if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
			lote = cell.getStringCellValue();
		}
		return lote;
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
			LOGGER.error("", e);
		}

	}

	private static void criarAba(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setFillBackgroundColor(new XSSFColor(new Color(1.0F, 0.2F, 0.2F)));
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
		style.setFillBackgroundColor(new XSSFColor(new Color(1.0F, 0.2F, 0.2F)));
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