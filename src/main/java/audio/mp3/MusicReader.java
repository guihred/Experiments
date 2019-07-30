package audio.mp3;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class MusicReader {
    private static final Logger LOGGER = HasLogging.log();

    private MusicReader() {
    }

    public static Image extractEmbeddedImage(File mp3) {
        try {
            Mp3File mp31 = new Mp3File(mp3);
            if (mp31.hasId3v2Tag()) {
                ID3v2 tag = mp31.getId3v2Tag();
                byte[] albumImage = tag.getAlbumImage();
                if (albumImage != null) {
                    return new Image(new ByteArrayInputStream(albumImage));
                }

            }

        } catch (Exception e) {
            LOGGER.trace("", e);
        }
        return null;
    }

    public static String getDescription(Music selectedItem) {
        return Stream
            .of(selectedItem.getTitulo(), selectedItem.getArtista(), selectedItem.getAlbum(), selectedItem.getPasta())
            .filter(StringUtils::isNotBlank).distinct().map(e -> e.replaceAll("[ /:]", "_"))
            .collect(Collectors.joining(" - "));
    }

    public static ObservableList<Music> getMusicas(File file) {

        ObservableList<Music> musicas = FXCollections.observableArrayList();
        Path start = file.toPath();
        try (Stream<Path> find = Files.find(start, 6, (dir, name) -> dir.toFile().getName().endsWith(".mp3"))) {
            find.forEach(path -> musicas.add(readTags(path.toFile())));
        } catch (Exception e) {
            LOGGER.trace("", e);
        }
        return musicas;
    }

    public static void main(String[] args) {

        try {
            File file = ResourceFXUtils.getUserFolder("Music");
            ObservableList<Music> musicas = getMusicas(file);
            musicas.forEach(s -> LOGGER.info("{}", s));
        } catch (Exception e) {
            LOGGER.trace("", e);
        }
    }

    public static Music readTags(File sourceFile) {
        Music musica = new Music(sourceFile);
        String title = "";
        String artist = "";
        String album = "";
        String year = "";
        String track = "";
        String genre2 = "";
        try {
            Mp3File mediaFile = new Mp3File(sourceFile);

            ID3v1 tagv1 = mediaFile.getId3v1Tag();
            ID3v2 tagv2 = mediaFile.getId3v2Tag();
            if (mediaFile.hasId3v1Tag()) {
                ID3v1 leTag = tagv1;
                title = notNull(leTag.getTitle(), title);
                artist = notNull(leTag.getArtist(), artist);
                album = notNull(leTag.getAlbum(), album);
                year = notNull(leTag.getYear(), year);
                genre2 = notNull(leTag.getGenreDescription(), genre2);
            }
            if (mediaFile.hasId3v2Tag()) {
                ID3v2 leTag = tagv2;
                title = notNull(leTag.getTitle(), title);
                artist = notNull(leTag.getArtist(), artist);
                album = notNull(leTag.getAlbum(), album);
                year = notNull(leTag.getYear(), year);
                track = notNull(leTag.getTrack(), track);
                musica.setImage(extractEmbeddedImage(sourceFile));
                genre2 = notNull(leTag.getGenreDescription(), genre2);
            }
        } catch (Exception e) {
            LOGGER.error("ERROR FILE {}", sourceFile);
            LOGGER.trace("ERROR FILE {}", sourceFile, e);
        }

        if (genre2 == null || genre2.indexOf('(') == 0 || "".equals(genre2)) {
            genre2 = "Undefined";
        }
        musica.setTitulo(StringUtils.isBlank(title) ? sourceFile.getName().replaceAll("\\.mp3", "") : title);
        musica.setGenero(genre2);
        musica.setArtista(artist);
        musica.setAlbum(album);
        musica.setAno(year);
        musica.setTrilha(track);
        musica.setArquivo(sourceFile);
        return musica;
    }

    public static void saveMetadata(Music a) {
        saveMetadata(a, a.getArquivo());
    }

    public static void saveMetadata(Music a, File file) {

        File file2 = ResourceFXUtils.getOutFile("copy_" + file.getName());
        try {
            Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Mp3File mp3File = new Mp3File(file);
            mp3File.removeId3v1Tag();
            ID3v2 tags = mp3File.hasId3v2Tag() ? mp3File.getId3v2Tag() : new ID3v24Tag();
            tags.setAlbum(a.getAlbum());
            tags.setArtist(a.getArtista());
            tags.setTitle(a.getTitulo());
            tags.setYear(a.getAno());
            if (a.getImage() != null) {
                String value = getDescription(a);
                Image image = a.getImage();
                File destination = ResourceFXUtils.getOutFile("test" + value + ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", destination);
                byte[] allBytes = Files.readAllBytes(destination.toPath());
                tags.setAlbumImage(allBytes, "image/png");
            }
            mp3File.setId3v2Tag(tags);
            mp3File.save(file2.getAbsolutePath());

            if (Files.isWritable(file.toPath())) {
                copyFileBack(file, file2);
            }
            Files.deleteIfExists(file2.toPath());
            LOGGER.info("Saving {} in {}", a, file);
        } catch (Exception e) {
            copyFileBack(file, file2);

            LOGGER.error("", e);
        }
    }

    private static void copyFileBack(File file, File file2) {
        try {
            Files.copy(file2.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(file2.toPath());
        } catch (IOException e1) {
            LOGGER.error("ERROR COPYING", e1);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T notNull(T... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] != null) {
                return objects[i];
            }
        }
        return null;
    }

}