package fxtests;

import fxpro.earth.CubeNode;
import fxpro.earth.EarthCubeMain;
import javafx.scene.input.MouseButton;
import org.junit.Test;

public class FXEngineEarthCubeTest extends AbstractTestExecution {
    @Test
    public void verifyEarthCubeMain() {
        show(EarthCubeMain.class);
        CubeNode lookupFirst;
        while ((lookupFirst = lookupFirst(CubeNode.class)) == null) {
            // DOES NOTHING
        }
        sleep(2000);
        drag(lookupFirst, MouseButton.PRIMARY);
        moveRandom(50);
        drop();
    }

}
