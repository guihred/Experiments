package contest;

import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static simplebuilder.SimpleDialogBuilder.bindWindow;
import static utils.StringSigaUtils.removerDiacritico;

import contest.db.ContestQuestion;
import contest.db.Organization;
import extract.PdfUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.StringSigaUtils;
import utils.SupplierEx;

public final class IadesHelper {
    public static final Logger LOG = HasLogging.log();
    private static final List<String> IT_KEYWORDS = Arrays.asList("Informação", "Sistema", "Tecnologia",
        "Informática");

    private IadesHelper() {
    }

    public static void addClasses(Concurso con, TableCell<Concurso, Object> cell) {
        cell.setText(con.getNome());
        cell.getStyleClass().removeAll("amarelo", "vermelho");
        if (con.getVagas().isEmpty()) {
            cell.getStyleClass().add("vermelho");
            con.getVagas().addListener((Observable c) -> {
                ObservableList<?> observableList = (ObservableList<?>) c;
                if (con.getNome().equals(cell.getText()) && !observableList.isEmpty()) {
                    cell.getStyleClass().remove("vermelho");
                    if (IadesHelper.hasTI(observableList)) {
                        cell.getStyleClass().add("amarelo");
                    }
                }
            });
        } else if (IadesHelper.hasTI(con.getVagas())) {
            cell.getStyleClass().add("amarelo");
        }
    }

    public static String addDomain(Property<String> domain, String l) {
        if (l.startsWith("http")) {
            return l;
        }
        return domain.getValue() + (!l.startsWith("/") ? "/" + l : l);
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

    public static boolean hasTI(ObservableList<?> observableList) {
        return observableList.stream().map(Objects::toString).anyMatch(e -> IadesHelper.IT_KEYWORDS.stream()
            .anyMatch(m -> containsIgnoreCase(e, m) || containsIgnoreCase(removerDiacritico(e), removerDiacritico(m))));
    }

    public static void saveAnswers(ContestReader entities, List<String> linesRead, String findFirst) {
        String answers = getAnswers(entities, linesRead, findFirst);
        if (answers.length() != entities.getListQuestions().size()) {
            LOG.info("QUESTIONS DON'T MATCH {} {}", answers.length(), entities.getListQuestions().size());
            return;
        }
        ObservableList<ContestQuestion> listQuestions = entities.getListQuestions();
        for (int i = 0; i < listQuestions.size(); i++) {
            ContestQuestion contestQuestion = listQuestions.get(i);
            contestQuestion.setAnswer(answers.charAt(i));
        }
        entities.saveAll();
    }

    public static void saveContestValues(Property<Concurso> concurso, String vaga, Node vagasView) {
        if (vaga == null) {
            return;
        }
        Concurso value = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value.getLinksFound();
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
        getContestQuestions(file2, Organization.IADES,
            entities -> {
                saveQuestions(concurso, vaga, linksFound, number, entities);
                Platform.runLater(() -> new ContestApplication(entities).start(bindWindow(new Stage(), vagasView)));
            });
    }

    private static String getAnswers(ContestReader entities, List<String> linesRead, String findFirst) {
        int indexOf = linesRead.indexOf(findFirst);
        List<String> subList = linesRead.subList(indexOf, linesRead.size() - 1);
        List<String> answersList = subList.stream().filter(StringUtils::isNotBlank).filter(s -> s.matches("[\\sA-E#]+"))
            .collect(Collectors.toList());
        StringBuilder answers = new StringBuilder();
        for (String string : answersList) {
            String split = of(string.split("")).filter(s -> s.matches("[A-E#]")).collect(Collectors.joining());
            answers.append(split);
            if (answers.length() >= entities.getListQuestions().size()) {
                return answers.toString();
            }
        }
        return answers.toString();
    }

    private static Path getFirstPDF(File file, String number) throws IOException {
        try (Stream<Path> find = Files.find(file.toPath(), 3, (path, info) -> nameMatches(number, path))) {
            Optional<Path> findFirst = find.findFirst();
            if (findFirst.isPresent()) {
                return findFirst.get();

            }
        }

        try (Stream<Path> find = Files.find(file.toPath(), 3,
            (path, info) -> path.toFile().getName().endsWith(".pdf"))) {
            Optional<Path> findFirst = find.findFirst();
            if (findFirst.isPresent()) {
                return findFirst.get();

            }
            LOG.info("NO PDF found {} {}- {}", file, Arrays.asList(file.list()), number);
            return null;
        }
    }

    private static boolean nameMatches(String number, Path path) {
        String fileName = path.toFile().getName();
        return (containsIgnoreCase(fileName, number) || fileName.matches(".*" + number.replaceAll(" ", ".*") + ".*")
            || of(number.split(" ")).filter(e -> e.length() > 2).anyMatch(m -> containsIgnoreCase(fileName, m))
            || of(number.split(" ")).map(StringSigaUtils::removerDiacritico).filter(e -> e.length() > 2)
                .anyMatch(m -> containsIgnoreCase(fileName, m)))
            && fileName.endsWith(".pdf");
    }

    private static void saveQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        Entry<String, String> gabarito = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).findFirst().orElse(null);
        if (gabarito == null) {
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        File gabaritoFile = ExtractUtils.extractURL(gabarito.getValue());
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabarito.getKey(), linesRead);
            return;
        }
        saveAnswers(entities, linesRead, findFirst.get());

    }

}
