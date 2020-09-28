package ethical.hacker.ssh;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.sshd.server.SshServer;
import simplebuilder.SimpleButtonBuilder;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;

public class SSHSessionApp extends Application {
    private Button serverButton;
    private SshServer server;

    @Override
    public void start(Stage primaryStage) {

        Text text = new Text();
        text.textProperty().bind(Bindings.createStringBinding(
                () -> EchoShell.getCommands().stream().collect(Collectors.joining("\n")), EchoShell.getCommands()));
        TextField portField = new TextField("22");
        TextField commandField = new TextField();
        TextField hostField = new TextField();
        TextField userField = new TextField();
        PasswordField passwordField = new PasswordField();
        serverButton = SimpleButtonBuilder.newButton("Start Server", () -> {
            server = BaseTestSupport.setupTestServer();
            server.start();
            portField.setText("" + server.getPort());
            hostField.setText(BaseTestSupport.TEST_LOCALHOST);
            userField.setText(BaseTestSupport.getCurrentTestName());
            passwordField.setText(BaseTestSupport.getCurrentTestName());
            serverButton.setDisable(true);
        });

        Text text2 = new Text();
        Button clientButton = SimpleButtonBuilder.newButton("Start Client", () -> {
            try (FileOutputStream fileOutputStream = new FileOutputStream(ResourceFXUtils.getOutFile("log/log.txt"));
                    PrintStream out = new PrintTextStream(fileOutputStream, true, StandardCharsets.UTF_8.displayName(),
                            text2.textProperty())) {
                String testLocalhost = hostField.getText();
                SSHClientUtils.sendMessage(commandField.getText(), testLocalhost,
                        StringSigaUtils.toInteger(portField.getText()), userField.getText(), passwordField.getText(),
                        out);
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5));
        addRow(gridPane, "Host:", hostField);
        addRow(gridPane, "Port:", portField);
        addRow(gridPane, "User:", userField);
        addRow(gridPane, "Password:", passwordField);
        addRow(gridPane, "Command:", commandField);
        gridPane.add(clientButton, 1, 5);
        gridPane.addColumn(2, serverButton, text);
        ScrollPane child = new ScrollPane(text2);
        gridPane.add(child, 0, 6, 3, 1);
        primaryStage.setTitle("SSH Session APP");
        final int width = 320;
        final int height = 400;
        primaryStage.setScene(new Scene(gridPane, width, height));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("deprecation")
    private static void addRow(GridPane gridPane, String text3, Node hostField) {
        Text text = new Text(text3);
        text.setId(text3.replaceAll(":", ""));
        text.setTextAlignment(TextAlignment.RIGHT);
        gridPane.addRow(gridPane.impl_getRowCount(), text, hostField);
        GridPane.setHalignment(text, HPos.RIGHT);
    }

}
