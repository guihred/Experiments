package ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

class PieGraph extends Canvas {
    double layout = 30;
    double maxLayout = 480;
    double lineSize = 5;
    int bins = 20;
    long ybins = 20;
    double xProportion;
    double yProportion;
    GraphicsContext gc;
    ObservableMap<String, Long> histogram = FXCollections.observableHashMap();
    int radius = 275;

    public PieGraph() {
        super(550, 550);
        this.gc = this.getGraphicsContext2D();
        drawGraph();
        // histogram.addListener((InvalidationListener) observable -> drawGraph());
        radius = (int) (gc.getCanvas().getWidth() / 2);
    }

    public void setHistogram(Map<String, Long> histogram) {
        this.histogram.putAll(histogram);
        drawGraph();
    }

    public void drawGraph() {
        long sum = histogram.values().stream().mapToLong(e -> e).sorted().sum();
        List<Color> availableColors = new ArrayList<>();
        int cubicRoot = Integer.max((int) Math.ceil(Math.pow(histogram.size(), 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(255 - i / cubicRoot / cubicRoot % cubicRoot * 256 / cubicRoot) % 256,
                    Math.abs(255 - i / cubicRoot % cubicRoot * 256 / cubicRoot) % 256,
                    Math.abs(255 - i % cubicRoot * 256 / cubicRoot) % 256);

            availableColors.add(rgb);
        }
        Collections.shuffle(availableColors);
        gc.clearRect(0, 0, 550, 550);

        double centerX = gc.getCanvas().getWidth() / 4;
        double centerY = gc.getCanvas().getHeight() / 4;
        double startAngle = 90;
        gc.setLineWidth(0.5);
        List<Entry<String, Long>> collect = histogram.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Long>::getValue)).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            Entry<String, Long> entry = collect.get(i);
            double arcExtent = entry.getValue() * 360d / sum;
            gc.setFill(availableColors.get(i));

            gc.fillArc(centerX, centerY, radius, radius, startAngle, arcExtent, ArcType.ROUND);
            gc.strokeArc(centerX, centerY, radius, radius, startAngle, arcExtent, ArcType.ROUND);

            startAngle += arcExtent;
        }
        startAngle = 90;
        for (int i = 0; i < collect.size(); i++) {
            Entry<String, Long> entry = collect.get(i);
            double arcExtent = entry.getValue() * 360d / sum;
            double x = Math.sin(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius / 1.5 + centerX + radius / 2;
            double y = Math.cos(Math.toRadians(arcExtent / 2 + startAngle + 90)) * radius / 1.5 + centerY + radius / 2;
            gc.strokeText(entry.getKey(), x, y);

            startAngle += arcExtent;
        }
        System.out.println(collect);
        drawLegend(collect, availableColors);
    }

    public void drawLegend(List<Entry<String, Long>> collect, List<Color> availableColors) {
        double x = gc.getCanvas().getWidth() / 10;
        double y = gc.getCanvas().getHeight() * 7 / 8;
        int columns = (int) Math.sqrt(collect.size()) + 1;
        int maxLetter = collect.stream().map(Entry<String, Long>::getKey).mapToInt(e -> e.length()).max().orElse(0);
        double a = gc.getCanvas().getWidth() / columns / 4 + maxLetter * 4;
        double b = gc.getCanvas().getHeight() / columns / 8;
        for (int i = 0; i < collect.size(); i++) {
            int index = collect.size() - i - 1;
            Entry<String, Long> entry = collect.get(index);
            int j = i / columns;
            double x2 = x + a * (i % columns);
            double y2 = y + b * j;
            gc.strokeText(entry.getKey(), x2, y2);
            gc.setFill(availableColors.get(index));
            gc.fillRect(x2 - 10, y2 - 8, 8, 8);
            gc.strokeRect(x2 - 10, y2 - 8, 8, 8);
        }
    }

}