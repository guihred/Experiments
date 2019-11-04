package fxtests;

import fxsamples.PhotoViewer;
import java.util.Set;
import javafx.scene.Node;
import org.junit.Test;
import utils.ConsumerEx;

public class FXPhotoViewerTest extends AbstractTestExecution {

    @Test
    public void verifyPhotoViewer() {
        show(PhotoViewer.class);
        Set<Node> queryAll = lookup(".button").queryAll();
        queryAll.forEach(ConsumerEx.ignore(t -> {
            for (int i = 0; i < 10; i++) {
                clickOn(t);
            }
        }));
    }

}
