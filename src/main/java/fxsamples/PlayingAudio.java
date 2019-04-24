package fxsamples;

import java.net.MalformedURLException;
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;
import utils.HasLogging;
import utils.ResourceFXUtils;

/**
 * Chapter 7 Playing Audio using JavaFX media API.
 *
 * @author cdea
 */
public class PlayingAudio extends Application {
	private static final Logger LOGGER = HasLogging.log();
	private static final String STOP_BUTTON_ID = "stop-button";
	private static final String PAUSE_BUTTON_ID = "pause-button";
	private static final String CLOSE_BUTTON_ID = "close-button";
	private static final String VIS_CONTAINER_ID = "viz-container";
	private static final String SEEK_POS_SLIDER_ID = "seek-position-slider";
	private MediaPlayer mediaPlayer;
	private Point2D anchorPt;
	private Point2D previousLocation;
	private ChangeListener<Duration> progressListener;
	private Stage mainStage;
    private Group playButton;
    private Group pauseButton;

	/**
	 * After a file is dragged onto the application a new MediaPlayer instance
	 * is created with a media file.
	 *
	 * @param stage
	 *            The stage window (primaryStage)
	 * @param url
	 *            The URL pointing to an audio file
	 */
	public void playMedia(String url) {
		Scene scene = mainStage.getScene();
		if (mediaPlayer != null) {
			mediaPlayer.pause();
			mediaPlayer.setOnPaused(null);
			mediaPlayer.setOnPlaying(null);
			mediaPlayer.setOnReady(null);
			mediaPlayer.currentTimeProperty().removeListener(progressListener);
			mediaPlayer.setAudioSpectrumListener(null);
		}
		Media media = new Media(url);
		// display media's metadata
		for (String s : media.getMetadata().keySet()) {
            LOGGER.info(s);
		}
		mediaPlayer = new MediaPlayer(media);
		// as the media is playing move the slider for progress
		mediaPlayer.currentTimeProperty().addListener(progressListener);

		mediaPlayer.setOnReady(() -> {
			updatePlayAndPauseButtons(false);
			Slider progressSlider = (Slider) scene.lookup("#" + SEEK_POS_SLIDER_ID);
			progressSlider.setValue(0);
			progressSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
			mediaPlayer.play();
		});
			// back to the beginning
		mediaPlayer.setOnEndOfMedia(() -> {
			updatePlayAndPauseButtons(true);
			// change buttons to play and rewind
				mediaPlayer.stop();
		});
				// setup visualization (circle container)
		Group vizContainer = (Group) mainStage.getScene().lookup("#" + VIS_CONTAINER_ID);
		mediaPlayer
				.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
					vizContainer.getChildren().clear();
					int i = 0;
					double y = mainStage.getScene().getHeight() / 2;
					Random rand = new Random(System.currentTimeMillis());
					// Build random colored circles
                    final int bound = 255;
					for (float phase : phases) {
                        int red = rand.nextInt(bound);
						int green = rand.nextInt(bound);
						int blue = rand.nextInt(bound);
						Circle circle = new SimpleCircleBuilder().radius(10)
								.centerX((double) 10 + i)
								.centerY(y + (double) phase * 100)
                                .fill(Color.rgb(red, green, blue, 7. / 10))
								.build();
						vizContainer.getChildren().add(circle);
						i += 5;
					}
				});
	}

	/**
	 * @param args
	 *            the command line arguments
	 */

	@Override
	public void start(Stage primaryStage) {
		mainStage = primaryStage;
		mainStage.centerOnScreen();
		Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.BLACK);
		// load JavaFX CSS style
		mainStage.setScene(scene);
		// Initialize stage to be movable via mouse
		initMovablePlayer();
		// application area
		Node applicationArea = createApplicationArea();
		// Container for random circles bouncing about
		Node vizContainer = new Group();
		vizContainer.setId(VIS_CONTAINER_ID);
		// Create the button panel
		Node buttonPanel = createButtonPanel();
		// Progress and seek position slider
		Slider progressSlider = createSlider();
		// Update slider as video is progressing
		progressListener = (observable, oldValue, newValue) -> progressSlider.setValue(newValue.toSeconds());
		// Initializing to accept files
		// dragged over surface to load media
		initFileDragNDrop();
		// Create the close button
		Node closeButton = createCloseButton();
        root.getChildren().addAll(applicationArea, vizContainer, buttonPanel, progressSlider, closeButton);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("media.css"));

		primaryStage.show();
	}

	/**
	 * A simple rectangular area as the surface of the app.
	 * 
	 * @return Node a Rectangle node.
	 */
	private Node createApplicationArea() {
		Scene scene = mainStage.getScene();
		Rectangle applicationArea = new Rectangle();
        // add selector to style app-area
        applicationArea.setId("app-area");

		// make the app area rectangle the size of the scene.
		applicationArea.widthProperty().bind(scene.widthProperty());
		applicationArea.heightProperty().bind(scene.heightProperty());
		return applicationArea;
	}

	/**
	 * Creates a node containing the audio player's stop, pause and play
	 * buttons.
	 *
	 * @return Node A button panel having play, pause and stop buttons.
	 */
	private Node createButtonPanel() {
		Scene scene = mainStage.getScene();
		// create button control panel
        Group buttonGroup = new Group();
		// Button area
        final Rectangle buttonArea = new Rectangle(60, 35);
		buttonArea.setId("button-area");
		buttonGroup.getChildren().add(buttonArea);
		// stop button control
        Button stopButton = new Button();

		stopButton.setId(STOP_BUTTON_ID);
        SVGPath arc = new SimpleSvgPathBuilder().content("M 6,4 v8l8-4z M9 17 a 9 9 0 1 1 0.1 0").fill(Color.WHITE)
                .build();
        playButton = new Group(arc);
        pauseButton = new Group();
		stopButton.setOnMousePressed(mouseEvent -> {
			if (mediaPlayer != null) {
                updatePlayAndPauseButtons(true);
				if (mediaPlayer.getStatus() == Status.PLAYING) {
					mediaPlayer.stop();
				}
			}
		});
        ToggleButton toggleButton = new ToggleButton(null, pauseButton);
        toggleButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                boolean selected = toggleButton.isSelected();
                updatePlayAndPauseButtons(selected);
                if (selected && mediaPlayer.getStatus() == Status.PLAYING) {
                    mediaPlayer.pause();
                } else if (!selected) {
                    mediaPlayer.play();
                }
            }
        });

        // play button

		// pause control
		pauseButton.setId(PAUSE_BUTTON_ID);

        SVGPath pauseBackground = new SimpleSvgPathBuilder()
                .content("M 4,3 v11h4v-11z M 10,3 v11h4v-11z M9 17 a 9 9 0 1 1 0.1 0")
                .styleClass("pause-circle")
                .fill(Color.WHITE).build();

        pauseButton.getChildren().addAll(pauseBackground);
        buttonGroup.getChildren().addAll(stopButton, toggleButton);
		// move button group when scene is resized
		buttonGroup.translateXProperty().bind(scene.widthProperty().subtract(buttonArea.getWidth() + 6));
		buttonGroup.translateYProperty().bind(scene.heightProperty().subtract(buttonArea.getHeight() + 6));
		return buttonGroup;
	}

	/**
	 * The close button to exit application
	 *
	 * @return Node representing a close button.
	 */
	private Node createCloseButton() {
		Scene scene = mainStage.getScene();
		Group closeButton = new Group();
		closeButton.setId(CLOSE_BUTTON_ID);
        Node closeBackground = new Circle(5, 0, 7, Color.RED);
		closeBackground.setId("close-circle");
		Node closeXmark = new Text(2, 4, "X");
		closeButton.translateXProperty().bind(scene.widthProperty().subtract(15));
		closeButton.setTranslateY(10);
		closeButton.getChildren().addAll(closeBackground, closeXmark);
		// exit app
		closeButton.setOnMouseClicked(mouseEvent -> Platform.exit());
		return closeButton;
	}

	/**
	 * A position slider to seek backward and forward that is bound to a media
	 * player control.
	 *
	 * @return Slider control bound to media player.
	 */
	private Slider createSlider() {
        Slider slider = new SimpleSliderBuilder(0, 100, 1).build();
		slider.setId(SEEK_POS_SLIDER_ID);
		slider.valueProperty().addListener(observable -> {
			if (slider.isValueChanging() && mediaPlayer != null
					&& mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
				// convert seconds to millis
				double dur = slider.getValue() * 1000;
				mediaPlayer.seek(Duration.millis(dur));
			}
		});
		Scene scene = mainStage.getScene();
		slider.setTranslateX(10);
		slider.translateYProperty().bind(scene.heightProperty().subtract(50));
		return slider;
	}

	/**
	 * Initialize the Drag and Drop ability for media files.
	 *
	 */
	private void initFileDragNDrop() {
		Scene scene = mainStage.getScene();
		scene.setOnDragOver(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
			if (db.hasFiles() || db.hasUrl()) {
				dragEvent.acceptTransferModes(TransferMode.LINK);
			} else {
				dragEvent.consume();
			}
		});
		// Dropping over surface
		scene.setOnDragDropped(dragEvent -> {
			Dragboard db = dragEvent.getDragboard();
			boolean success = false;
			if (db.hasFiles()) {
				success = true;
                if (!db.getFiles().isEmpty()) {
					tryPlayMedia(db);
				}
			} else {
				// audio file from some host or jar
				playMedia(db.getUrl());
				success = true;
			}
			dragEvent.setDropCompleted(success);
			dragEvent.consume();
		});
	}

	/**
	 * Initialize the stage to allow the mouse cursor to move the application
	 * using dragging.
	 *
	 */
	private void initMovablePlayer() {
		Scene scene = mainStage.getScene();
		// starting initial anchor point
		scene.setOnMousePressed(mouseEvent -> anchorPt = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY()));
		// dragging the entire stage
		scene.setOnMouseDragged(mouseEvent -> {
			if (anchorPt != null && previousLocation != null) {
				mainStage.setX(previousLocation.getX() + mouseEvent.getScreenX() - anchorPt.getX());
				mainStage.setY(previousLocation.getY() + mouseEvent.getScreenY() - anchorPt.getY());
			}
		});
		// set the current location
		scene.setOnMouseReleased(mouseEvent -> previousLocation = new Point2D(mainStage.getX(), mainStage
				.getY()));
		// Initialize previousLocation after Stage is shown
		mainStage.addEventHandler(WindowEvent.WINDOW_SHOWN,
				t -> previousLocation = new Point2D(mainStage.getX(), mainStage.getY()));
	}

    private void tryPlayMedia(Dragboard db) {
		try {
			String filePath = db.getFiles().get(0).toURI().toURL().toString();
			playMedia(filePath);
		} catch (MalformedURLException ex) {
			LOGGER.error("", ex);
		}
	}
	private void updatePlayAndPauseButtons(boolean playVisible) {
		Scene scene = mainStage.getScene();
        ToggleButton toggle = (ToggleButton) scene.lookup(".toggle-button");
		// hide or show buttons
		if (playVisible) {
			// show play button
            toggle.setGraphic(playButton);
		} else {
            toggle.setGraphic(pauseButton);
			// show pause button
		}
	}

	public static void main(String[] args) {
		launch(args);

	}

}
