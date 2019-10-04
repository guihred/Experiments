package gaming.ex21;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
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

    private CatanModel catanModel;

	@FXML
	private GridPane combinationGrid;

    public CatanModel getModel() {
		return catanModel;
	}

	public void initialize() {
        catanModel = new CatanModel(center);
        catanModel
				.setEdges(Terrain.addTerrains(center, catanModel.getSettlePoints(), catanModel.getTerrains(),
						catanModel.getPorts()));
		catanModel.setUserChart(userChart);
		catanModel.setResourceChoices(ResourceType.createResourceChoices(catanModel::onSelectResource, resourceChoices));
		catanModel.setExchangeButton(exchange);
		catanModel.setMakeDeal(makeDeal);
        catanModel.getElements().addListener(ListHelper.onChangeElement(center));
		catanModel.currentPlayerProperty().addListener((ob, old, newV) -> catanModel.onChangePlayer(newV));
        skipTurn.disableProperty().bind(Bindings.createBooleanBinding(catanModel::isSkippable, catanModel.getDiceThrown(),
						catanModel.getResourceChoices().visibleProperty(), catanModel.currentPlayerProperty(),
						catanModel.getElements()));
        throwDices.disableProperty().bind(catanModel.getDiceThrown());
		ListHelper.newDeal(dealsBox, catanModel.getDeals(),
				t -> Deal.isDealUnfeasible(t, catanModel.currentPlayerProperty(), catanModel.getCards()),
				catanModel::onMakeDeal,
				catanModel.currentPlayerProperty(), catanModel.getDiceThrown());
        makeDeal.setDisable(true);
		catanModel.setCurrentPlayer(PlayerColor.BLUE);
        catanModel.onSkipTurn();
        userChart.setOnWin((t, u) -> initialize());
        catanModel.combinationGrid(combinationGrid) ;
    }
    public void onActionExchange() {
        catanModel.setResourceSelect(SelectResourceType.EXCHANGE);
    }

    public void onActionMakeDeal() {
        catanModel.setResourceSelect(SelectResourceType.MAKE_DEAL);
    }

    public void onActionROAD(ActionEvent e) {
        Combination combination = Combination.valueOf(((Button) e.getSource()).getId());
        catanModel.onCombinationClicked(combination);
    }

    public void onActionSkipTurn() {
        catanModel.onSkipTurn();
    }

    public void onActionThrowDices() {
        catanModel.throwDice();
    }

    public void onMouseDraggedStackPane0(MouseEvent e) {
        catanModel.handleMouseDragged(e);
    }

    public void onMousePressedStackPane0(MouseEvent e) {
        catanModel.handleMousePressed(e);
    }

    public void onMouseReleasedStackPane0(MouseEvent e) {
        catanModel.handleMouseReleased(e);
    }

    @Override
    public void start(Stage primaryStage) {
        double size = CatanResource.RADIUS * Math.sqrt(3) * 11 / 2;
        CommonsFX.loadFXML("Settlers of Catan", "CatanApp.fxml", this, primaryStage, size * 3 / 2, size);
    }

	public static void main(String[] args) {
        launch(args);
    }
}
