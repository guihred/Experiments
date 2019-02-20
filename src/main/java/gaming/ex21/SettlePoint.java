package gaming.ex21;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class SettlePoint extends Group {
    public SettlePoint(Circle circle) {
        relocate(circle.getCenterX(), circle.getCenterY());
        circle.setCenterX(0);
        circle.setCenterY(0);
        getChildren().add(circle);
        getChildren().add(new Text("0"));
        setManaged(false);
    }

}
