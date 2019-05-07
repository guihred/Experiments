/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex02;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.KeyValue;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

/**
 *
 * @author Note
 */
public class MemoryModel {

    public static final int MAP_WIDTH = 8;
    public static final int MAP_HEIGHT = 5;

    private final MemorySquare[][] map = new MemorySquare[MAP_WIDTH][MAP_HEIGHT];
    private final IntegerProperty nPlayed = new SimpleIntegerProperty(0);

    public MemoryModel() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new MemorySquare();
            }
        }

        final MemoryImage[] values = MemoryImage.values();
        Color[] colors = new Color[] { Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN, Color.BLACK, };

        List<MemorySquare> emptySquares = Stream.of(map).flatMap(Stream::of).filter(s -> s.getMemoryImage() == null)
            .collect(Collectors.toList());
        Collections.shuffle(emptySquares);
        for (int i = 0; !emptySquares.isEmpty(); i++) {
            final MemoryImage value = values[i % values.length];
            Color color = colors[(i + i / colors.length) % colors.length];
            addMemoryImage(value, color, emptySquares);
            addMemoryImage(value, color, emptySquares);
        }
    }

    public MemorySquare[][] getMap() {
        return map;
    }

    final EventHandler<MouseEvent> createMouseClickedEvento(MemorySquare mem) {
        EventHandler<MouseEvent> mouseClicked = event -> displayIfHidden(mem);
        mem.getFinalShape().setOnMouseClicked(mouseClicked);
        mem.setOnMouseClicked(mouseClicked);
        return mouseClicked;
    }

    private void addMemoryImage(final MemoryImage value, Color color, List<MemorySquare> emptySquares) {
        final MemorySquare mem = emptySquares.remove(0);
        mem.setMemoryImage(value);
        mem.setColor(color);
        createMouseClickedEvento(mem);
    }

    private void displayIfHidden(MemorySquare mem) {
        if (mem.getState() == MemorySquare.State.HIDDEN) {
            nPlayed.set(nPlayed.get() + 1);
            mem.setState(MemorySquare.State.SHOWN);
            if (nPlayed.get() % 2 == 0) {
                final List<MemorySquare> shownSquares = Stream.of(map).flatMap(Stream::of)
                    .filter(s -> s.getState() == MemorySquare.State.SHOWN).collect(Collectors.toList());
                if (shownSquares.stream().map(MemorySquare::getMemoryImage).distinct().count() == 1
                    && shownSquares.stream().map(MemorySquare::getColor).distinct().count() == 1) {
                    shownSquares.forEach((MemorySquare c) -> c.setState(MemorySquare.State.FOUND));
                } else {
                    shownSquares.forEach((MemorySquare c) -> new SimpleTimelineBuilder()
                        .addKeyFrame(Duration.seconds(1), new KeyValue(c.stateProperty(), MemorySquare.State.HIDDEN))
                        .build().play());
                }
                nPlayed.set(0);
            }
        }
    }

}
