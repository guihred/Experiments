package mp3Audio;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.lang3.StringUtils;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag.Genre;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LeitorMusicas {
	private static final Logger LOGGER = LoggerFactory.getLogger(LeitorMusicas.class);

	private LeitorMusicas() {
	}

	public static void main(String[] args) {

		try {
			String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
			File file = new File(new File(path).getParentFile(), "Music");
            ObservableList<Musica>
			musicas = getMusicas(file);
            musicas.forEach(s -> LOGGER.trace("{}", s));
        } catch (Exception e) {
            LOGGER.trace("", e);
		}
	}


    public static ObservableList<Musica> getMusicas(File file) {

		ObservableList<Musica> musicas = FXCollections.observableArrayList();
		Path start = file.toPath();
        try (Stream<Path> find = Files.find(start, 6, (dir, name) -> dir.toFile().getName().endsWith(".mp3"));) {
            find.forEach(
            		path -> musicas.add(readTags(path.toFile())));
        } catch (Exception e) {
            LOGGER.trace("", e);
        }
		


		
		return musicas;
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

	public static Musica readTags(File sourceFile) {
		Musica musica = new Musica();
		String title = "", artist = "", album = "", year = "", track = "", genre2 = "";

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
		extractEmbeddedImageData(mediaFile);


		return musica;
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

	public static byte[] extractEmbeddedImageData(File mp3) {
		return extractEmbeddedImageData(new MP3File(mp3));
	}

	public static byte[] extractEmbeddedImageData(MP3File mp3) {

		try {
			for (ID3Tag tag : mp3.getTags()) {

				if (tag instanceof ID3V2_3_0Tag) {
					ID3V2_3_0Tag tag2 = (ID3V2_3_0Tag) tag;

					if (tag2.getAPICFrames() != null && tag2.getAPICFrames().length > 0) {
						// Simply take the first image that is available.
						APICID3V2Frame frame = tag2.getAPICFrames()[0];
						return frame.getPictureData();
					}
				}
			}
			ID3V2Tag id3v2Tag = mp3.getID3V2Tag();
			ID3V2Frame[] singleFrames = id3v2Tag.getSingleFrames();
            String singleFramesStr = Arrays.toString(singleFrames);
            LOGGER.trace("SingleFrames={}", singleFramesStr);
		} catch (Exception e) {
            LOGGER.trace("", e);
		}
        return new byte[0];
	}

	public static List<String> getImagens(String artista) {
		try {
			HttpTransport transport = new ApacheHttpTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			Customsearch build = new Customsearch.Builder(transport, jsonFactory, null)
					.setCustomsearchRequestInitializer(
							new CustomsearchRequestInitializer("AIzaSyBAsSX8EPLHAZlother07UAMPF7vqBA2dWcisc"))
					.setApplicationName("wdmsim").build();
			Cse cse = build.cse();

			Search execute = cse.list(artista).setCx("001081779786768539865:7f2uwv0iufy").setSearchType("image")
					.execute();

			List<Result> items = execute.getItems();
			if (items != null) {
				return items.stream().map(Result::getLink).collect(Collectors.toList());
			}

		} catch (Exception e) {
            LOGGER.trace("", e);
		}
		return Collections.emptyList();
	}

}