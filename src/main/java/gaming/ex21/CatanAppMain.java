package gaming.ex21;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class CatanAppMain extends Application {
    @FXML
    private StackPane center;

    @FXML
    private Button makeDeal;

    @FXML
    private Button exchange;
    @FXML
    private Button skipTurn;
    @FXML
    private UserChart userChart;
    @FXML
    private VBox dealsBox;
    @FXML
    private HBox resourceChoices;
    @FXML
    private Button throwDices;
    @FXML
    private GridPane combinationGrid;

    private CatanModel catanModel = new CatanModel();

    public CatanModel getModel() {
        return catanModel;
    }

    public void initialize() {
        center.setPrefWidth(700);
        catanModel.setEdges(CatanHelper.addTerrains(center, catanModel.getSettlePoints(), catanModel.getTerrains(),
            catanModel.getPorts()));
        catanModel.setUserChart(userChart);
        catanModel
            .setResourceChoices(ResourceType.createResourceChoices(catanModel::onSelectResource, resourceChoices));
        catanModel.setExchangeButton(exchange);
        catanModel.setMakeDeal(makeDeal);
        catanModel.getElements().addListener(ListHelper.onChangeElement(center));
        catanModel.currentPlayerProperty().addListener((ob, old, newV) -> catanModel.onChangePlayer(newV));
        skipTurn.disableProperty()
            .bind(Bindings.createBooleanBinding(
                () -> CatanHelper.isSkippable(catanModel.getDiceThrown(), resourceChoices,
                    catanModel.getElements(), catanModel.currentPlayerProperty()),
                catanModel.getDiceThrown(), resourceChoices.visibleProperty(),
                catanModel.currentPlayerProperty(), catanModel.getElements()));
        throwDices.disableProperty().bind(catanModel.getDiceThrown());
        ListHelper.newDeal(dealsBox, catanModel.getDeals(),
            t -> Deal.isDealUnfeasible(t, catanModel.getCurrentPlayer(), catanModel.getCards()), catanModel::onMakeDeal,
            catanModel.currentPlayerProperty(), catanModel.getDiceThrown());
        makeDeal.setDisable(true);
        catanModel.setCurrentPlayer(PlayerColor.BLUE);
        catanModel.onSkipTurn();
        userChart.setOnWin(() -> start((Stage) userChart.getScene().getWindow()));
        Combination.combinationGrid(combinationGrid, catanModel::onCombinationClicked, catanModel::disableCombination,
            catanModel.currentPlayerProperty(), catanModel.getDiceThrown());
    }

    public void onActionExchange() {
        catanModel.setResourceSelect(SelectResourceType.EXCHANGE);
    }

    public void onActionMakeDeal() {
        catanModel.setResourceSelect(SelectResourceType.MAKE_DEAL);
    }

    public void onActionSkipTurn() {
        catanModel.onSkipTurn();
    }

    public void onActionThrowDices() {
        catanModel.throwDice();
    }

    public void onMouseDraggedStackPane0(MouseEvent e) {
        catanModel.getDragContext().dragElement(e, catanModel.getSettlePoints(), catanModel.getTerrains(),
            catanModel.getEdges());
    }

    public void onMousePressedStackPane0(MouseEvent e) {
        catanModel.getDragContext().pressElement(e, center, catanModel.getElements());
    }

    public void onMouseReleasedStackPane0(MouseEvent e) {
        catanModel.handleMouseReleased(e.getX(), e.getY());
    }

    @Override
    public void start(Stage primaryStage) {
        double size = CatanResource.RADIUS * Math.sqrt(3) * 11 / 2;
        CommonsFX.loadFXML("Settlers of Catan", "CatanApp.fxml", this, primaryStage, size * 3, size);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
