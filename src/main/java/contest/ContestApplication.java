package contest;

import static utils.CommonsFX.onCloseWindow;
import static utils.ResourceFXUtils.convertToURL;
import static utils.SupplierEx.get;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.HibernateUtil;
import utils.ResourceFXUtils;

public class ContestApplication extends Application {

    private ContestReader contestQuestions;

    public ContestApplication() {

    }

    public ContestApplication(ContestReader contestQuestions) {
        this.contestQuestions = contestQuestions;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Contest Questions");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(convertToURL(ResourceFXUtils.toFile("ContestApplication.fxml")));
        Scene scene = new Scene(get(fxmlLoader::load));
        ContestApplicationController controller = fxmlLoader.getController();
        if (contestQuestions != null) {
            controller.setContestQuestions(contestQuestions);
        }
        primaryStage.setTitle("Contest Questions");
        primaryStage.setScene(scene);
        if (contestQuestions == null) {
            onCloseWindow(primaryStage, HibernateUtil::shutdown);
        }
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
