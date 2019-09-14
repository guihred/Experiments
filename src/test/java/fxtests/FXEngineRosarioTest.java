package fxtests;

import java.io.File;
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
        RosarioCommons.choseFile("Carregar Arquivo SNGPC")
            .setInitialDirectory(value.getParentFile());
        RosarioCommons.choseFile("Carregar Arquivo Anvisa")
            .setInitialDirectory(value.getParentFile());
		getLogger().info("FXEngineRosarioTest STARTED");
	}

	@Test
	public void verify() throws Exception {
		getLogger().info("VERIFYING FXEngineRosarioTest ");
		clickOn("#SNGPC");
		type(typeText(value.getName()));
		type(KeyCode.ENTER);
        clickOn("#anvisa");
		type(typeText("anvisa2208.xlsx"));
        type(KeyCode.ENTER);
        clickOn("Importar Arquivo");
		clickOn(".text-field");
		String text = "asdsd";
		type(typeText(text));
		eraseText(text.length());
		clickOn("Exportar Excel");
		getLogger().info("VERIFIED FXEngineRosarioTest ");
	}

}
