package gaming.ex21;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import simplebuilder.SimpleTextBuilder;
import utils.CommonsFX;

public class UserChart extends VBox {
    private static final String USER_PNG = "user.png";
    private final ImageView userImage = CatanResource.newImage(USER_PNG, Color.BLUE, 100);
    private final Text userPoints = new SimpleTextBuilder().text("0").wrappingWidth(userImage.fitWidthProperty())
        .textAlignment(TextAlignment.CENTER).build();
    private final Dice dice1 = new Dice();
    private final Dice dice2 = new Dice();
    private final EnumMap<PlayerColor, LongProperty> playersPoints = new EnumMap<>(PlayerColor.class);
    private final ObjectProperty<PlayerColor> color = new SimpleObjectProperty<>(PlayerColor.BLUE);

    private final VBox availablePorts = new VBox();
    private final ExtraPoint largestArmy = new ExtraPoint("largestarmy.png");
    private final ExtraPoint longestRoad = new ExtraPoint("longestroad.png");
    private final Group cardGroup = new Group();

    public UserChart() {
        VBox vBox = new VBox();
        for (PlayerColor playerColor : PlayerColor.values()) {
            SimpleLongProperty value = new SimpleLongProperty(0);
            playersPoints.put(playerColor, value);
            ImageView newImage = CatanResource.newImage(USER_PNG, playerColor.getColor(), 30);
            Text build = new SimpleTextBuilder().text(value.asString()).textOrigin(VPos.CENTER).build();
            HBox e = new HBox(newImage, build);
            e.visibleProperty().bind(color.isEqualTo(playerColor).not());
            e.managedProperty().bind(e.visibleProperty());
            vBox.getChildren().add(e);
        }
        VBox vBox2 = new VBox(userImage, userPoints, new HBox(dice1, dice2));
        largestArmy.visibleProperty().bind(color.isEqualTo(largestArmy.playerProperty()));
        longestRoad.visibleProperty().bind(color.isEqualTo(longestRoad.playerProperty()));
        getChildren().addAll(new HBox(vBox, vBox2, availablePorts, largestArmy, longestRoad), cardGroup);

    }

    public long countPoints(final PlayerColor newPlayer, List<SettlePoint> settlePoints,
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
        userImage.setImage(CatanResource.newImage(USER_PNG, newV.getColor()));
        color.setValue(newV);
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
                CommonsFX.displayDialog("Player " + playerColor + " Won", "Reset", () -> {
                    BorderPane root = (BorderPane) availablePorts.getScene().getRoot();
                    Pane center = (Pane) root.getCenter();
                    center.getChildren().clear();
                    Pane right = (Pane) root.getRight();
                    right.getChildren().clear();
                    CatanModel.create(center, right);
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
}
