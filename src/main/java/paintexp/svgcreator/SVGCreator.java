package paintexp.svgcreator;

import graphs.entities.ZoomableScrollPane;
import java.util.List;
import java.util.Locale;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import simplebuilder.SimpleToggleGroupBuilder;

public class SVGCreator extends Application {

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

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        path.setStroke(Color.BLACK);
        path.setManaged(false);
        path.setFill(Color.TRANSPARENT);
        stack.setAlignment(Pos.TOP_LEFT);
        stack.addEventHandler(MouseEvent.ANY, this::handleEvent);
        stack.setPrefHeight(500);
        stack.setPrefWidth(500);
        BorderPane root = new BorderPane(new ZoomableScrollPane(stack));
        SimpleToggleGroupBuilder commandsGroup = new SimpleToggleGroupBuilder()
                .onChange((obj, old, newV) -> onChangeCommand(newV));
        for (SVGCommand svg : SVGCommand.values()) {
            commandsGroup.addToggle(svg.name(), svg);
        }
        List<Node> togglesAs = commandsGroup.getTogglesAs(Node.class);
        GridPane commands = new GridPane();
        commands.addColumn(0, togglesAs.subList(0, togglesAs.size() / 2).toArray(new Node[0]));
        commands.addColumn(1, togglesAs.subList(togglesAs.size() / 2, togglesAs.size()).toArray(new Node[0]));
        commands.prefHeight(1000);
        root.setLeft(commands);
        contentField.textProperty().addListener(e -> onTextChange());
        contentField.setText("M0,0");
        root.setTop(contentField);
        commandsGroup.select(0);
        Scene scene = new Scene(root);
        stage.setTitle("SVG Creator");
        stage.setScene(scene);
        stage.show();
    }

    private String getArcCommand(MouseEvent e, double initialX, double initialY) {
        double d = initialX - lastPoints.getX();
        double rx = e.getX() - initialX + d / 2;
        double f = initialY - lastPoints.getY();
        double ry = e.getY() - initialY + f / 2;
        int largeArc = rx * rx + ry * ry > (d * d + f * f) / 4 ? 1 : 0;
        double m = (e.getX() - initialX) * (lastPoints.getY() - initialY)
                - (e.getY() - initialY) * (lastPoints.getX() - initialX);
        Point2D e2 = new Point2D(initialX - d / 2, initialY - f / 2);
        if (!points.contains(e2)) {
            points.add(e2);
        }
        double degrees = Math.toDegrees(Math.atan2(ry, rx));
        int sweepFlag = m < 0 ? 1 : 0;
        return String.format(Locale.ENGLISH, "%s %s %.2f %.2f %.1f %d %d %.1f %.1f ", getContent(), command.name(), rx,
                ry, degrees, largeArc, sweepFlag, initialX, initialY);
    }

    private String getCommandFor6(MouseEvent e, double initialX, double initialY) {
        String format;
        double x = e.getX();
        double y = e.getY();
        if (points.size() > 1) {
            Point2D point2d2 = points.get(1);
            x = point2d2.getX();
            y = point2d2.getY();
        }
        format = String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f %.1f %.1f ", getContent(),
                command.name(), x, y, e.getX(), e.getY(), initialX, initialY);
        return format;
    }

    private void handleEvent(MouseEvent e) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_PRESSED == eventType) {
            if (pointStage == 0) {
                if (!points.isEmpty()) {
                    lastPoints = points.get(0);
                }
                points.clear();
            }
            double initialX = e.getX();
            double initialY = e.getY();
            points.add(new Point2D(initialX, initialY));

            int args = command.getArgs();
            if (args < 4) {
                handleSimple(e, args);
            }
        }
        if (MouseEvent.MOUSE_DRAGGED == eventType) {
            int args = command.getArgs();
            if (pointStage == 0) {
                handleSimple(e, args);
            }
        }
        if (MouseEvent.MOUSE_MOVED == eventType) {
            int args = command.getArgs();
            if (pointStage > 0) {
                handleSimple(e, args);
            }

        }
        if (MouseEvent.MOUSE_RELEASED == eventType) {
            int args = command.getArgs() == 7 ? 4 : command.getArgs();
            if (args > 1) {
                pointStage = (pointStage + 1) % (args / 2);
            }
            if (pointStage == 0) {
                double initialX = e.getX();
                double initialY = e.getY();
                points.add(new Point2D(initialX, initialY));
                setContent(path.getContent());
            }

        }

    }

    private void handleSimple(MouseEvent e, int args) {
        String format = getContent();
        Point2D point2d = points.get(0);
        double initialX = point2d.getX();
        double initialY = point2d.getY();
        switch (args) {
            case 0:
                format = String.format(Locale.ENGLISH, "%s %s ", getContent(), command.name());
                break;
            case 1:
                double length = command == SVGCommand.V ? e.getY() : e.getX();
                format = String.format(Locale.ENGLISH, "%s %s %.1f ", getContent(), command.name(), length);
                break;
            case 2:
                format = String.format(Locale.ENGLISH, "%s %s %.1f %.1f ", getContent(), command.name(), e.getX(),
                        e.getY());
                break;
            case 4:
                format = String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f ", getContent(), command.name(),
                        e.getX(), e.getY(), initialX, initialY);
                break;
            case 6:
                format = getCommandFor6(e, initialX, initialY);
                break;
            case 7:
                format = getArcCommand(e, initialX, initialY);
                break;
            default:
                break;

        }
        String initialContent = getContent();
        contentField.setText(format);
        setContent(initialContent);
    }

    private void onChangeCommand(Toggle newV) {
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


    public static void main(String[] args) {
        launch(args);
    }

}
