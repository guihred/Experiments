package paintexp.svgcreator;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import simplebuilder.SimpleCircleBuilder;
import utils.HasLogging;

public class SVGChanger {
    private static final Logger LOG = HasLogging.log();
    private double moveX;
    private double moveY;
    private final DoubleProperty currentX = new SimpleDoubleProperty();
    private final DoubleProperty currentY = new SimpleDoubleProperty();
    private final StringProperty path = new SimpleStringProperty();
    private Circle circle = new SimpleCircleBuilder().managed(false).radius(5).centerX(currentX).centerY(currentY)
            .build();

    public SVGChanger(String path) {
        this.path.set(path);
    }

    public String convertToRelative() {
        currentX.set(0);
        currentY.set(0);
        Pattern compile = Pattern.compile("([A-Za-z][^A-Za-z]+)");
        Matcher matcher = compile.matcher(path.get());
        matcher.reset();
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String svgCommand = matcher.group(1).trim();
            LOG.trace(svgCommand);
            String replace = replaceCommand(svgCommand);
            LOG.trace(replace);
            matcher.appendReplacement(sb, replace.replaceAll("\\.0+(\\D)", "$1"));
            String currentPosition = String.format(Locale.ENGLISH, "(%.2f,%.2f)", currentX.get(), currentY.get());
            LOG.trace(currentPosition);
        }
        matcher.appendTail(sb);
        LOG.info(path.get());
        String convertedPath = sb.toString();
        LOG.info(convertedPath);
        return convertedPath;

    }

    public Circle getCircle() {
        return circle;
    }

    public StringProperty pathProperty() {
        return path;
    }

    public void setPath(String value) {
        path.set(value);

    }

    private boolean isUpperCase(final String substring) {
        return substring.matches("[A-Z]");
    }

    private String replaceCommand(String group) {
        String svgCommand = group.substring(0, 1);
        int args = SVGCommand.valueOf(svgCommand.toUpperCase()).getArgs();
        String[] split = group.split("[\\s,[A-Za-z]]+|(?<=\\d)(?=-)");
        if (args == 1) {
            return replaceOneCommand(split, svgCommand);
        }
        if (args == 0) {
            currentX.set(moveX);
            currentY.set(moveY);
            return group.toLowerCase();
        }
        if (args % 2 == 0) {
            return replaceEvenCommand(split, svgCommand, args);
        }
        if (args == 7) {
            return replaceOddCommand(split, svgCommand, args);
        }
        return group;
    }

    private String replaceEvenCommand(final String[] split, final String letter, final int args) {
        StringBuilder cmd = new StringBuilder();
        for (int i = 2; i < split.length; i += 2) {
            double xCoord = Double.parseDouble(split[i - 1]);
            double yCoord = Double.parseDouble(split[i]);
            if (isUpperCase(letter)) {
                double rx = currentX.get();
                double ry = currentY.get();
                if (i % args == 0) {
                    currentX.set(xCoord);
                }
                xCoord = xCoord - rx;
                if (i % args == 0) {
                    currentY.set(yCoord);
                }
                yCoord = yCoord - ry;
            } else {
                if (i % args == 0) {
                    currentX.set(currentX.get() + xCoord);
                    currentY.set(currentY.get() + yCoord);
                }
            }
            String format = String.format(Locale.ENGLISH, "%.2f %.2f ", xCoord, yCoord);
            cmd.append(format);
        }
        if (letter.equalsIgnoreCase("M")) {
            moveX = currentX.get();
            moveY = currentY.get();
        }

        return String.format("%s%s", letter.toLowerCase(), cmd.toString());
    }

    private String replaceOddCommand(final String[] split, final String substring, final int args) {
        StringBuilder cmd = new StringBuilder();
        for (int i = args; i < split.length; i += args) {
            double xCoord = Double.parseDouble(split[i - 1]);
            double yCoord = Double.parseDouble(split[i]);
            if (isUpperCase(substring)) {
                double rx = currentX.get();
                double ry = currentY.get();
                currentX.set(xCoord);
                xCoord = xCoord - rx;
                currentY.set(yCoord);
                yCoord = yCoord - ry;
            } else {
                currentX.set(currentX.get() + xCoord);
                currentY.set(currentY.get() + yCoord);
            }
            String otherArgs = Stream.of(split).skip(1L + i - args).limit(args - 2L).collect(Collectors.joining(" "));
            String format = String.format(Locale.ENGLISH, "%s%s %.2f %.2f", substring.toLowerCase(), otherArgs, xCoord,
                    yCoord);
            cmd.append(format);
        }
        return cmd.toString();
    }

    private String replaceOneCommand(final String[] split, final String letter) {
        StringBuilder cmd = new StringBuilder();
        for (int i = 1; i < split.length; i += 2) {
            double coord = Double.parseDouble(split[i]);
            boolean vertical = "v".equalsIgnoreCase(letter);
            boolean horizontal = !vertical;
            if (isUpperCase(letter)) {
                double rx = currentX.get();
                double ry = currentY.get();
                currentX.set(vertical ? currentX.get() : coord);
                currentY.set(horizontal ? currentY.get() : coord);
                coord -= vertical ? ry : rx;
            } else {
                currentX.set(currentX.get() + (vertical ? 0 : coord));
                currentY.set(currentY.get() + (horizontal ? 0 : coord));
            }
            String format = String.format(Locale.ENGLISH, "%.2f ", coord);
            cmd.append(format);
        }
        return String.format("%s%s", letter.toLowerCase(), cmd.toString());
    }

    public static void main(final String[] args) {
        String original = "M0,0 L 246.0 278.0 L 235.0 316.0 L 224.0 320.0 M 310.0 295.0 M 324.0 253.0 M 325.0 305.0 H 289.0 H 358.0 H 228.0 T 222.0 275.0 T 158.0 269.0 T 253.0 289.0 Q 329.0 343.0 249.0 249.0 Q 8.0 107.0 272.0 283.0 C 238.0 289.0 227.0 268.0 249.0 249.0 A -19 -32 -120.7 1 0 249.0 249.0 A -276 -190 -145.5 1 0 307.0 291.0 S 159.0 191.0 205.0 249.0 S 3.0 90.0 206.0 217.0 Z Z Z V 264.0 V 267.0 V 270.0 ";
        String convertToRelative = new SVGChanger(original).convertToRelative();
        LOG.info(convertToRelative);
        LOG.info(original);

    }

}
