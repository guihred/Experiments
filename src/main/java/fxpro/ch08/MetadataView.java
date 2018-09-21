/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch08;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import utils.ResourceFXUtils;

/**
 *
 * @author Note
 */
public class MetadataView extends BaseSongView {

	public static final String DEFAULT_PICTURE = ResourceFXUtils.toExternalForm("fb.jpg");

    public MetadataView(SongModel song) {
        super(song);
    }

    @Override
    protected Node initView() {
        Label artist = new Label();
        artist.setId("artist");
        artist.textProperty().bind(songModel.artistProperty());
        Label album = new Label();
        album.setId("album");
        album.textProperty().bind(songModel.albumProperty());
        Label title = new Label();
        title.textProperty().bind(songModel.titleProperty());
        title.setId("title");
        Label year = new Label();
        year.setId("year");
        year.textProperty().bind(songModel.yearProperty());
        final Reflection reflection = new Reflection();
        reflection.setFraction(0.2);
		final Image image = new Image(DEFAULT_PICTURE);
        ImageView albumCover = new ImageView(image);
        albumCover.setFitWidth(240);
        albumCover.setPreserveRatio(true);
        albumCover.setSmooth(true);
        albumCover.setEffect(reflection);
        albumCover.imageProperty().bind(songModel.albumCoverProperty());
        final GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(20);
        gp.add(title, 1, 0);
        gp.add(artist, 1, 1);
		gp.add(albumCover, 0, 0, 1, GridPane.REMAINING);
        gp.add(album, 1, 2);
        gp.add(year, 1, 3);
		final ColumnConstraints c1 = new ColumnConstraints();
        final ColumnConstraints c0 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0, c1);
        final RowConstraints r0 = new RowConstraints();
        r0.setValignment(VPos.TOP);
        gp.getRowConstraints().addAll(r0, r0, r0, r0);
        return gp;
    }

}
