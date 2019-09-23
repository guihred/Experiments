package paintexp;

import static utils.DrawOnPoint.withinImage;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import paintexp.tool.PaintModel;
import paintexp.tool.SelectRectTool;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.PixelatedImageView;
import utils.StageHelper;

public final class PaintViewUtils {
    private static final String PERCENTAGE_FIELD = "Percentage";

    private PaintViewUtils() {
    }

    public static void crop(PaintModel paintModel, WritableImage image) {

        paintModel.getImageStack().getChildren().clear();
		ImageView imageView = new PixelatedImageView(image);
        SelectRectTool tool = paintModel.getCurrentSelectTool();
        tool.setImageSelected(null);
        paintModel.setImage(image);
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);
        paintModel.createImageVersion();
    }

    public static void flipRotate(PaintModel paintModel, WritableImage image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(height, width);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setArgb(height - j - 1, i, pixelReader.getArgb(i, j));
            }
        }
        final WritableImage writableImage1 = writableImage;
        paintModel.setFinalImage(writableImage1);
        paintModel.createImageVersion();
    }

    public static void resize(PaintModel paintModel, WritableImage image) {
        VBox root = new VBox();
        root.getChildren().add(new Text("Redimension"));
        SimpleToggleGroupBuilder groupBuilder = new SimpleToggleGroupBuilder();
        List<RadioButton> togglesAs = groupBuilder.addRadioToggle(PERCENTAGE_FIELD).addRadioToggle("Pixels").select(0)
            .getTogglesAs(RadioButton.class);
        HBox row1 = new HBox(new Text("By:"));
        row1.getChildren().addAll(togglesAs);
        root.getChildren().add(row1);
        TextField widthField = new TextField("100");
        root.getChildren().add(new HBox(new Text("Width:"), widthField));
        TextField heightField = new TextField("100");
        root.getChildren().add(new HBox(new Text("Height:"), heightField));
        CheckBox keepProportion = new CheckBox("Keep Proportion");
        keepProportion.setSelected(true);
        root.getChildren().add(keepProportion);
        groupBuilder.onChange((o, old, newV) -> {
            boolean pencentage = PERCENTAGE_FIELD.equals(((RadioButton) newV).getText());
            widthField.setText(pencentage ? "100" : "" + (int) image.getWidth());
            heightField.setText(pencentage ? "100" : "" + (int) image.getHeight());
        });
        double ratio = image.getWidth() / image.getHeight();
        keepProportion
            .setOnAction(e -> onResizeOptionsChange(groupBuilder, keepProportion, widthField, heightField, ratio));
        widthField.textProperty()
            .addListener(e -> onResizeOptionsChange(groupBuilder, keepProportion, widthField, heightField, 1 / ratio));
        heightField.textProperty()
            .addListener(e -> onResizeOptionsChange(groupBuilder, keepProportion, heightField, widthField, ratio));
        root.getChildren().add(SimpleButtonBuilder.newButton("Resize", e -> {
            finishResize(image, groupBuilder, widthField, heightField, paintModel);
            StageHelper.closeStage(root);
            paintModel.createImageVersion();
        }));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private static void changeIfDifferent(final TextField field, final String toBeValue) {
        if (!field.getText().equals(toBeValue) && Math.abs(tryParse(field) - NumberUtils.toInt(toBeValue, 0)) > 1) {
            field.setText(toBeValue);
        }
    }

    private static void finishResize(final WritableImage image, final SimpleToggleGroupBuilder groupBuilder,
        final TextField widthField, final TextField heightField, PaintModel paintModel) {
        ToggleButton selectedItem = (ToggleButton) groupBuilder.selectedItem();
        double newWidth = PERCENTAGE_FIELD.equals(selectedItem.getText())
            ? tryParse(widthField) * image.getWidth() / 100
            : tryParse(widthField);

        double newHeight = PERCENTAGE_FIELD.equals(selectedItem.getText())
            ? tryParse(heightField) * image.getHeight() / 100
            : tryParse(heightField);
        WritableImage newImage = new WritableImage((int) newWidth, (int) newHeight);
        double width = image.getWidth();
        double height = image.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = image.getPixelReader().getColor(i, j);
                double yRatio = newHeight / height;
                int y = (int) (j * yRatio);
                double xRatio = newWidth / width;
                int x = (int) (i * xRatio);
                if (withinImage(x, y, newImage)) {
                    newImage.getPixelWriter().setColor(x, y, color);
                }
                setPixels(newImage, color, x, y, xRatio, yRatio);
            }
        }
        paintModel.setFinalImage(newImage);
    }

    private static void onResizeOptionsChange(final SimpleToggleGroupBuilder groupBuilder,
        final CheckBox keepProportion, final TextField changedField, final TextField otherField, final double ratio) {
        RadioButton selectedItem = (RadioButton) groupBuilder.selectedItem();
        if (keepProportion.isSelected()) {
            if (PERCENTAGE_FIELD.equals(selectedItem.getText())) {
                changeIfDifferent(otherField, changedField.getText());
                return;
            }
            if (StringUtils.isNumeric(changedField.getText())) {
                int width2 = tryParse(changedField);
                String newHeight = "" + (int) (ratio * width2);
                changeIfDifferent(otherField, newHeight);
            }
        }
    }

    private static void setPixels(WritableImage newImage, Color color, int x, int y, double xRatio, double yRatio) {
        for (int l = 0; l < xRatio; l++) {
            for (int k = 0; k < yRatio; k++) {
                if (withinImage(x + l, y + k, newImage)) {
                    newImage.getPixelWriter().setColor(x + l, y + k, color);
                }
            }
        }
    }

    private static int tryParse(final TextField widthField) {
        return NumberUtils.toInt(widthField.getText().replaceAll("\\D", ""), 0);
    }
}
