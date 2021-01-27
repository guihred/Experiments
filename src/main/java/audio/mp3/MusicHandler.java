package audio.mp3;

import extract.Music;
import extract.MusicReader;
import extract.SongUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleVBoxBuilder;
import simplebuilder.StageHelper;
import utils.*;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;
import utils.fx.AutocompleteField;

public final class MusicHandler implements EventHandler<MouseEvent> {
    private final TableView<Music> musicaTable;

    public MusicHandler(@NamedArg("musicaTable") TableView<Music> musicaTable) {
        this.musicaTable = musicaTable;
    }

    public TableView<Music> getMusicaTable() {
        return musicaTable;
    }

    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            handleMousePressed(getMusicaTable().getSelectionModel().getSelectedItems());
        }
    }

    @Override
    public void handle(MouseEvent e) {
        if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
            handleMousePressed(getMusicaTable().getSelectionModel().getSelectedItems());
        }
    }

    public static void fixSongs(TableView<Music> musicasTable) {
        ObservableList<Music> items = musicasTable.getItems();
        Music music = items.stream()
                .filter(m -> StringUtils.isBlank(m.getArtista()) || StringUtils.isBlank(m.getAlbum())
                        || m.getTitulo().contains("-") || m.getArtista().contains("/")
                        || m.toString().contains("mari brasil mix"))
                .findFirst().orElseGet(() -> musicasTable.getSelectionModel().getSelectedItem());
        if (music == null) {
            return;
        }
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        List<String> fields = ClassReflectionUtils.getFields(Music.class);
        Map<String, AutocompleteField> mappedFields = new LinkedHashMap<>();
        for (String name : fields) {
            Object fieldValue = ClassReflectionUtils.getFieldValue(music, name);
            if (fieldValue instanceof StringProperty) {
                StringProperty a = (StringProperty) fieldValue;
                AutocompleteField textField = new AutocompleteField();
                textField.textProperty().bindBidirectional(a);
                mappedFields.put(name, textField);
                String fieldName = StringSigaUtils.changeCase(name);
                vBox.getChildren().add(SimpleVBoxBuilder.newVBox(fieldName, textField));
            }
        }
        RunnableEx.runNewThread(() -> makeFieldMap(items, mappedFields.keySet()), o -> {
            if ("1.0".equals(music.getVersion())) {
                o.put("genero", MusicReader.getID3v1Genres());
            }

            CommonsFX.runInPlatform(() -> o.forEach((name, entries) -> mappedFields.get(name).setEntries(entries)));
        });
        if (StringUtils.isBlank(music.getAlbum())) {
            music.setAlbum(music.getPasta());
        }
        Image imageData = music.getImage();
        if (imageData != null) {
            vBox.getChildren().addAll(MusicHandler.view(imageData));
        }
        vBox.getChildren().add(SimpleButtonBuilder.newButton("_Fix", f -> {
            MusicReader.saveMetadata(music);
            StageHelper.closeStage(vBox);
        }));
        new SimpleDialogBuilder().title("Fix Song").text("Fix Fields").node(vBox).bindWindow(musicasTable)
                .displayDialog();

    }

    public static void handleMousePressed(List<Music> songs) {
        if (songs.isEmpty() || songs.stream().anyMatch(e -> !e.getArquivo().exists())) {
            return;
        }
        Music music = songs.get(0);
        if (!music.isNotMP3()) {
            new SimpleDialogBuilder().show(EditSongController.class, music);

            return;
        }
        SimpleDialogBuilder dialog = new SimpleDialogBuilder();
        dialog.text("Convert");
        for (Music m : songs) {
            dialog.text(String.format("%n%s", m.getArquivo().getName()));
        }
        dialog.button("_Convert to Mp3", () -> {
            DoubleBinding finalResult = new SimpleDoubleProperty(0).add(0);
            for (Music m : songs) {
                DoubleProperty convertToAudio = SongUtils.convertToAudio(m.getArquivo());
                finalResult = finalResult.add(convertToAudio);
            }
            return finalResult.divide(songs.size());
        }, () -> ConsumerEx.foreach(songs, s -> onConvertionEnded(s.getArquivo())));
        dialog.displayDialog();
    }

    public static void handleMousePressed(Music songs) {
        handleMousePressed(Arrays.asList(songs));
    }

    public static ImageView view(Image music) {
        ImageView imageView = new ImageView(music);
        final int prefWidth = 50;
        imageView.setFitWidth(prefWidth);
        imageView.setPreserveRatio(true);
        return imageView;
    }


    private static Map<String, Set<String>> makeFieldMap(ObservableList<Music> items,
            Collection<String> autocompleteMap) {
        return items.stream().flatMap(mu -> autocompleteMap.stream().map(FunctionEx.ignore(name -> {
            Object fieldValue = ClassReflectionUtils.getFieldValue(mu, name);
            StringProperty a = (StringProperty) fieldValue;
            return new SimpleEntry<>(name, a.get());
        })).filter(Objects::nonNull)).collect(Collectors.groupingBy(SimpleEntry<String, String>::getKey,
                Collectors.mapping(SimpleEntry<String, String>::getValue, Collectors.toSet())));
    }

    private static void onConvertionEnded(File arquivo) throws IOException {
        File file = new File(arquivo.getParentFile(), arquivo.getName().replaceAll("\\..+", ".mp3"));
        if (!file.exists()) {
            return;
        }
        Path path = arquivo.toPath();
        String name = path.toFile().getName();
        File outFile = ResourceFXUtils.getOutFile(name.replaceAll(".+\\.(\\w+)$", "$1") + "/" + name);
        ExtractUtils.copy(path, outFile);
        Files.deleteIfExists(path);
    }

}