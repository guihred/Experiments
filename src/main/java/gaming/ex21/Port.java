package gaming.ex21;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Port extends Group {

    public static final int RADIUS = 70;
    private final ResourceType type;

    public Port(final ResourceType type) {
        this.type = type;
		getChildren().add(new StackPane(getNumberText()));
        setManaged(false);
    }


    public ResourceType getType() {
        return type;
    }

    private Text getNumberText() {
        Text e = new Text();
        e.setFont(Font.font(20));
        e.setVisible(type != ResourceType.DESERT);
        return e;
    }

}
