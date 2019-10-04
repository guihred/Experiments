package gaming.ex21;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import utils.CommonsFX;
import utils.StageHelper;

public class UserChart extends VBox {
    @FXML
    private ImageView userImage;
    @FXML
    private ImageView greenImage;
    @FXML
    private ImageView redImage;
    @FXML
    private ImageView blueImage;
    @FXML
    private ImageView yellowImage;
    @FXML
    private Text userPoints;
    @FXML
    private Dice dice1;
    @FXML
    private Dice dice2;
    @FXML
    private ObjectProperty<PlayerColor> color;

    @FXML
    private VBox availablePorts;
    @FXML
    private ExtraPoint largestArmy;
    @FXML
    private ExtraPoint longestRoad;
    @FXML
    private Text greenPoints;
    @FXML
    private Text redPoints;
    @FXML
    private Text bluePoints;
    @FXML
    private Text yellowPoints;
    @FXML
    private Group cardGroup;
    private EnumMap<PlayerColor, LongProperty> playersPoints = new EnumMap<>(PlayerColor.class);
    private BiConsumer<Pane, Pane> onWin;

    public UserChart() {
        CommonsFX.loadRoot("UserChart.fxml", this);
        for (PlayerColor playerColor : PlayerColor.values()) {
            playersPoints.put(playerColor, new SimpleLongProperty(0));
        }
        bindText(PlayerColor.RED, redPoints, redImage);
        bindText(PlayerColor.GREEN, greenPoints, greenImage);
        bindText(PlayerColor.BLUE, bluePoints, blueImage);
        bindText(PlayerColor.YELLOW, yellowPoints, yellowImage);
        largestArmy.visibleProperty().bind(color.isEqualTo(largestArmy.playerProperty()));
        longestRoad.visibleProperty().bind(color.isEqualTo(longestRoad.playerProperty()));
        userImage.setImage(CatanResource.newImage(CatanResource.USER_PNG, PlayerColor.BLUE));
    }

    public long countPoints(PlayerColor newPlayer, List<SettlePoint> settlePoints,
        Map<PlayerColor, List<DevelopmentType>> usedCards, List<EdgeCatan> edges) {
        long pointsCount = settlePoints.stream().filter(s -> s.getElement() instanceof Village)
            .filter(e -> e.getElement().getPlayer() == newPlayer).count();
        pointsCount += settlePoints.stream().filter(s -> s.getElement() instanceof City)
            .filter(e -> e.getElement().getPlayer() == newPlayer).count() * 2;
        pointsCount += usedCards.get(newPlayer).stream().filter(e -> e == DevelopmentType.UNIVERSITY).count();
        long armySize = usedCards.get(newPlayer).stream().filter(e1 -> e1 == DevelopmentType.KNIGHT).count();
        if (armySize >= 3 && largestArmy.getRecord() < armySize) {
            largestArmy.setPlayer(newPlayer);
            largestArmy.setRecord(armySize);
        }
        if (largestArmy.getPlayer() == newPlayer) {
            pointsCount += 2;
        }
        long roadSize = EdgeCatan.countRoadSize(newPlayer, edges);
        if (roadSize >= 5 && longestRoad.getRecord() < roadSize) {
            longestRoad.setPlayer(newPlayer);
            longestRoad.setRecord(roadSize);
        }
        if (longestRoad.getPlayer() == newPlayer) {
            pointsCount += 2;
        }
        return pointsCount;
    }

    public PlayerColor getColor() {
        return color.get();
    }

    public PlayerColor getWinner(List<SettlePoint> settlePoints2, Map<PlayerColor, List<DevelopmentType>> usedCards2,
        List<EdgeCatan> edges2, Map<PlayerColor, List<CatanCard>> cards2) {
        return getWinner(this, settlePoints2, usedCards2, edges2, cards2);
    }

    public void setCards(List<CatanCard> currentCards) {
        cardGroup.getChildren().clear();
        for (CatanCard type : currentCards) {
            cardGroup.getChildren().add(type);
        }
        Collection<List<CatanCard>> values = currentCards.stream().filter(e -> e.getResource() != null)
            .collect(Collectors.groupingBy(CatanCard::getResource)).values().stream().collect(Collectors.toList());
        double layoutX = 0;
        double layoutY = 0;
        List<CatanCard> collect = currentCards.stream().filter(e -> e.getResource() == null)
            .collect(Collectors.toList());
        values.add(collect);
        for (List<CatanCard> list : values) {
            for (CatanCard catanCard : list) {
                catanCard.relocate(layoutY, layoutX);
                layoutX += 10;
            }
            layoutX = 0;
            layoutY += CatanCard.PREF_WIDTH;
        }
    }

    public void setColor(PlayerColor newV) {
        userImage.setImage(CatanResource.newImage(CatanResource.USER_PNG, newV));
        color.setValue(newV);
    }

    public void setOnWin(BiConsumer<Pane, Pane> onWin) {
        this.onWin = onWin;

    }

    public void setPoints(PlayerColor newPlayer, List<SettlePoint> settlePoints,
        Map<PlayerColor, List<DevelopmentType>> usedCards, List<EdgeCatan> edges) {
        for (PlayerColor playerColor : PlayerColor.values()) {
            long points = countPoints(playerColor, settlePoints, usedCards, edges);
            playersPoints.get(playerColor).set(points);
            if (playerColor == newPlayer) {
                userPoints.setText(points + " Points");
            }
            if (points >= 10) {
                StageHelper.displayDialog("Player " + playerColor + " Won", "Reset", () -> {
                    BorderPane root = (BorderPane) availablePorts.getScene().getRoot();
                    Pane center = (Pane) root.getCenter();
                    center.getChildren().clear();
                    Pane right = (Pane) root.getLeft();
                    right.getChildren().clear();
                    onWin.accept(center, right);
                });
            }
        }
    }

    public int throwDice() {
        return dice1.throwDice() + dice2.throwDice();
    }

    public void updatePorts(final PlayerColor newV, List<Port> ports, List<SettlePoint> settlePoints,
        ObjectProperty<PlayerColor> currentPlayer) {
        ports.stream().filter(p -> !availablePorts.getChildren().contains(p.getStatus()))
            .filter(p -> settlePoints.stream().filter(s -> s.getElement() != null)
                .filter(s -> s.getElement().getPlayer() == newV).anyMatch(p.getPoints()::contains))
            .forEach(p -> {
                HBox newStatus = p.getStatus();
                availablePorts.getChildren().add(newStatus);
                newStatus.visibleProperty().bind(currentPlayer.isEqualTo(newV));
            });
    }

    private void bindText(PlayerColor player, Text points, ImageView image) {
        points.textProperty().bind(playersPoints.get(player).asString());
        points.visibleProperty().bind(color.isEqualTo(player).not());
        image.setImage(CatanResource.newImage(CatanResource.USER_PNG, player));
    }

    public static PlayerColor getWinner(UserChart userChart2, List<SettlePoint> settlePoints2,
        Map<PlayerColor, List<DevelopmentType>> usedCards2, List<EdgeCatan> edges2,
        Map<PlayerColor, List<CatanCard>> cards2) {
        return PlayerColor.vals().stream()
            .max(Comparator.comparing((PlayerColor e) -> userChart2.countPoints(e, settlePoints2, usedCards2, edges2))
                .thenComparing(e -> cards2.get(e).size()))
            .orElse(userChart2.getColor());
    }
}
