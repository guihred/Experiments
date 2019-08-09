package fxtests;

import java.io.File;
import java.util.Objects;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import rosario.RosarioCommons;
import rosario.RosarioComparadorArquivos;
import utils.ResourceFXUtils;


public class FXEngineRosarioTest extends AbstractTestExecution {

	private File value = ResourceFXUtils.toFile("sngpc2808.pdf");

	@Override
	public void start(Stage stage) throws Exception {
		ResourceFXUtils.initializeFX();
		getLogger().info("STARTING FXEngineRosarioTest");
		stage.setMaximized(true);
		RosarioComparadorArquivos rosarioComparadorArquivos = new RosarioComparadorArquivos();
		rosarioComparadorArquivos.start(stage);
        RosarioCommons.setOpenAtExport(false);
        RosarioCommons.getFileChoose().get("Carregar Arquivo SNGPC")
            .setInitialDirectory(value.getParentFile());
        RosarioCommons.getFileChoose().get("Carregar Arquivo Anvisa")
            .setInitialDirectory(value.getParentFile());
		getLogger().info("FXEngineRosarioTest STARTED");
	}

	@Test
	public void verify() throws Exception {
		getLogger().info("VERIFYING FXEngineRosarioTest ");
		clickOn("#SNGPC");
		type(typeName(value.getName()));
		type(KeyCode.ENTER);
        clickOn("#anvisa");
        type(typeName("anvisa2208.xlsx"));
        type(KeyCode.ENTER);
        clickOn("Importar Arquivo");
		clickOn(".text-field");
		String text = "asdsd";
        type(typeName(text));
		eraseText(text.length());
		clickOn("Exportar Excel");
		getLogger().info("VERIFIED FXEngineRosarioTest ");
	}

    private KeyCode[] typeName(String name) {
        return name.chars().mapToObj(e -> Objects.toString((char) e).toUpperCase())
				.map(s -> ".".equals(s) ? "Period" : s).map(KeyCode::getKeyCode)
				.toArray(KeyCode[]::new);
    }

}
