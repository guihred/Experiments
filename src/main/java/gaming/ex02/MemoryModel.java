/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex02;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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

        final Random random = new Random();
        final MemoryImage[] values = MemoryImage.values();
        Color[] colors = new Color[]{
            Color.BLUE,
            Color.YELLOW,
            Color.RED,
            Color.GREEN,
            Color.BLACK,};

        for (int i = 0; i < MAP_WIDTH * MAP_HEIGHT / 2; i++) {
            final MemoryImage value = values[i % values.length];
            Color c = colors[(i + i / colors.length) % colors.length];
			List<MemorySquare> collect = Stream.of(map).flatMap(Stream::of).filter(s -> s.getMemoryImage() == null)
					.collect(Collectors.toList());
            final MemorySquare mem = collect.get(random.nextInt(collect.size()));
			mem.setMemoryImage(value);
            mem.setColor(c);
            createMouseClickedEvento(mem);

			collect = Stream.of(map).flatMap(Stream::of).filter(s -> s.getMemoryImage() == null)
					.collect(Collectors.toList());
            final MemorySquare mem2 = collect.get(random.nextInt(collect.size()));
			mem2.setMemoryImage(value);
            mem2.setColor(c);
            createMouseClickedEvento(mem2);

        }

    }

    final EventHandler<MouseEvent> createMouseClickedEvento(MemorySquare mem) {
        EventHandler<MouseEvent> mouseClicked = (MouseEvent event) -> {
			if (mem.getState() == MemorySquare.State.HIDDEN) {
                nPlayed.set(nPlayed.get() + 1);
				mem.setState(MemorySquare.State.SHOWN);
                if (nPlayed.get() % 2 == 0) {
					final List<MemorySquare> collect = Stream.of(map).flatMap(Stream::of)
							.filter(s -> s.getState() == MemorySquare.State.SHOWN).collect(Collectors.toList());
                    if (collect.stream().map(MemorySquare::getMemoryImage).distinct().count() == 1
                            && collect.stream().map(MemorySquare::getColor).distinct().count() == 1) {
						collect.forEach((MemorySquare c) -> c.setState(MemorySquare.State.FOUND));
                    } else {
						collect.forEach((MemorySquare c) -> new Timeline(
								new KeyFrame(Duration.seconds(1), new KeyValue(c.stateProperty(), MemorySquare.State.HIDDEN)))
										.play());

                    }
                    nPlayed.set(0);
                }
            }
        };
        mem.getFinalShape().setOnMouseClicked(mouseClicked);
        mem.setOnMouseClicked(mouseClicked);
        return mouseClicked;
    }

	public MemorySquare[][] getMap() {
		return map;
	}


}
