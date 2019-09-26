package ethical.hacker.ssh;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import simplebuilder.SimpleButtonBuilder;
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
				() -> EchoShell.getCommands().stream().collect(Collectors.joining("\n")), EchoShell.getCommands()));
		TextField portField = new TextField("22");
		TextField commandField = new TextField();
		TextField hostField = new TextField();
		TextField userField = new TextField();
		PasswordField passwordField = new PasswordField();
		final EventHandler<ActionEvent> onAction = e -> {
			server = BaseTestSupport.setupTestServer();
			try {
				server.start();
				portField.setText("" + server.getPort());
				hostField.setText(BaseTestSupport.TEST_LOCALHOST);
				userField.setText(BaseTestSupport.getCurrentTestName());
				passwordField.setText(BaseTestSupport.getCurrentTestName());
			} catch (IOException e1) {
				LOG.error("", e1);
			}
			serverButton.setDisable(true);
		};
		serverButton = SimpleButtonBuilder.newButton("Start Server", onAction);

		Text text2 = new Text();
		Button clientButton = SimpleButtonBuilder.newButton("Start Client", e -> {
			try (FileOutputStream fileOutputStream = new FileOutputStream(ResourceFXUtils.getOutFile("log.txt"));
					PrintStream out = new PrintTextStream(fileOutputStream, true, StandardCharsets.UTF_8.displayName(),
							text2)) {
				String testLocalhost = hostField.getText();
				SSHClientUtils.sendMessage(commandField.getText(), testLocalhost,
						StringSigaUtils.toInteger(portField.getText()), userField.getText(), passwordField.getText(),
						out);

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

	private final class PrintTextStream extends PrintStream {
		private final Text text2;

		private PrintTextStream(OutputStream out, boolean autoFlush, String encoding, Text text2)
				throws UnsupportedEncodingException {
			super(out, autoFlush, encoding);
			this.text2 = text2;
		}

		@Override
		public void write(byte[] b, int off, int len) {
			super.write(b, off, len);
			text2.setText(text2.getText() + new String(b, off, len, StandardCharsets.UTF_8));
		}
	}

}
