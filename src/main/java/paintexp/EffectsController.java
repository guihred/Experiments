package paintexp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import paintexp.tool.PaintModel;
import paintexp.tool.PaintToolHelper;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ex.FunctionEx;

public class EffectsController {
    @FXML
    private WritableImage writableImage0;
    @FXML
    private ComboBox<Effect> effectsCombo;
    @FXML
    private Button adjust;

    private Map<String, Double> maxMap = new HashMap<>();
    @FXML
    private ImageView view;
    @FXML
    private FlowPane effectsOptions;
    private PaintController paintController;
    private PaintModel paintModel;

    public void initialize() {
        WritableImage original = paintController.getSelectedImage();
        PixelReader reader = original.getPixelReader();
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage image = new WritableImage(reader, width, height);
        view.setImage(image);
        effectsCombo.setItems(getEffects(effectsCombo.getItems()));
        effectsCombo.getSelectionModel().selectedIndexProperty().addListener(e -> {
            view.setEffect(effectsCombo.getSelectionModel().getSelectedItem());
            PaintToolHelper.addOptionsAccordingly(effectsCombo.getSelectionModel().getSelectedItem(),
                    effectsOptions.getChildren(), maxMap, effectsCombo.getItems());
        });

    }

    public void onActionAdjust() {
        Bounds bounds = view.getBoundsInParent();
        int width2 = (int) bounds.getWidth() + 2;
        int height2 = (int) bounds.getHeight() + 2;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT.invert());
        WritableImage viewImage = view.snapshot(params, new WritableImage(width2, height2));
        paintController.setFinalImage(viewImage);
        paintModel.createImageVersion();
        StageHelper.closeStage(view);
    }

    public void show(PaintController paintController1, PaintModel paintModel1) {
        paintController = paintController1;
        paintModel = paintModel1;
        new SimpleDialogBuilder().bindWindow(paintModel1.getImageSize()).title("Add Effect")
                .node(CommonsFX.loadParent("Effects.fxml", this)).displayDialog();
    }

    public static ObservableList<Effect> getEffects(ObservableList<Effect> observableList) {
        return observableList.stream().map(FunctionEx.makeFunction(e -> e.getClass().newInstance()))
                .filter(Objects::nonNull).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

}
