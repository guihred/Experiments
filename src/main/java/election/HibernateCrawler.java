package election;

import java.util.List;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.CrawlerTask;
import utils.ImageTableCell;

public class HibernateCrawler extends Application {
    private CandidatoDAO candidatoDAO = new CandidatoDAO();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hibernate Entities");

        IntegerProperty maxResult = new SimpleIntegerProperty(100);
        List<Candidato> list = candidatoDAO.list(0, maxResult.get());
        VBox root = new VBox();
        ObservableList<Object> value = FXCollections.observableArrayList();
        value.addAll(list);
        TableView<Object> build = new SimpleTableViewBuilder<>()
            .addColumn("fotoUrl", "fotoUrl", s -> new ImageTableCell<>())
            .addColumn("nome", "nome")
            .addColumn("numero", "numero")
            .addColumn("nomeCompleto", "nomeCompleto")
            .addColumn("cargo", "cargo")
            .addColumn("cidade", "cidade")
            .addColumn("nascimento", "nascimento")
            .addColumn("ocupacao", "ocupacao")
            .addColumn("partido", "partido")
            .addColumn("votos", "votos")
            .addColumn("eleito", "eleito")
            .equalColumns().items(value).build();

        root.getChildren().add(build);

        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }
}
