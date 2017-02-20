package mp3Audio;

import java.io.File;
import java.text.MessageFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Musica {
	private StringProperty album = new SimpleStringProperty("");

	private StringProperty ano = new SimpleStringProperty("");

	private File arquivo;

	private StringProperty artista = new SimpleStringProperty("");

	private StringProperty genero = new SimpleStringProperty("");

	private StringProperty titulo = new SimpleStringProperty("");

	private StringProperty trilha = new SimpleStringProperty("");

	public StringProperty albumProperty() {
		return album;
	}

	public StringProperty anoProperty() {
		return ano;
	}

	public StringProperty artistaProperty() {
		return artista;
	}

	public StringProperty generoProperty() {
		return genero;
	}

	public String getAlbum() {
		return album.get();
	}

	public String getAno() {
		return ano.get();
	}

	public File getArquivo() {
		return arquivo;
	}

	public String getArtista() {
		return artista.get();
	}

	public String getGenero() {
		return genero.get();
	}

	public String getTitulo() {
		return titulo.get();
	}

	public String getTrilha() {
		return trilha.get();
	}

	public void setAlbum(String album) {
		this.album.set(album);
	}

	public void setAno(String ano) {
		this.ano.set(ano);
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	public void setArtista(String artista) {
		this.artista.set(artista);
	}

	public void setGenero(String genero) {
		this.genero.set(genero);
	}

	public void setTitulo(String titulo) {
		this.titulo.set(titulo);
	}

	public void setTrilha(String trilha) {
		this.trilha.set(trilha);
	}

	public StringProperty tituloProperty() {
		return titulo;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"Musica [titulo={0}, artista={1}, album={2}, ano={3}, trilha={4}, genero={5}, arquivo={6}]",
				titulo.get(), artista.get(), album.get(), ano.get(), trilha.get(), genero.get(), arquivo.getPath());
	}

	public StringProperty trilhaProperty() {
		return trilha;
	}
}