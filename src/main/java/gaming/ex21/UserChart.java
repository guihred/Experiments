package gaming.ex21;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import simplebuilder.SimpleDialogBuilder;

public class UserChart extends UserChartVariables {

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

    public PlayerColor getWinner(List<SettlePoint> settlePoints2, Map<PlayerColor, List<DevelopmentType>> usedCards2,
            List<EdgeCatan> edges2, Map<PlayerColor, List<CatanCard>> cards2) {
        return PlayerColor.vals().stream()
                .max(Comparator.comparingLong((PlayerColor e) -> countPoints(e, settlePoints2, usedCards2, edges2))
                        .thenComparingInt(e -> cards2.get(e).size()))
                .orElse(getColor());
    }

    public void setCards(List<CatanCard> currentCards) {
        CatanCard.placeCards(currentCards, cardGroup);
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
                new SimpleDialogBuilder().text("Player " + playerColor + " Won").button("Reset", () -> {
                    BorderPane root = (BorderPane) availablePorts.getScene().getRoot();
                    Pane center = (Pane) root.getCenter();
                    center.getChildren().clear();
                    Pane right = (Pane) root.getLeft();
                    right.getChildren().clear();
                    onWin.run();
                }).bindWindow(userPoints).displayDialog();
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
                    Pane newStatus = p.getStatus();
                    availablePorts.getChildren().add(newStatus);
                    newStatus.visibleProperty().bind(currentPlayer.isEqualTo(newV));
                });
    }

}
