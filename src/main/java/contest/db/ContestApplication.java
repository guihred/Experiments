package contest.db;

import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static utils.ResourceFXUtils.convertToURL;
import static utils.SupplierEx.get;

import japstudy.db.HibernateUtil;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import utils.ResourceFXUtils;

public class ContestApplication extends Application {

    private ContestReader contestQuestions;

    public ContestApplication() {

    }

    public ContestApplication(ContestReader contestQuestions) {
        this.contestQuestions = contestQuestions;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Contest Questions");
        File file = ResourceFXUtils.toFile("ContestApplication.fxml");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(convertToURL(file));
        Scene scene = new Scene(get(fxmlLoader::load));
        ContestApplicationController controller = fxmlLoader.getController();
        if (contestQuestions != null) {
            controller.setContestQuestions(contestQuestions);
        }
        primaryStage.setTitle("Contest Questions");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    static void extracted(ListView<ContestQuestionAnswer> options, ContestQuestion contestQuestion) {
        ObservableList<ContestQuestionAnswer> items = options.getItems();
        for (int i = 0; i < contestQuestion.getOptions().size(); i++) {
            if (i < 0 || i >= items.size()) {
                items.add(contestQuestion.getOptions().get(i));
            } else {
                items.set(i, contestQuestion.getOptions().get(i));
            }
        }
    }

    static String getText(ContestReader contestQuestions2,int cur) {
        List<String> map = contestQuestions2.getContestTexts().stream().filter(t -> isBetween(t, cur))
            .map(ContestText::getText).filter(StringUtils::isNotBlank).flatMap(s -> Stream.of(s.split("\n")))
            .map(String::trim)
            .collect(Collectors.toList());

        int orElse = map.stream().mapToInt(String::length).max().orElse(0);

        return IntStream.range(0, map.size())
            .mapToObj(i -> mapLines(map, orElse, i))
            .collect(Collectors.joining("\n"));
    }

    private static String addSpaces(String str, int diff) {

        Pattern compile = Pattern.compile(" +");
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < diff; j++) {
            if (!matcher.find()) {
                matcher.appendTail(sb);
                matcher = compile.matcher(sb.toString().trim());
                sb.delete(0, sb.length());
                j--;
            } else {
                String group = matcher.group(0);
                matcher.appendReplacement(sb, group + " ");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean isBetween(ContestText tex, int j) {
        if (tex.getMin() == null || tex.getMax() == null) {
            return false;
        }
        int i = j + 1;
        return tex.getMin() <= i && tex.getMax() >= i;
    }

    private static String justified(List<String> map, int maxLetters, int i) {
        String str = map.get(i);
        int diff = maxLetters - str.length();
        if (i == 0 || paragraphEnd(map.get(i - 1))) {
            return leftPad(addSpaces(str, Math.max(diff - 8, 0)), maxLetters, "");
        }
        if (diff >= maxLetters / 2 || paragraphEnd(str)) {
            return rightPad(str, maxLetters, "");
        }
        String sb = addSpaces(str, diff);
        return rightPad(sb, maxLetters, "");
    }

    private static String mapLines(List<String> map, int orElse, int i) {

        String object = justified(map, orElse, i);
        return String.format("(%02d)    %s", i + 1, object);
    }

    private static boolean paragraphEnd(String str) {
        return str.endsWith(".") || str.endsWith(".”")||str.matches("Texto \\d+");
    }
}
