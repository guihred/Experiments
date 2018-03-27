package contest.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import simplebuilder.ResourceFXUtils;

public final class ContestReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContestReader.class);

	private ContestReader() {
	}

	public static void main(String[] args) {
		File file = ResourceFXUtils.toFile("sngpc.pdf");
		System.out.println(file.exists());
		try {
			ObservableList<ContestQuestion> medicamentosSNGPCPDF;
			medicamentosSNGPCPDF = getMedicamentosSNGPCPDF(file);
			medicamentosSNGPCPDF.forEach(System.out::println);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	public static ObservableList<ContestQuestion> getMedicamentosSNGPCPDF(File file) throws IOException {
		PDFTextStripper pdfStripper = new PDFTextStripper();

		try (RandomAccessFile source = new RandomAccessFile(file, "r");
				COSDocument cosDoc = parseAndGet(source);
				PDDocument pdDoc = new PDDocument(cosDoc);) {
			pdfStripper.setStartPage(1);
			String parsedText = pdfStripper.getText(pdDoc);
			cosDoc.close();
			String[] linhas = parsedText.split("\r\n");
			ContestQuestion medicamento = new ContestQuestion();
			ObservableList<ContestQuestion> listaMedicamentos = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				medicamento = tryReadSNGPCLine(linhas, medicamento, listaMedicamentos, i);

			}
			return listaMedicamentos;
		} catch (Exception e) {
			throw e;
		}

	}

	private static ContestQuestion tryReadSNGPCLine(String[] linhas, ContestQuestion m,
			ObservableList<ContestQuestion> arrayList, int i) {
		ContestQuestion medicamento = m;
		try {

			String s = linhas[i];
			String[] split = s.trim().split("\\s+");

			if ("Página:".equals(split[0]) || !StringUtil.isNumeric(split[split.length - 1])) {
				return medicamento;
			}
			if (split.length == 4 || split.length == 3) {
				medicamento.setLote(
						!StringUtil.isNumeric(split[0]) ? split[0] : Integer.toString(Integer.valueOf(split[0])));
				medicamento.setRegistro(split[1].replaceAll("\\D+", ""));
				medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
				arrayList.add(medicamento);
				medicamento = medicamento.clonar();
			} else {
				medicamento.setCodigo(Integer.valueOf(split[0]));
				medicamento.setNome(
						Stream.of(split).skip(1).limit((long) split.length - 2).collect(Collectors.joining(" ")));

				medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
			}
		} catch (Exception e) {
			System.out.println("ERRO LINHA =" + i);
			LOGGER.error("", e);
		}
		return medicamento;
	}

	public static ObservableList<ContestQuestion> getMedicamentosRosario(File file) throws IOException {
		PDFTextStripper pdfStripper = new PDFTextStripper();
		try (RandomAccessFile source = new RandomAccessFile(file, "r");
				COSDocument cosDoc = parseAndGet(source);
				PDDocument pdDoc = new PDDocument(cosDoc);) {
			pdfStripper.setStartPage(1);
			String parsedText = pdfStripper.getText(pdDoc);
			String[] linhas = parsedText.split("\r\n");
			IntUnaryOperator mapper = e -> e;
			IntStream.of(1, 2, 3).map(mapper);
			Map<String, IntUnaryOperator> hashMap = new HashMap<>();

			ObservableList<ContestQuestion> medicamentos = FXCollections.observableArrayList();
			for (int i = 0; i < linhas.length; i++) {
				ContestQuestion medicamento = tryReadRosarioLine(hashMap, linhas, i);
				if (medicamento != null) {
					medicamentos.add(medicamento);
					if (StringUtil.isBlank(medicamento.getNome())) {
						medicamento.setNome(medicamentos.get(medicamentos.size() - 2).getNome());
					}
				}
			}
			return medicamentos;
		} catch (Exception e) {
			throw e;
		}

	}

	private static ContestQuestion tryReadRosarioLine(Map<String, IntUnaryOperator> mapaCampos, String[] linhas,
			int i) {
		try {
			String s = linhas[i];
			String[] split = s.trim().split("\\s+");
			if (split.length > 2 && (s.toLowerCase().contains("descricao") || s.toLowerCase().contains("codproduto")
					|| s.toLowerCase().contains("qtestoquecomercial"))) {
				if (split[1].equalsIgnoreCase("codproduto")) {
					mapaCampos.put("codproduto", j -> j - 2);
				}
				if (split[0].equalsIgnoreCase("codproduto")) {
					mapaCampos.put("codproduto", j -> 0);
				}
				mapaCampos.put("qtestoquecomercial", j -> j - 1);
			}
			if (!s.endsWith(",00")) {
				return null;
			}
			if (split.length >= 2) {
				ContestQuestion medicamento = new ContestQuestion();
				String s2 = split[mapaCampos.getOrDefault("codproduto", j -> 0).applyAsInt(split.length)];

					medicamento.setCodigo(
							Integer
							.valueOf(s2));
				medicamento.setNome(IntStream
						.range(0, split.length).filter(e -> mapaCampos.values().stream()
								.mapToInt(j -> j.applyAsInt(split.length)).noneMatch(j -> j == e))
						.mapToObj(e -> split[e])
								.collect(Collectors.joining(" ")));
				medicamento.setQuantidade(
						Integer.valueOf(split[mapaCampos.getOrDefault("qtestoquecomercial", j -> j - 1)
								.applyAsInt(split.length)].replace(",00", "").replace(".", "")));
				return medicamento;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
		PDFParser parser = new PDFParser(source);
		parser.parse();
		return parser.getDocument();
	}

	public static boolean isPDF(File selectedFile) {
		return selectedFile.getName().endsWith(".pdf");
	}



}