package audio.mp3;

import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

public class Music {
	private StringProperty album = new SimpleStringProperty("");

	private StringProperty ano = new SimpleStringProperty("");
    private Image image;

	private File arquivo;

	private StringProperty artista = new SimpleStringProperty("");

	private StringProperty genero = new SimpleStringProperty("");

	private StringProperty titulo = new SimpleStringProperty("");

    private StringProperty trilha = new SimpleStringProperty("");
    private StringProperty pasta = new SimpleStringProperty("");

    public Music() {
    }

    public Music(File file) {
        setArquivo(file);
        setTitulo(file.getName());
        setPasta(file.getParentFile().getName());
    }

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

	public int getAnoInt() {
        return StringUtils.isNumeric(ano.get()) ? Integer.parseInt(ano.get()) : 2000;
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

	public Image getImage() {
        return image;
    }

	public String getPasta() {
        return pasta.get();
    }

	public String getTitulo() {
		return titulo.get();
	}

	public String getTrilha() {
		return trilha.get();
	}

	public StringProperty pastaProperty() {
	    return pasta;
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

	public void setImage(Image image) {
        this.image = image;
    }

    public void setPasta(String pasta) {
        this.pasta.set(pasta);
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
        return String.format("[%s, %s, %s, %s, %s, %s, %s]", titulo.get(), artista.get(), album.get(), ano.get(),
            trilha.get(), genero.get(), arquivo.getPath());
    }

    public StringProperty trilhaProperty() {
		return trilha;
	}
}