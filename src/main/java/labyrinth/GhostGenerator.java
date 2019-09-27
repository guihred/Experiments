package labyrinth;

import java.security.SecureRandom;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import utils.ResourceFXUtils;

public final class GhostGenerator {
	public static final double SIZE = 60;
	public static final Color LIGHT_COLOR = Color.grayRgb(125);
    private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");

    private static final SecureRandom random = new SecureRandom();

    static String[][] mapa = { 
        { "_", "_", "_", "_", "_", "|" }, 
        { "|", "_", "_", "_", "_", "|" },
        { "|", "|", "_", "|", "_", "|" }, 
        { "_", "|", "_", "|", "_", "|" }, 
        { "|", "|", "_", "|", "_", "|" },
        { "|", "_", "_", "|", "_", "|" }, 
        { "|", "_", "_", "_", "|", "_" }, 
        { "_", "|", "_", "_", "_", "|" },
        { "_", "_", "|", "|", "|", "_" }, 
        { "_", "|", "_", "|", "_", "|" }, 
        { "|", "|", "_", "_", "|", "_" },
        { "_", "_", "_", "_", "_", "|" }, 
        { "|", "_", "_", "_", "_", "_" }, 
        { "|", "|", "_", "|", "_", "|" },
        { "|", "_", "|", "_", "_", "|" }, 
        { "|", "_", "_", "_", "_", "|" }, 
        { "_", "_", "_", "|", "_", "|" },
        { "_", "_", "_", "_", "_", "_" }, 
    };

    private GhostGenerator() {
    }

    public static MeshView generateGhost(Color animalColor) {
        return generateGhost(MESH_GHOST, animalColor);
    }

    public static MeshView generateGhost(String file, Color animalColor) {
        Mesh mesh = ResourceFXUtils.importStlMesh(file);
        MeshView animal = new MeshView(mesh);
        PhongMaterial sample = new PhongMaterial(animalColor);
        sample.setSpecularColor(GhostGenerator.LIGHT_COLOR);
        sample.setSpecularPower(16);
        animal.setMaterial(sample);
        animal.setTranslateY(15);

        animal.setTranslateZ(rnd(mapa[0].length * SIZE));
        animal.setTranslateX(rnd(mapa.length * SIZE));

        animal.setScaleX(4. / 10);
        animal.setScaleZ(4. / 10);
        animal.setScaleY(1);

        return animal;
    }

	private static double rnd(double bound) {
		return random.nextDouble() * bound;
    }

}
