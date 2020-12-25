package contest;

import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static utils.StringSigaUtils.removerDiacritico;

import contest.db.ContestQuestion;
import contest.db.Organization;
import extract.PdfUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleDialogBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class IadesHelper {
    private static final String VERMELHO = "vermelho";
    private static final String AMARELO = "amarelo";
    private static final Logger LOG = HasLogging.log();
    private static final List<String> IT_KEYWORDS = Arrays.asList("Informação", "Sistema", "Tecnologia", "Informática");

    private IadesHelper() {
    }

    public static void addClasses(Concurso con, Labeled cell) {
        cell.setText(con.getNome());
        cell.getStyleClass().removeAll(AMARELO, VERMELHO);
        if (IadesHelper.hasTI(con.getVagas())) {
            cell.getStyleClass().add(AMARELO);
            return;
        }
        if (con.getVagas().isEmpty()) {
            cell.getStyleClass().add(VERMELHO);
            con.getVagas().addListener((Observable c) -> {
                List<?> vagasList = (List<?>) c;
                if (con.getNome().equals(cell.getText()) && !vagasList.isEmpty()) {
                    cell.getStyleClass().remove(VERMELHO);
                    if (IadesHelper.hasTI(vagasList)) {
                        cell.getStyleClass().add(AMARELO);
                    }
                }
            });
        }
    }

    public static void addClasses(String con, Labeled cell) {
        cell.setText(con);
        cell.getStyleClass().removeAll(AMARELO);
        if (con != null && hasItKeyword(con)) {
            cell.getStyleClass().add(AMARELO);
        }
    }

    public static int containsNumber(String number, Entry<String, String> e) {
        if (containsIgnoreCase(e.getKey(), number)) {
            return 0;
        }
        if (of(number.split(" ")).filter(s -> s.length() > 2).anyMatch(m -> containsIgnoreCase(e.getKey(), m))) {
            return 0;
        }
        if (number.startsWith("2") && containsIgnoreCase(e.getKey(), "médio")) {
            return 1;
        }
        if (number.startsWith("1") && containsIgnoreCase(e.getKey(), "superior")) {
            return 1;
        }
        if (containsIgnoreCase(e.getKey(), "definitivo")) {
            return 2;
        }
        return 5;
    }

    @SafeVarargs
    public static ContestReader getContestQuestions(File file, Organization organization,
        Consumer<ContestReader>... r) {
        ContestReader instance = new ContestReader();
        LOG.info("READING {}", file);
        instance.readFile(file, organization);
        of(r).forEach(e -> e.accept(instance));
        return instance;
    }

    public static File getPDF(String number, File file) {
        if (file.getName().endsWith(".pdf")) {
            return file;
        }
        File file3 = new File(file.getParentFile(), file.getName().replaceAll("\\.\\w+", ""));

        return SupplierEx.get(() -> getFirstPDF(file3, number).toFile());
    }

    public static boolean hasFileExtension(String key) {
        return key.endsWith(".pdf") || key.endsWith(".zip") || key.endsWith(".rar");
    }

    public static boolean hasTI(Collection<?> observableList) {
        return observableList.stream().map(Objects::toString).anyMatch(IadesHelper::hasItKeyword);
    }

    public static boolean saveAnswers(ContestReader entities, List<String> linesRead, String findFirst) {
        String answers = getAnswers(entities, linesRead, findFirst);
        if (answers.length() != entities.getListQuestions().size()) {
            LOG.info("QUESTIONS DON'T MATCH {} {}", answers.length(), entities.getListQuestions().size());
            return false;
        }
        List<ContestQuestion> listQuestions = entities.getListQuestions();
        for (int i = 0; i < listQuestions.size(); i++) {
            ContestQuestion contestQuestion = listQuestions.get(i);
            contestQuestion.setAnswer(answers.charAt(i));
        }
        entities.saveAll();
        return true;
    }

    public static void saveContestValues(Property<Concurso> concurso, String vaga, Node vagasView) {
        if (vaga == null) {
            return;
        }
        Concurso value = concurso.getValue();
        List<Entry<String, String>> linksFound = value.getLinksFound();
        String number = Objects.toString(vaga).replaceAll("\\D", "");
        Entry<String, String> orElse = linksFound.stream().filter(e -> e.getKey().contains("Provas"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).findFirst().orElse(null);
        if (orElse == null) {
            LOG.info("NO LINK FOR Provas found {} - {}", vaga, value);
            return;
        }
        File file = ExtractUtils.extractURL(orElse.getValue());
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", orElse, value, vaga);
            return;
        }
        File file2 = getPDF(number, file);
        getContestQuestions(file2, Organization.IADES, entities -> {
            saveQuestions(concurso, vaga, linksFound, number, entities);
            CommonsFX.runInPlatform(
                    () -> new SimpleDialogBuilder().bindWindow(vagasView).show(ContestApplication.class, entities));
        });
    }

    private static String getAnswers(ContestReader entities, List<String> linesRead, String findFirst) {
        int indexOf = linesRead.indexOf(findFirst);
        List<String> subList = linesRead.subList(indexOf, linesRead.size() - 1);
        List<String> answersList = subList.stream().filter(StringUtils::isNotBlank).filter(s -> s.matches("[\\sA-E#]+"))
            .collect(Collectors.toList());
        StringBuilder answers = new StringBuilder();
        for (String string : answersList) {
            String letters = of(string.split("")).filter(s -> s.matches("[A-E#]")).collect(Collectors.joining());
            answers.append(letters);
            if (answers.length() >= entities.getListQuestions().size()) {
                return answers.toString();
            }
        }
        return answers.toString();
    }

    private static Path getFirstPDF(File file, String number) {
        return FileTreeWalker.getFirstFileMatch(file, path -> nameMatches(number, path)).stream().findFirst()
            .orElseGet(() -> FileTreeWalker.getFirstPathByExtension(file, ".pdf"));
    }

    private static boolean hasItKeyword(String e) {
        return IadesHelper.IT_KEYWORDS.stream()
            .anyMatch(m -> containsIgnoreCase(e, m) || containsIgnoreCase(removerDiacritico(e), removerDiacritico(m)));
    }

    private static boolean nameMatches(String number, Path path) {
        String fileName = path.toFile().getName();
        return (containsIgnoreCase(fileName, number) || fileName.matches(".*" + number.replaceAll(" ", ".*") + ".*")
            || of(number.split(" ")).filter(e -> e.length() > 2).anyMatch(m -> containsIgnoreCase(fileName, m))
            || of(number.split(" ")).map(StringSigaUtils::removerDiacritico).filter(e -> e.length() > 2)
                .anyMatch(m -> containsIgnoreCase(fileName, m)))
            && fileName.endsWith(".pdf");
    }

    private static void saveQuestions(Property<Concurso> concurso, String vaga, List<Entry<String, String>> linksFound,
        String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();

        List<Entry<String, String>> gabaritos = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).collect(Collectors.toList());
        if (gabaritos.isEmpty()) {
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }

        for (Entry<String, String> gabarito : gabaritos) {
            File gabaritoFile = ExtractUtils.extractURL(gabarito.getValue());
            List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
                .collect(Collectors.toList());
            String[] parts = Objects.toString(vaga, "").split("\\s*-\\s*");
            String cargo = parts[parts.length - 1].trim();
            Optional<String> findFirst = linesRead.stream()
                .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
            if (!findFirst.isPresent()) {
                LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabarito.getKey(), linesRead);
                continue;
            }
            if (saveAnswers(entities, linesRead, findFirst.get())) {
                return;
            }
        }

    }

}
