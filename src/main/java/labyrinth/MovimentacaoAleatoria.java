package labyrinth;

import static labyrinth.GhostGenerator.getMapa;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import simplebuilder.SimpleDialogBuilder;

class MovimentacaoAleatoria extends AnimationTimer {
    private final CommomLabyrinth labyrinth3dWallTexture;
    private MeshView[] animais;
    // EAST, WEST, NORTH, SOUTH
    private int[] direction;
    private Random random = new Random();

    public MovimentacaoAleatoria(CommomLabyrinth labyrinth3dWallTexture, MeshView... animais) {
        this.labyrinth3dWallTexture = labyrinth3dWallTexture;
        this.animais = animais;
        direction = new int[animais.length];
    }

    @Override
    public void handle(long now) {
        for (int i = 0; i < animais.length; i++) {
            MeshView enemy = animais[i];
            final int step = 1;
            int dir = direction[i];
            goToDirection(dir, enemy, step);
            if (checkCollision(enemy)) {
                goToDirection(dir, enemy, -step);
                enemy.setRotationAxis(Rotate.Y_AXIS);
                enemy.setRotate(direction[i] * 90.);
                direction[i] = random.nextInt(4);

            }
            if (now % 1000 == 0) {
                direction[i] = random.nextInt(4);
            }
            if (labyrinth3dWallTexture.getCamera().getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                stop();
                displayEndOfGame(this::start);
            }

        }
    }

    private boolean checkCollision(MeshView enemy) {
        return labyrinth3dWallTexture.checkColision(enemy.getBoundsInParent())
            || notWithinRange(enemy.getTranslateZ(), getMapa()[0].length * LabyrinthWall.SIZE)
            || notWithinRange(enemy.getTranslateX(), getMapa().length * LabyrinthWall.SIZE);
    }

    private void displayEndOfGame(Runnable run) {
        new SimpleDialogBuilder().text("Você Morreu").button("Ok.", () -> {
            labyrinth3dWallTexture.getCamera().setTranslateX(0);
            labyrinth3dWallTexture.getCamera().setTranslateY(0);
            labyrinth3dWallTexture.getCamera().setTranslateZ(0);
            run.run();
        }).displayDialog();
    }

    private static void goToDirection(int dir, MeshView enemy, final int step) {
        // NORTH
        if (dir == 3) {
            enemy.setTranslateZ(enemy.getTranslateZ() + step);
        }
        // WEST
        if (dir == 2) {
            enemy.setTranslateX(enemy.getTranslateX() - step);
        }
        // SOUTH
        if (dir == 1) {
            enemy.setTranslateZ(enemy.getTranslateZ() - step);
        }
        // EAST
        if (dir == 0) {
            enemy.setTranslateX(enemy.getTranslateX() + step);
        }
    }

    private static boolean notWithinRange(double coord, double maxSize) {
        return coord < 0 || coord > maxSize;
    }

}