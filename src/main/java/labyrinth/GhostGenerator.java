package labyrinth;

import java.security.SecureRandom;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import utils.ResourceFXUtils;

public final class GhostGenerator {
    static final int SIZE = 60;
    static final Color LIGHT_COLOR = Color.grayRgb(125);
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

        int posicaoInicialZ = rnd(mapa[0].length * SIZE);
        animal.setTranslateZ(posicaoInicialZ);
        int posicaoInicialX = rnd(mapa.length * SIZE);
        animal.setTranslateX(posicaoInicialX);

        animal.setScaleX(4. / 10);
        animal.setScaleZ(4. / 10);
        animal.setScaleY(1);

        return animal;
    }

    private static int rnd(int bound) {
        return random.nextInt(bound);
    }

}
