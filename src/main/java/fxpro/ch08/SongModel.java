/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import extract.MusicReader;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class SongModel {


    private static final Image DEFAULT_ALBUM_COVER
			= new Image(MetadataView.DEFAULT_PICTURE);
    private static final Logger LOG = HasLogging.log();
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

    public ObjectProperty<Image> albumCoverProperty() {
        return albumCover;
    }
    public StringProperty albumProperty() {
        return album;
    }
    public StringProperty artistProperty() {
        return artist;
    }
    public String getAlbum() {
        return album.get();
    }

    public Image getAlbumCover() {
        return albumCover.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer.get();
    }

    public MediaPlayer getPlayer() {
        return mediaPlayer.get();
    }

	public String getTitle() {
        return title.get();
    }

    public ObjectProperty<MediaPlayer> mediaPlayerProperty() {
		return mediaPlayer;
    }

    public void setAlbum(String value) {
        album.set(value);
    }

	public void setURL(String url) {
        if (mediaPlayer.get() != null) {
            mediaPlayer.get().stop();
        }
        initializeMedia(url);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty yearProperty() {
        return year;
    }

    private void handleMetadata(String key, Object value) {
        LOG.trace("Key={},Value={}", key, value);
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
        if ("image".equals(key) && value instanceof Image) {
            setAlbumCover(ImageFXUtils.imageCopy((Image) value));
        }

    }

    private void initializeMedia(String url) {
        resetProperties();
        RunnableEx.run(() -> {
            final Media media = new Media(url);
            media.getMetadata()
                .addListener((MapChangeListener<String, Object>) ch -> handleMetadata(ch.getKey(), ch.getValueAdded()));
            CommonsFX.runInPlatform(() -> tryGetAlbumCover(url));
            mediaPlayer.setValue(new MediaPlayer(media));
            mediaPlayer.getValue().volumeProperty().set(0);
            mediaPlayer.get()
                .setOnError(() -> LOG.trace("MediaPlayer Error: {}", mediaPlayer.get().getError().getMessage()));
        });
    }
    private void resetProperties() {
        setArtist("");
        setAlbum("");
        setTitle("");
        setYear("");
        setAlbumCover(DEFAULT_ALBUM_COVER);
    }

    private void setAlbumCover(Image value) {
        albumCover.setValue(value);
    }


    private void setArtist(String value) {
        artist.setValue(value);
    }

    private void setTitle(String value) {
        title.setValue(value);
    }

    private void setYear(String value) {
        year.setValue(value);
    }

    private void tryGetAlbumCover(String url) {
		try {
            Image extractEmbeddedImageData = MusicReader
                    .extractEmbeddedImage(new File(new URL(URLDecoder.decode(url, "UTF-8")).getFile()));
            setAlbumCover(extractEmbeddedImageData);
		} catch (Exception e) {
            LOG.error("", e);
		}
	}
}
