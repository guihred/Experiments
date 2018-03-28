package contest.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ContestReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContestReader.class);

    private ContestReader() {
    }

    public static void main(String[] args) {
        File file = new File("102 - Analista de Tecnologia da Informacao - Tipo D.pdf");
        System.out.println(file.exists());
        try {
            ObservableList<ContestQuestion> medicamentosSNGPCPDF;
            medicamentosSNGPCPDF = getMedicamentosSNGPCPDF(file);
            medicamentosSNGPCPDF.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("", e);
        }
    }

    public static ObservableList<ContestQuestion> getMedicamentosSNGPCPDF(File file) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        ObservableList<ContestQuestion> listaMedicamentos = FXCollections.observableArrayList();
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc);) {
            int numberOfPages = pdDoc.getNumberOfPages();
            Contest contest = new Contest();
            // PrintImageLocations printImageLocations = new PrintImageLocations();
            for (int i = 2; i < numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i);
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i + 1);
                pdfStripper.processPage(page);
                // printImageLocations.processPage(page);

                String parsedText = pdfStripper.getText(pdDoc);

                String[] linhas = parsedText.split("\r\n");

                ContestQuestion question = tryReadSNGPCLine(linhas, listaMedicamentos, contest);

            }
            return listaMedicamentos;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public static void copyStream(Object numb, InputStream obj) throws IOException, FileNotFoundException {

        InputStream createInputStream = obj;
        IOUtils.copy(createInputStream, new FileOutputStream(new File("teste" + numb)));
    }

    public static void save(Object numb, BufferedImage image, String ext) {

        File file = new File("teste" + numb + "." + ext);
        try {
            ImageIO.write(image, ext, file); // ignore returned boolean
        } catch (IOException e) {
            System.out.println("Write error for " + file.getPath() + ": " + e.getMessage());
        }
    }

    private static ContestQuestion tryReadSNGPCLine(String[] linhas, ObservableList<ContestQuestion> arrayList,
            Contest contest) {

        try {
            int estagio = 0;
            int option = 0;
            StringBuilder text = new StringBuilder();
            String subject = null;
            ContestQuestion contestQuestion = new ContestQuestion();
            ContestQuestionAnswer answer = new ContestQuestionAnswer();
            answer.setExercise(contestQuestion);
            for (int i = 0; i < linhas.length; i++) {
                String s = linhas[i];

                if (s.contains("P R O V A  O B J E T I V A")) {
                    estagio = 1;
                    continue;
                }
                if (s.matches("Questões de \\d+ a \\d+\\s*")) {
                    subject = linhas[i - 1];
                    continue;
                }

                if (s.startsWith("Textos ")) {
                    estagio = 2;
                    continue;
                }
                if (s.matches("^\\d+\\s+$")) {
                    continue;
                }
                if (s.matches("QUESTÃO .+___+\\s+")) {
                    if (estagio == 4) {
                        contestQuestion = new ContestQuestion();
                        contestQuestion.setContest(contest);
                        contestQuestion.setSubject(subject);
                        option = 0;
                    }
                    estagio = 3;
                    continue;
                }
                if (s.matches("\\([A-E]\\).+")) {
                    if (estagio == 4) {
                        answer = new ContestQuestionAnswer();
                        answer.setExercise(contestQuestion);
                    }

                    answer.setNumber(option);
                    option++;
                    estagio = 4;

                }
                if (StringUtils.isBlank(s) && estagio == 4) {
                    estagio = 0;
                }

                switch (estagio) {
                    case 0:
                        contestQuestion.setContest(contest);
                        contestQuestion.setSubject(subject);
                        break;
                    case 1:
                        if (StringUtils.isNotBlank(s)) {
                            subject = s;
                            contestQuestion.setSubject(subject);
                            estagio = 0;
                        }
                        break;
                    case 2:
                        if (StringUtils.isNotBlank(s)) {
                            text.append(s + "\n");
                        }
                        break;
                    case 3:
                        if (StringUtils.isNotBlank(s)) {
                            contestQuestion.appendExercise(s + "\n");
                        }
                        break;
                    case 4:
                        if (StringUtils.isNotBlank(s)) {
                            answer.appendAnswer(s + "\n");
                        }
                        break;
                }

                // String[] split = s.trim().split("\\s+");
                if (StringUtils.isNotBlank(s) && estagio != 0) {
                    System.out.println(s);
                }

            }

        } catch (Exception e) {
            System.out.println("ERRO LINHA =");
            LOGGER.error("", e);
        }
        return null;
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
                    // if (StringUtil.isBlank(medicamento.getNome())) {
                    // medicamento.setNome(medicamentos.get(medicamentos.size() - 2).getNome());
                    // }
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

                // medicamento.setCodigo(
                // Integer
                // .valueOf(s2));
                // medicamento.setNome(IntStream
                // .range(0, split.length).filter(e -> mapaCampos.values().stream()
                // .mapToInt(j -> j.applyAsInt(split.length)).noneMatch(j -> j == e))
                // .mapToObj(e -> split[e])
                // .collect(Collectors.joining(" ")));
                // medicamento.setQuantidade(
                // Integer.valueOf(split[mapaCampos.getOrDefault("qtestoquecomercial", j -> j -
                // 1)
                // .applyAsInt(split.length)].replace(",00", "").replace(".", "")));
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