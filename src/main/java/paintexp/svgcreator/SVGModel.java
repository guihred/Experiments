package paintexp.svgcreator;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;

public class SVGModel {

    private SVGCommand command;
    private String content = "";
    private TextField contentField = new TextField();
    private ObservableList<Point2D> points = FXCollections.observableArrayList();

    private Point2D lastPoints = new Point2D(0, 0);
    private int pointStage;
    private SVGPath path = new SVGPath();

    private StackPane stack = new StackPane(path);

    private SVGChanger svgChanger = new SVGChanger(path.contentProperty());
    private Slider slider;
    private double lastScale = 1;

    private Text width = new Text();
    private Text height = new Text();

    public BorderPane createBorderPane() {
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
        commands.add(CommonsFX.newButton("Relative", e -> makeRelative()), 0, commandList.size() + 3, 2, 1);

        VBox newSlider = CommonsFX.newSlider("Scale", 0.5, 2, svgChanger.scaleProperty());
        slider = (Slider) newSlider.lookup(".slider");
        SimpleSliderBuilder.onChange(slider, (a, b, c) -> rescale());
        slider.valueProperty().addListener(o -> rescale());
        commands.add(newSlider, 0, commandList.size() + 4, 2, 1);
        commands.add(width, 0, commandList.size() + 5, 2, 1);
        commands.add(height, 0, commandList.size() + 6, 2, 1);

        commands.prefHeight(1000);
        root.setLeft(commands);
        contentField.textProperty().addListener(e -> onTextChange());
        contentField.setText("M0,0");
        root.setTop(contentField);
        commandsGroup.select(0);
        return root;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void undo() {
        String replaceAll = path.getContent().replaceAll("[a-zA-Z][^a-zA-Z]+$", "");
        contentField.setText(replaceAll);
    }

    private void handleEvent(MouseEvent e) {
        if (MouseEvent.MOUSE_PRESSED == e.getEventType()) {
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
        if (MouseEvent.MOUSE_DRAGGED == e.getEventType() && pointStage == 0) {
            handleSimple(e);
        }
        if (MouseEvent.MOUSE_MOVED == e.getEventType() && pointStage > 0) {
            handleSimple(e);
        }
        if (MouseEvent.MOUSE_RELEASED == e.getEventType()) {
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
        String format = command.getFormat(getContent(), e.getX(), e.getY(), points, lastPoints);
        String initialContent = getContent();
        contentField.setText(format);
        setContent(initialContent);
    }

    private void makeRelative() {
        String relative = svgChanger.convertToRelative();
        contentField.setText(relative);
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
        svgChanger.convertToRelative();
        width.setText(String.format("Width%n%.0f", svgChanger.getWidth()));
        height.setText(String.format("Height%n%.0f", svgChanger.getHeight()));
    }

    private void rescale() {
        if (slider.isValueChanging()) {
            return;
        }
        double scale = slider.getValue();
        svgChanger.setScale(1 / lastScale);
        contentField.setText(svgChanger.convertToRelative());
        svgChanger.setScale(scale);
        contentField.setText(svgChanger.convertToRelative());
        lastScale = scale;
    }

}
