/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch08;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import mp3Audio.LeitorMusicas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SongModel {
	protected transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_IMG_URL
            = new File("C:\\Users\\Note\\Pictures\\fb.jpg").toURI().toString();
    private static final Image DEFAULT_ALBUM_COVER
            = new Image(DEFAULT_IMG_URL);
    private final StringProperty album
            = new SimpleStringProperty(this, "album");
    private final StringProperty artist
            = new SimpleStringProperty(this, "artist");
    private final StringProperty title
            = new SimpleStringProperty(this, "title");
    private final StringProperty year
            = new SimpleStringProperty(this, "year");
    private final ObjectProperty<Image> albumCover
            = new SimpleObjectProperty<>(this, "albumCover");
    private final ReadOnlyObjectWrapper<MediaPlayer> mediaPlayer
            = new ReadOnlyObjectWrapper<>(this, "mediaPlayer");

    public SongModel() {
        resetProperties();
    }
    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty titleProperty() {
        return title;
    }
    public StringProperty yearProperty() {
        return year;
    }
    public ObjectProperty<Image> albumCoverProperty() {
        return albumCover;
    }
    public void setURL(String url) {
        if (mediaPlayer.get() != null) {
            mediaPlayer.get().stop();
        }
        initializeMedia(url);
    }

    public String getAlbum() {
        return album.get();
    }

    public void setAlbum(String value) {
        album.set(value);
    }

    public StringProperty albumProperty() {
        return album;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer.get();
    }

	public ObjectProperty<MediaPlayer> mediaPlayerProperty() {
		return mediaPlayer;
    }

    private void resetProperties() {
        setArtist("");
        setAlbum("");
        setTitle("");
        setYear("");
        setAlbumCover(DEFAULT_ALBUM_COVER);
    }

    private void initializeMedia(String url) {
        resetProperties();
        try {
            final Media media = new Media(url);
			media.getMetadata().addListener(
					(MapChangeListener<String, Object>) ch -> handleMetadata(ch.getKey(), ch.getValueAdded()));
			Platform.runLater(() -> {
				tryGetAlbumCover(url);
			});

            mediaPlayer.setValue(new MediaPlayer(media));

            mediaPlayer.get().setOnError(() -> {
                String errorMessage = mediaPlayer.get().getError().getMessage();
                System.out.println("MediaPlayer Error: " + errorMessage);
            });
        } catch (RuntimeException re) {
			logger.error("", re);
            System.out.println("Caught Exception: " + re.getMessage());
        }
    }

	private void tryGetAlbumCover(String url) {
		try {
			byte[] extractEmbeddedImageData = LeitorMusicas
					.extractEmbeddedImageData(new File(new URL(URLDecoder.decode(url, "UTF-8")).getFile()));
			setAlbumCover(new Image(new ByteArrayInputStream(extractEmbeddedImageData)));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

    private void handleMetadata(String key, Object value) {
		System.out.println("Key=" + key + ",Value=" + value);
		if ("album".equals(key)) {
            setAlbum(value.toString());
		} else if ("artist".equals(key)) {
            setArtist(value.toString());
        }
		if ("title".equals(key)) {
            setTitle(value.toString());
        }
		if ("year".equals(key)) {
            setYear(value.toString());
        }
		if ("image".equals(key)) {
            setAlbumCover((Image) value);
        }

    }

    void setArtist(String value) {
        artist.setValue(value);
    }

    public String getArtist() {
        return artist.get();
    }
    public String getTitle() {
        return title.get();
    }

    public Image getAlbumCover() {
        return albumCover.get();
    }


    void setTitle(String value) {
        title.setValue(value);
    }

    void setYear(String value) {
        year.setValue(value);
    }

    public MediaPlayer getPlayer() {
        return mediaPlayer.get();
    }

    void setAlbumCover(Image value) {
        albumCover.setValue(value);
    }
}
