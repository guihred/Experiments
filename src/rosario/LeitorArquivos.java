package rosario;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LeitorArquivos {

	public static void main(String[] args) throws IOException {
		File file = new File("sngpc.pdf");
		getMedicamentosSNGPCPDF(file);
	}

	public static ObservableList<Medicamento> getMedicamentosSNGPCPDF(File file) throws IOException {
		PDFTextStripper pdfStripper = new PDFTextStripper();
		PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(file));
		COSDocument cosDoc = parser.getDocument();
		PDDocument pdDoc = new PDDocument(cosDoc);
		parser.parse();

		pdfStripper.setStartPage(1);

		String parsedText = pdfStripper.getText(pdDoc);
		String[] linhas = parsedText.split("\r\n");
		Medicamento medicamento = new Medicamento();
		ObservableList<Medicamento> arrayList = FXCollections.observableArrayList();
		for (int i = 0; i < linhas.length; i++) {
			String s = linhas[i];
			String[] split = s.split(" ");

			if (split[0].equals("Página:") || !StringUtil.isNumeric(split[split.length - 1])) {
				continue;
			}
			if (split.length == 4) {
				medicamento.setLote(split[0]);
				medicamento.setRegistro(split[1].replaceAll("\\D+", ""));
				medicamento.setQuantidade(Integer.valueOf(split[3]));
				arrayList.add(medicamento);
				medicamento = medicamento.clonar();
			} else {
				medicamento.setCodigo(split[0]);
				medicamento.setNome(Stream.of(split).skip(1).limit(split.length - 2).collect(Collectors.joining(" ")));
				if (split.length > 3) {
					medicamento.setApresentacao(split[2]);
				}
				medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
			}

		}
		return arrayList;

	}

	public static ObservableList<Medicamento> getMedicamentosSNGPC(File file) throws IOException {
		Document parse = Jsoup.parse(file, StandardCharsets.UTF_8.name());
		ObservableList<Medicamento> arrayList = FXCollections.observableArrayList();

		Medicamento medicamento = new Medicamento();
		Elements select = parse.body().select("TR");
		for (Element element : select) {
			Elements children = element.children();
			List<String> collect = children.stream().map(Element::text).map(String::trim)
					.filter(s -> !StringUtil.isBlank(s.trim()) && (s.length() > 1 || StringUtil.isNumeric(s))).collect(Collectors.toList());
			if (collect.isEmpty()) {
				continue;
			}

			int size = collect.size();

			String text2 = collect.get(size - 1);

			if (size == 5 && StringUtil.isNumeric(text2) || size == 4 && !StringUtil.isNumeric(collect.get(1)) || size == 3
					&& !StringUtil.isNumeric(collect.get(1))) {
				String text = collect.get(0);
				if (StringUtil.isNumeric(text)) {
					medicamento.setCodigo(collect.get(0));
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

	public static ObservableList<Medicamento> getMedicamentosRosario(File selectedFile) throws IOException {

		Workbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(selectedFile));

		Sheet sheetAt = xssfWorkbook.getSheetAt(0);
		Iterator<Row> iterator = sheetAt.iterator();
		for (int i = 0; i < 5; i++) {
			iterator.next();
		}
		ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();

		while (iterator.hasNext()) {
			Row next = iterator.next();
			Medicamento medicamento = new Medicamento();
			Cell cell = next.getCell(0);
			medicamento.setCodigo(cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? Integer.toString((int) cell.getNumericCellValue()) : cell
					.getStringCellValue());
			medicamento.setNome(next.getCell(1).getStringCellValue());
			medicamento.setQuantidade((int) next.getCell(2).getNumericCellValue());
			medicamentos.add(medicamento);
		}
		return medicamentos;

	}

	public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) throws IOException {

		Workbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(selectedFile));

		Sheet sheetAt = xssfWorkbook.getSheetAt(0);
		Iterator<Row> iterator = sheetAt.iterator();
		ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();

		while (iterator.hasNext()) {
			try {
				Row next = iterator.next();
				Medicamento medicamento = new Medicamento();
				medicamento.setRegistro(next.getCell(0).getStringCellValue().replaceAll("\\D+", ""));
				medicamento.setNome(next.getCell(1).getStringCellValue());
				Cell cell = next.getCell(2);
				medicamento.setLote(cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? Integer.toString((int) cell.getNumericCellValue()) : cell
						.getStringCellValue());
				medicamento.setQuantidade((int) next.getCell(3).getNumericCellValue());
				medicamentos.add(medicamento);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return medicamentos;

	}

	public static void exportarArquivo(List<Medicamento> medicamentos, ObservableList<Medicamento> medicamentosAnvisa) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();
		Sheet createSheet = wb.createSheet("Estoque SNGPC");
		criarAba(medicamentos, wb, createSheet);
		Sheet sheet = wb.createSheet("Estoque ANVISA");
		criarAba(medicamentosAnvisa, wb, sheet);

		OutputStream file = new FileOutputStream(new File("resultado.xlsx"));

		wb.write(file);

	}

	private static void criarAba(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setFillBackgroundColor(new XSSFColor(new Color(1.0f, 0.2f, 0.2f)));
		style.setFillPattern(CellStyle.FINE_DOTS);
		int j = 0;
		for (int i = 0; i < medicamentos.size(); i++) {
			Medicamento medicamento = medicamentos.get(i);
			if (!medicamento.isLoteValido() || !medicamento.isRegistroValido() || !medicamento.isQuantidadeValido()) {

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
		}

		createSheet.autoSizeColumn(0);
		createSheet.autoSizeColumn(1);
		createSheet.autoSizeColumn(2);
		createSheet.autoSizeColumn(3);
		createSheet.autoSizeColumn(4);
	}

}