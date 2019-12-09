package extract;

import java.io.File;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import utils.ResourceFXUtils;

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
        arquivo = file;
        titulo.set(file.getName());
        pasta.set(file.getParentFile().getName());
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Music other = (Music) obj;
        return Objects.equals(album.get(), other.album.get()) && Objects.equals(artista.get(), other.artista.get())
            && Objects.equals(image != null, other.image != null) && Objects.equals(titulo.get(), other.titulo.get());
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
        if (StringUtils.isNumeric(ano.get())) {
            return Integer.parseInt(ano.get());
        }
        return ResourceFXUtils.getYearCreation(arquivo.toPath());
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

    @Override
    public int hashCode() {
        return Objects.hash(album, ano, arquivo, artista, genero, image, pasta, titulo, trilha);
    }

    public boolean isNotMP3() {
        return !arquivo.getName().matches(".+\\.mp3");
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