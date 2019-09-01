package contest.db;

import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Concurso {
    private String url;
    private String nome;
    private ObservableList<String> vagas = FXCollections.observableArrayList();
    private ObservableList<Map.Entry<String, String>> linksFound = FXCollections.observableArrayList();

    public ObservableList<Map.Entry<String, String>> getLinksFound() {
        return linksFound;
    }

    public String getNome() {
        return nome;
    }

    public String getUrl() {
        return url;
    }

    public ObservableList<String> getVagas() {
        return vagas;
    }

    public void setLinksFound(List<Map.Entry<String, String>> linksFound) {
        this.linksFound.clear();
        this.linksFound.addAll(linksFound);
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVagas(ObservableList<String> vagas) {
        this.vagas = vagas;
    }
}