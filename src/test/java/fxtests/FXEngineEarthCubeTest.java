package fxtests;

import fxpro.earth.CubeNode;
import fxpro.earth.EarthCubeMain;
import javafx.scene.input.KeyCode;
import org.junit.Test;

public class FXEngineEarthCubeTest extends AbstractTestExecution {
    @Test
    public void verifyEarthCubeMain() {
        show(EarthCubeMain.class);
        sleep(2000);
        CubeNode cube = lookupFirst(CubeNode.class);
        randomDrag(cube, 100);
        press(KeyCode.CONTROL);
        randomDrag(cube, 100);
        release(KeyCode.CONTROL);
        press(KeyCode.ALT);
        randomDrag(cube, 100);
        release(KeyCode.ALT);
        type(KeyCode.SPACE);
        sleep(1500);
        type(KeyCode.SPACE);
        sleep(1000);
        type(KeyCode.ESCAPE);
    }



}
