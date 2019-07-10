package ethical.hacker.ssh;

import java.io.FileOutputStream;
import java.io.IOException;
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
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;

public class SSHSessionApp extends Application {
    private static final Logger LOG = HasLogging.log();

    private SshServer server;
    private Button serverButton;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Text text = new Text();
        text.textProperty().bind(Bindings.createStringBinding(
            () -> EchoShell.COMMANDS.stream().collect(Collectors.joining("\n")), EchoShell.COMMANDS));
        TextField portField = new TextField("22");
        TextField commandField = new TextField();
        TextField hostField = new TextField();
        TextField userField = new TextField();
        PasswordField passwordField = new PasswordField();
        serverButton = CommonsFX.newButton("Start Server", e -> {
            server = SSHClientUtils.setupTestServer();
            try {
                server.start();
                portField.setText("" + server.getPort());
                hostField.setText(SSHClientUtils.TEST_LOCALHOST);
                userField.setText(SSHClientUtils.getCurrentTestName());
                passwordField.setText(SSHClientUtils.getCurrentTestName());
            } catch (IOException e1) {
                LOG.error("", e1);
            }
            serverButton.setDisable(true);
        });


        Text text2 = new Text();
        Button clientButton = CommonsFX.newButton("Start Client", e -> {
            try (PrintStream out = newPrintStream(text2);) {
                String testLocalhost = hostField.getText();
                SSHClientUtils.sendMessage(commandField.getText(), testLocalhost,
                    StringSigaUtils.toInteger(portField.getText()),
                    userField.getText(), passwordField.getText(), out);

            } catch (Exception e1) {
                LOG.error("", e1);
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

    @SuppressWarnings("deprecation")
    private void addRow(GridPane gridPane, String text3, Node hostField) {
        Text text = new Text(text3);
        text.setId(text3.replaceAll(":", ""));
        text.setTextAlignment(TextAlignment.RIGHT);
        gridPane.addRow(gridPane.impl_getRowCount(), text, hostField);
        GridPane.setHalignment(text, HPos.RIGHT);
    }

    private PrintStream newPrintStream(Text text2) {
        try {
            return new PrintStream(new FileOutputStream(ResourceFXUtils.getOutFile("log.txt")), true,
                StandardCharsets.UTF_8.displayName()) {

                @Override
                public void write(byte[] b, int off, int len) {
                    super.write(b, off, len);
                    text2.setText(text2.getText() + new String(b, off, len, StandardCharsets.UTF_8));
                }

            };
        } catch (Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
