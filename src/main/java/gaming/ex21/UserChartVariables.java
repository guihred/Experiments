package gaming.ex21;

import java.util.EnumMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import utils.CommonsFX;

public class UserChartVariables extends VBox {
    @FXML
    protected ImageView userImage;
    @FXML
    protected ImageView greenImage;
    @FXML
    protected ImageView redImage;
    @FXML
    protected ImageView blueImage;
    @FXML
    protected ImageView yellowImage;
    @FXML
    protected Text userPoints;
    @FXML
    protected Dice dice1;
    @FXML
    protected Dice dice2;
    @FXML
    protected ObjectProperty<PlayerColor> color;

    @FXML
    protected VBox availablePorts;
    @FXML
    protected ExtraPoint largestArmy;
    @FXML
    protected ExtraPoint longestRoad;
    @FXML
    protected Text greenPoints;
    @FXML
    protected Text redPoints;
    @FXML
    protected Text bluePoints;
    @FXML
    protected Text yellowPoints;
    @FXML
    protected Group cardGroup;
    protected EnumMap<PlayerColor, SimpleLongProperty> playersPoints = new EnumMap<>(PlayerColor.class);
    protected Runnable onWin;

    public UserChartVariables() {
        load();
    }

    public PlayerColor getColor() {
        return color.get();
    }

    public void setColor(PlayerColor newV) {
        userImage.setImage(CatanResource.newImage(CatanResource.USER_PNG, newV));
        color.setValue(newV);
    }

    public void setOnWin(Runnable onWin) {
        this.onWin = onWin;
    }

    protected void bindText(PlayerColor player, Text points, ImageView image) {
        points.textProperty().bind(playersPoints.get(player).asString());
        points.visibleProperty().bind(color.isEqualTo(player).not());
        image.setImage(CatanResource.newImage(CatanResource.USER_PNG, player));
    }

    private final void load() {
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

}
