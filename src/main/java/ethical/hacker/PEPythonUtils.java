package ethical.hacker;

import static simplebuilder.SimpleVBoxBuilder.newVBox;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.CommonsFX;
import utils.ConsoleUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class PEPythonUtils extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Text fileType = SimpleTextBuilder.newBoldText("");
        Text sections = SimpleTextBuilder.newBoldText("");
        Text timestamp = SimpleTextBuilder.newBoldText("");
        Text exports = SimpleTextBuilder.newBoldText("");
        Text imports = SimpleTextBuilder.newBoldText("");
        Button loadFile =
                new FileChooserBuilder().name("Load").title("Load File").extensions("All Files", "*.*").onSelect(s -> {
                    runSet(() -> getFileType(s), fileType);
                    runSet(() -> join(getSections(s)), sections);
                    runSet(() -> join(getTimestamp(s)), timestamp);
                    runSet(() -> join(getImports(s)), imports);
                    runSet(() -> join(getExports(s)), exports);
                }).buildOpenButton();
        VBox build = new SimpleVBoxBuilder(5, newVBox("Load File", loadFile), newVBox("File Type", fileType),
                newVBox("Timestamp", timestamp), newVBox("Sections", sections), newVBox("Exports", exports),
                newVBox("Imports", imports)).build();
        primaryStage.setScene(new Scene(new ScrollPane(build)));
        primaryStage.setTitle("PE Analyzer");
        primaryStage.show();

    }

    public static String getFileType(File file) {
        List<String> executeInConsoleInfo = simpleExecution("python/magicExample.py", file);
        return executeInConsoleInfo.stream().findFirst().orElse(null);
    }

    public static List<String> getSections(File file) {
        return simpleExecution("python/pedisplaySections.py", file);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String fullPath(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/").replaceAll("^/", "");
    }

    private static List<String> getExports(File s) {
        return simpleExecution("python/peexportsExample.py", s);
    }

    private static List<String> getImports(File s) {
        return simpleExecution("python/peimportExample.py", s);
    }

    private static List<String> getTimestamp(File s) {
        return simpleExecution("python/peDisplayTimestamp.py", s);
    }

    private static String join(List<String> sections2) {
        return sections2.stream().map(s -> s.replaceAll("b'|'$|\\\\x00", "")).collect(Collectors.joining("\n"));
    }

    private static void runSet(SupplierEx<String> run, Text text) {
        RunnableEx.runNewThread(run, s -> CommonsFX.runInPlatform(() -> text.setText(s)));
    }

    private static List<String> simpleExecution(String magicPath, File file) {
        File file2 = ResourceFXUtils.toFile(magicPath);
        String format = String.format("python %s %s", fullPath(file2), fullPath(file));
        return ConsoleUtils.executeInConsoleInfo(format);
    }
}
