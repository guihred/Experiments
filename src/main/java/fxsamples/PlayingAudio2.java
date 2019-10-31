package fxsamples;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;

/**
 * Chapter 7 Playing Audio using JavaFX media API.
 *
 * @author cdea
 */
public class PlayingAudio2 extends PlayingAudio {
    private int green = rnd();
    @Override
    protected void onAudioSpectrum(double timestamp, double duration, float[] magnitudes, float[] phases) {
        vizContainer.getChildren().clear();
        double y = mainStage.getScene().getHeight() / 2;
        // Build random colored circles
        final int j = 8;
        for (int i = 0; i < magnitudes.length / 2; i++) {
            double d = magnitudes[i];
            Rectangle circle = new SimpleRectangleBuilder().width(j).x(5. + i * j).height(y / 2 + d * 2)
                .y(y * 3 / 4 - d).fill(Color.rgb(i * 255 / phases.length, 255, green, 7. / 10)).build();
            vizContainer.getChildren().add(circle);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
