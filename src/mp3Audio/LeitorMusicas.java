package mp3Audio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.commons.lang3.StringUtils;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v1.ID3V1Tag.Genre;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

public class LeitorMusicas {

	public static void main(String[] args) throws IOException {
		File file = new File("C:/Users/Guilherme/Music");
		getMusicas(file);
	}


	public static ObservableList<Musica> getMusicas(File file) throws IOException {

		ObservableList<Musica> musicas = FXCollections.observableArrayList();
		Path start = file.toPath();
		Files.find(start, 6, (dir, name) -> dir.toFile().getName().endsWith(".mp3")).forEach(
				path -> musicas.add(readTags(path.toFile())));
		


		
		return musicas;
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
					if (leTag.getTitle() != null) {
						title = leTag.getTitle();
					}
					if (leTag.getArtist() != null) {
						artist = leTag.getArtist();
					}
					if (leTag.getAlbum() != null) {
						album = leTag.getAlbum();
					}
					try {
						year = "" + leTag.getYear();
					} catch (Exception e) {
					}
					if (leTag.getGenre() != null) {
						genre = leTag.getGenre();
					}

				} else if (lesTags[i] instanceof ID3V2_3_0Tag) {
					ID3V2_3_0Tag leTag = (ID3V2_3_0Tag) lesTags[i];
					if (leTag.getTitle() != null) {
						title = leTag.getTitle();
					}
					if (leTag.getArtist() != null) {
						artist = leTag.getArtist();
					}
					if (leTag.getAlbum() != null) {
						album = leTag.getAlbum();
					}
					try {
						year = "" + leTag.getYear();
					} catch (Exception e) {
					}
					try {
						track = "" + leTag.getTrackNumber();
					} catch (Exception e) {
					}
					if (leTag.getGenre() != null) {
						genre2 = leTag.getGenre();
					}
				}
			}

			if (v1) {
				genre2 = genre.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (genre2.indexOf("(") == 0 || genre2.equals("")) {
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
		} catch (ID3Exception e) {
			e.printStackTrace();
		}
		return null;
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
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

}