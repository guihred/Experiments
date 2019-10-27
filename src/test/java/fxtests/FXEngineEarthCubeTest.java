package fxtests;

import fxpro.earth.CubeNode;
import fxpro.earth.EarthCubeMain;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.Test;

public class FXEngineEarthCubeTest extends AbstractTestExecution {
    @Test
    public void verifyEarthCubeMain() {
        show(EarthCubeMain.class);
        CubeNode cube = lookupFirst(CubeNode.class);
        sleep(2000);
        drag(cube, MouseButton.PRIMARY);
        moveRandom(50);
        drop();
        press(KeyCode.CONTROL);
        drag(cube, MouseButton.PRIMARY);
        moveRandom(100);
        drop();
        release(KeyCode.CONTROL);
        press(KeyCode.ALT);
        drag(cube, MouseButton.PRIMARY);
        moveRandom(100);
        drop();
        release(KeyCode.ALT);
        type(KeyCode.SPACE);
        sleep(1500);
        type(KeyCode.SPACE);
        sleep(1000);
        type(KeyCode.ESCAPE);
    }

}
