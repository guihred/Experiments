package gaming.ex21;

import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class CatanVariables {
    protected final List<Terrain> terrains = FXCollections.observableArrayList();
    protected final List<SettlePoint> settlePoints = FXCollections.observableArrayList();
    protected List<EdgeCatan> edges;
    protected final Map<PlayerColor, List<CatanCard>> cards = PlayerColor.newMapList();
    protected final Map<PlayerColor, List<DevelopmentType>> usedCards = PlayerColor.newMapList();
    protected final SimpleObjectProperty<PlayerColor> currentPlayer = new SimpleObjectProperty<>();
    protected final ObservableList<CatanResource> elements = FXCollections.observableArrayList();
    protected final CatanDragContext dragContext = new CatanDragContext();
    protected final SimpleBooleanProperty diceThrown = new SimpleBooleanProperty(false);
    protected SelectResourceType resourcesToSelect = SelectResourceType.DEFAULT;
    protected int turnCount;
    protected final ObservableList<Deal> deals = FXCollections.observableArrayList();
    protected final Thief thief = new Thief();
    protected final List<Port> ports = Port.getPorts();
    protected final List<DevelopmentType> developmentCards = DevelopmentType.getDevelopmentCards();
    protected Node resourceChoices;
    protected Node exchangeButton;
    protected Node makeDeal;
    protected UserChart userChart;

    public SimpleObjectProperty<PlayerColor> currentPlayerProperty() {
        return currentPlayer;
    }

    public Map<PlayerColor, List<CatanCard>> getCards() {
        return cards;
    }

    public PlayerColor getCurrentPlayer() {
        return currentPlayer.get();
    }

    public ObservableList<Deal> getDeals() {
        return deals;
    }

    public SimpleBooleanProperty getDiceThrown() {
        return diceThrown;
    }

    public CatanDragContext getDragContext() {
        return dragContext;
    }

    public List<EdgeCatan> getEdges() {
        return edges;
    }

    public ObservableList<CatanResource> getElements() {
        return elements;
    }
    public PlayerColor getPlayerWinner() {
        return userChart.getWinner(settlePoints, usedCards, edges, cards);
    }

    public List<Port> getPorts() {
        return ports;
    }

    public List<SettlePoint> getSettlePoints() {
        return settlePoints;
    }

    public List<Terrain> getTerrains() {
        return terrains;
    }

    public void setCurrentPlayer(PlayerColor value) {
        currentPlayer.set(value);
    }

    public void setEdges(List<EdgeCatan> addTerrains) {
        edges = addTerrains;
    }

    public void setExchangeButton(Node exchangeButton) {
        this.exchangeButton = exchangeButton;
    }

    public void setMakeDeal(Node makeDeal) {
        this.makeDeal = makeDeal;
    }

    public void setResourceChoices(Node resourceChoices) {
        this.resourceChoices = resourceChoices;
    }

    public void setResourceSelect(SelectResourceType deal) {
        if (resourcesToSelect == SelectResourceType.DEFAULT) {
            resourcesToSelect = deal;
            resourceChoices.setVisible(true);
        }
        if (deal == SelectResourceType.MAKE_DEAL) {
            makeDeal.setDisable(true);
        }
    }

    public void setUserChart(UserChart userChart) {
        this.userChart = userChart;
    }
}
