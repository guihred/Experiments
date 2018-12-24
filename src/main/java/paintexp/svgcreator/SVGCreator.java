package paintexp.svgcreator;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class SVGCreator extends Application {

	static  double currentX = 0, currentY = 0;
	private SVGCommand command;
	private String content = "";
	private TextField contentField = new TextField();
	private ObservableList<Point2D> points = FXCollections.observableArrayList();

	private Point2D lastPoints = new Point2D(0, 0);
	private int pointStage;
	private SVGPath path = new SVGPath();

	private StackPane stack = new StackPane(path);

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	@Override
	public void start(final Stage stage) throws Exception {
		Scene scene = new Scene(createBorderPane());
		scene.setOnKeyPressed(e -> {
			if (e.isControlDown() && e.getCode() == KeyCode.Z) {
				undo();
			}
		});
		stage.setTitle("SVG Creator");
		stage.setScene(scene);
		stage.show();
	}

	private BorderPane createBorderPane() {
		path.setManaged(false);
		stack.setAlignment(Pos.TOP_LEFT);
		stack.addEventHandler(MouseEvent.ANY, this::handleEvent);
		stack.setPrefHeight(500);
		stack.setPrefWidth(500);
		BorderPane root = new BorderPane(stack);
		SimpleToggleGroupBuilder commandsGroup = new SimpleToggleGroupBuilder()
				.onChange((obj, old, newV) -> onChangeCommand(newV));
		for (SVGCommand svg : SVGCommand.values()) {
			commandsGroup.addToggle(svg.name(), svg);
		}
		List<ToggleButton> commandList = commandsGroup.getTogglesAs(ToggleButton.class);
		commandList.forEach(e -> e.setPrefWidth(50));
		GridPane commands = new GridPane();
		commands.addColumn(0, commandList.subList(0, commandList.size() / 2).toArray(new Node[0]));
		commands.addColumn(1, commandList.subList(commandList.size() / 2, commandList.size()).toArray(new Node[0]));
		ColorPicker strokeSelector = new ColorPicker(Color.BLACK);
		strokeSelector.setPrefWidth(100);
		path.strokeProperty().bind(strokeSelector.valueProperty());
		commands.add(strokeSelector, 0, commandList.size(), 2, 1);
		ColorPicker fillSelector = new ColorPicker(Color.TRANSPARENT);
		fillSelector.setPrefWidth(100);
		path.fillProperty().bind(fillSelector.valueProperty());
		commands.add(fillSelector, 0, commandList.size() + 1, 2, 1);
		ComboBox<FillRule> fillRule = new SimpleComboBoxBuilder<FillRule>().items(FillRule.values()).select(0)
				.prefWidth(100).build();
		path.fillRuleProperty().bind(fillRule.getSelectionModel().selectedItemProperty());
		commands.add(fillRule, 0, commandList.size() + 2, 2, 1);

		commands.prefHeight(1000);
		root.setLeft(commands);
		contentField.textProperty().addListener(e -> onTextChange());
		contentField.setText("M0,0");
		root.setTop(contentField);
		commandsGroup.select(0);
		return root;
	}

	private String getArcCommand(final double x, final double y, final double initialX, final double initialY) {
		double d = initialX - lastPoints.getX();
		double rx = x - initialX + d / 2;
		double f = initialY - lastPoints.getY();
		double ry = y - initialY + f / 2;
		int largeArc = rx * rx + ry * ry > (d * d + f * f) / 4 ? 1 : 0;
		Point2D e2 = new Point2D(initialX - d / 2, initialY - f / 2);
		if (!points.contains(e2)) {
			points.add(e2);
		}
		double degrees = Math.toDegrees(Math.atan2(ry, rx));
		double m = (x - initialX) * (lastPoints.getY() - initialY) - (y - initialY) * (lastPoints.getX() - initialX);
		int sweepFlag = m < 0 ? 1 : 0;
		return String.format(Locale.ENGLISH, "%s %s %.0f %.0f %.1f %d %d %.1f %.1f ", getContent(), command.name(), rx,
				ry, degrees, largeArc, sweepFlag, initialX, initialY);
	}

	private String getCommandFor6(final double x, final double y, final double initialX, final double initialY) {
		double secondX = points.size() > 1 ? points.get(1).getX() : x;
		double secondY = points.size() > 1 ? points.get(1).getY() : y;
		return String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f %.1f %.1f ", getContent(), command.name(),
				secondX, secondY, x, y, initialX, initialY);
	}

	private String getFormat(final MouseEvent e) {
		int args = command.getArgs();
		Point2D point2d = points.get(0);
		double initialX = point2d.getX();
		double initialY = point2d.getY();
		double y = Math.max(0, e.getY());
		double x = Math.max(0, e.getX());
		switch (args) {
			case 0:
				return String.format(Locale.ENGLISH, "%s %s ", getContent(), command.name());
			case 1:
				double length = command == SVGCommand.V ? y : x;
				return String.format(Locale.ENGLISH, "%s %s %.1f ", getContent(), command.name(), length);
			case 2:
				return String.format(Locale.ENGLISH, "%s %s %.1f %.1f ", getContent(), command.name(), x, y);
			case 4:
				return String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f ", getContent(), command.name(), x, y,
						initialX, initialY);
			case 6:
				return getCommandFor6(x, y, initialX, initialY);
			case 7:
				return getArcCommand(x, y, initialX, initialY);
			default:
				return getContent();
		}
	}

	private void handleEvent(final MouseEvent e) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED == eventType) {
			if (pointStage == 0) {
				if (!points.isEmpty()) {
					lastPoints = points.get(0);
				}
				points.clear();
			}
			points.add(new Point2D(Math.max(0, e.getX()), Math.max(0, e.getY())));
			if (command.getArgs() < 4) {
				handleSimple(e);
			}
		}
		if (MouseEvent.MOUSE_DRAGGED == eventType && pointStage == 0) {
			handleSimple(e);
		}
		if (pointStage > 0 && MouseEvent.MOUSE_MOVED == eventType) {
			handleSimple(e);
		}
		if (MouseEvent.MOUSE_RELEASED == eventType) {
			int args = command.getArgs() == 7 ? 4 : command.getArgs();
			if (args > 1) {
				pointStage = (pointStage + 1) % (args / 2);
			}
			if (pointStage == 0) {
				points.add(new Point2D(Math.max(0, e.getX()), Math.max(0, e.getY())));
				setContent(path.getContent());
			}
		}

	}

	private void handleSimple(final MouseEvent e) {
		String format = getFormat(e);
		String initialContent = getContent();
		contentField.setText(format);
		setContent(initialContent);
	}

	private void onChangeCommand(final Toggle newV) {
		if (newV != null) {
			command = (SVGCommand) newV.getUserData();
		}
		pointStage = 0;
		setContent(path.getContent());
	}

	private void onTextChange() {
		path.setContent(contentField.getText());
		setContent(path.getContent());
	}

	private void undo() {
		String replaceAll = path.getContent().replaceAll("[a-zA-Z][^a-zA-Z]+$", "");
		contentField.setText(replaceAll);
	}
	public static String convertToRelative(final String path) {
		Pattern compile = Pattern.compile("([A-Za-z][^A-Za-z]+)");
		Matcher matcher = compile.matcher(path);
		matcher.reset();
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String group = matcher.group(1).trim();
			String[] split = group.split("[\\s,[A-Za-z]]+|(?<=\\d)(?=-)");
			System.out.println(group);
			String substring = group.substring(0, 1);
			SVGCommand valueOf = SVGCommand.valueOf(substring.toUpperCase());
			int args = valueOf.getArgs();
			if (args % 2 == 0) {
				String a = replaceEvenCommand(split, substring, args);
				matcher.appendReplacement(sb, a);
			} else if (args == 7) {
				String a = replaceOddCommand(split, substring, args);
				matcher.appendReplacement(sb, a);
			} else {
				matcher.appendReplacement(sb, group);
			}
		}
		matcher.appendTail(sb);
		return sb.toString();

	}

	public static void main(final String[] args) {
		String original = "M15.53 1A5.52 5.52 0 0 0 11 6 5.52 5.52 0 0 0 0 6C0 12.37 11 20 11 20s11-7.63 11-13.42A5.53 5.53 0 0 0 15.53 1z";
		System.out.println(convertToRelative(original));
		System.out.println(original);

		launch(args);
	}

	private static String replaceEvenCommand(final String[] split, final String substring, final int args) {
		StringBuilder cmd = new StringBuilder();
		for (int i = 2; i < split.length; i += 2) {
			double xCoord = Double.parseDouble(split[i - 1]);
			double yCoord = Double.parseDouble(split[i]);
			if (substring.matches("[A-Z]")) {
				double rx = currentX;
				double ry = currentY;
				if (i == args) {
					currentX = xCoord;
				}
				xCoord = xCoord - rx;
				if (i == args) {
					currentY = yCoord;
				}
				yCoord = yCoord - ry;
			} else {
				currentX += xCoord;
				currentY += yCoord;
			}
			String format = String.format(Locale.ENGLISH, " %.1f %.1f", xCoord, yCoord);
			cmd.append(format);
		}
		return String.format("%s %s", substring.toLowerCase(),cmd.toString());
	}

	private static String replaceOddCommand(final String[] split, final String substring, final int args) {
		StringBuilder cmd = new StringBuilder();
		for (int i = args; i < split.length; i += args) {
			double xCoord = Double.parseDouble(split[i - 1]);
			double yCoord = Double.parseDouble(split[i]);
			if (substring.matches("[A-Z]")) {
				double rx = currentX;
				double ry = currentY;
				currentX = xCoord;
				xCoord = xCoord - rx;
				currentY = yCoord;
				yCoord = yCoord - ry;
			} else {
				currentX += xCoord;
				currentY += yCoord;
			}
			String string = Stream.of(split).skip(1 + i - args).limit(args - 2)
					.collect(Collectors.joining(" "));
			String format = String.format(Locale.ENGLISH, "%s%s %.1f %.1f", substring.toLowerCase(), string,
					xCoord, yCoord);
			cmd.append(format);
		}
		return cmd.toString();
	}

}
