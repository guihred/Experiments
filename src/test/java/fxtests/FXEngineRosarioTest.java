package fxtests;

import java.io.File;
import java.util.Objects;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import rosario.RosarioComparadorArquivos;
import utils.HasLogging;
import utils.ResourceFXUtils;


public class FXEngineRosarioTest extends ApplicationTest implements HasLogging {

    private File value = ResourceFXUtils.toFile("sngpc2808.pdf");

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        getLogger().info("STARTING FXEngineRosarioTest");
        stage.setMaximized(true);
        RosarioComparadorArquivos rosarioComparadorArquivos = new RosarioComparadorArquivos();
        rosarioComparadorArquivos.start(stage);
        rosarioComparadorArquivos.setOpenAtExport(false);
        rosarioComparadorArquivos.getFileChoose().get("Carregar Arquivo SNGPC")
                .setInitialDirectory(value.getParentFile());
        getLogger().info("FXEngineRosarioTest STARTED");
    }

    @Test
    public void verify() throws Exception {
        getLogger().info("VERIFYING FXEngineRosarioTest ");
        clickOn("#SNGPC");
        String name = value.getName();
        KeyCode[] array = name.chars().mapToObj(e -> Objects.toString((char) e).toUpperCase())
                .map(s -> ".".equals(s) ? "Period" : s).map(KeyCode::getKeyCode)
                .toArray(KeyCode[]::new);
        type(array);
        type(KeyCode.ENTER);
        clickOn(".text-field");
        String text = "asdsd";
        write(text);
        eraseText(text.length());
        clickOn("Exportar Excel");
        getLogger().info("VERIFIED FXEngineRosarioTest ");
    }

}
