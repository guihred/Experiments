package audio.mp3;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag.Genre;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class MusicReader {
	private static final Logger LOGGER = HasLogging.log();

	private MusicReader() {
	}

	public static ObservableList<Music> getMusicas(File file) {

		ObservableList<Music> musicas = FXCollections.observableArrayList();
		Path start = file.toPath();
        try (Stream<Path> find = Files.find(start, 6, (dir, name) -> dir.toFile().getName().endsWith(".mp3"))) {
            find.forEach(
            		path -> musicas.add(readTags(path.toFile())));
        } catch (Exception e) {
            LOGGER.trace("", e);
        }
		return musicas;
	}


    public static void main(String[] args) {

		try {
            File file = ResourceFXUtils.getUserFolder("Music");
            ObservableList<Music> musicas = getMusicas(file);
            musicas.forEach(s -> LOGGER.trace("{}", s));
        } catch (Exception e) {
            LOGGER.trace("", e);
		}
	}

    public static Music readTags(File sourceFile) {
		Music musica = new Music();
        String title = "";
        String artist = "";
        String album = "";
        String year = "";
        String track = "";
        String genre2 = "";
		MP3File mediaFile = new MP3File(sourceFile);
		boolean v1 = false;
		try {

			ID3Tag[] lesTags = mediaFile.getTags();
			Genre genre = Genre.Undefined;
			for (int i = 0; i < lesTags.length; i++) {
				if (lesTags[i] instanceof ID3V1_0Tag) {
					v1 = true;
					ID3V1_0Tag leTag = (ID3V1_0Tag) lesTags[i];
                    title = notNull(leTag.getTitle(), title);
                    artist = notNull(leTag.getArtist(), artist);
                    album = notNull(leTag.getAlbum(), album);
					year = trySetYear(year, leTag);
                    genre = notNull(leTag.getGenre(), genre);

				} else if (lesTags[i] instanceof ID3V2_3_0Tag) {
					ID3V2_3_0Tag leTag = (ID3V2_3_0Tag) lesTags[i];
                    title = notNull(leTag.getTitle(), title);
                    artist = notNull(leTag.getArtist(), artist);
                    album = notNull(leTag.getAlbum(), album);
					year = trySetYear(year, leTag);
					track = trySetTrack(track, leTag);
                    genre2 = notNull(leTag.getGenre(), genre2);
				}
			}

			if (v1) {
				genre2 = genre.toString();
			}

		} catch (Exception e) {
            LOGGER.trace("", e);
		}

		if (genre2.indexOf('(') == 0 || "".equals(genre2)) {
			genre2 = "Undefined";
		}
		musica.setTitulo(StringUtils.isBlank(title) ? sourceFile.getName() : title);
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

        try {
            MP3File mp3File = new MP3File(file);
            ID3V1_0Tag tags = new ID3V1_0Tag();
            tags.setAlbum(a.getAlbum());
            tags.setArtist(a.getArtista());
            tags.setTitle(a.getTitulo());
            tags.setYear(a.getAno());
            mp3File.setID3Tag(tags);
            mp3File.sync();
        } catch (Exception e) {
			LOGGER.error("", e);
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


	private static String trySetTrack(String track, ID3V2_3_0Tag leTag) {
		try {
			return Integer.toString(leTag.getTrackNumber());
		} catch (Exception e) {
            LOGGER.trace("", e);
		}
		return track;
	}

	private static String trySetYear(String y, ID3Tag leTag) {
		try {
			if (leTag instanceof ID3V1_0Tag) {
				return ((ID3V1_0Tag) leTag).getYear();
			}
			if (leTag instanceof ID3V2_3_0Tag) {
				return Integer.toString(((ID3V2_3_0Tag) leTag).getYear());
			}

		} catch (Exception e) {
            LOGGER.trace("", e);
		}
		return y;
	}





}