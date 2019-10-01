package paintexp.svgcreator;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.StageHelper;

public class SVGCreator extends Application {

    @FXML
    private SVGPath path;
    @FXML
    private TextField contentField;
    @FXML
    private Slider slider;
    @FXML
    private Text width;
    @FXML
    private Text height;
    @FXML
    private ToggleGroup toggleGroup1;
    @FXML
    private ImageView image;
    @FXML
    private StackPane stack;

    private int pointStage;
    private Point2D lastPoints = new Point2D(0, 0);
    private ObservableList<Point2D> points = FXCollections.observableArrayList();
    private SVGCommand command;
    private String content;
    private SVGChanger svgChanger;
    private double lastScale = 1;

    public String getContent() {
        return content;
    }

    public void handleEvent(MouseEvent e) {
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
        if (MouseEvent.MOUSE_DRAGGED == e.getEventType() && pointStage == 0
            || MouseEvent.MOUSE_MOVED == e.getEventType() && pointStage > 0) {
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

    public void initialize() {
        svgChanger = new SVGChanger(path.contentProperty());
        stack.addEventHandler(MouseEvent.ANY, this::handleEvent);
        slider.valueProperty().bindBidirectional(svgChanger.scaleProperty());
        SimpleSliderBuilder.onChange(slider, (a, b, c) -> rescale());
        slider.valueProperty().addListener(o -> rescale());
        contentField.textProperty().addListener(e -> onTextChange());
        contentField.setText("M0,0");
        toggleGroup1.selectToggle(toggleGroup1.getToggles().get(0));
        stack.sceneProperty().addListener(a -> stack.getScene().setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undo();
            }
        }));
    }

    public void onActionBackground(ActionEvent event) {
        StageHelper
				.fileAction("Imagem", ResourceFXUtils.getOutFile().getParentFile(),
						f -> image.setImage(new Image(ResourceFXUtils.convertToURL(f).toExternalForm())),
                "Imagens", "*.jpg", "*.png", "*.bmp", "*.jpg")
            .handle(event);
    }

    public void onActionRelative() {

        double scale = slider.getValue();
        svgChanger.setScale(1 / lastScale);
        contentField.setText(svgChanger.convertToRelative());
        svgChanger.setScale(scale);
        contentField.setText(svgChanger.convertToRelative());
        lastScale = scale;
        // M108 40 L 150.0 140.0 H 130.0 L 115.0 110.0 H 85.0 L 70.0 140.0 H 60.0 Z M
        // 100.0 75.0 L 112.0 103.0 H 87.0 Z
    }

    @SuppressWarnings("unused")
    public void onChangeCommand(ObservableValue<? extends Toggle> o, final Toggle oldV, final Toggle newV) {
        if (newV != null) {
            command = (SVGCommand) newV.getUserData();
        }
        pointStage = 0;
        setContent(path.getContent());
    }

    public void onTextChange() {
        path.setContent(contentField.getText());
        setContent(path.getContent());
        svgChanger.convertToRelative();
        width.setText(String.format("Width%n%.0f", svgChanger.getWidth()));

        height.setText(String.format("Height%n%.0f", svgChanger.getHeight()));
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public void start(final Stage stage) throws Exception {
        CommonsFX.loadFXML("SVG Creator", "SVGCreator.fxml", this, stage);
    }

    public void undo() {
        String replaceAll = path.getContent().replaceAll("[a-zA-Z][^a-zA-Z]+$", "");
        contentField.setText(replaceAll);
    }

    private void handleSimple(final MouseEvent e) {
        String format = command.getFormat(getContent(), e.getX(), e.getY(), points, lastPoints);
        String initialContent = getContent();
        contentField.setText(format);
        setContent(initialContent);
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
        Image image2 = image.getImage();
        if (image2 != null) {
            image.setFitWidth(image2.getWidth() * scale);
        }
        lastScale = scale;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
