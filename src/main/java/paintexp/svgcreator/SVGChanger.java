package paintexp.svgcreator;

import java.util.DoubleSummaryStatistics;
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
import utils.ex.HasLogging;

public class SVGChanger {
	private static final Logger LOG = HasLogging.log();
	private double moveX;
	private double moveY;
	private final DoubleProperty currentX = new SimpleDoubleProperty();
	private final DoubleProperty currentY = new SimpleDoubleProperty();
	private final DoubleProperty scale = new SimpleDoubleProperty(1);
	private final StringProperty path = new SimpleStringProperty();
	private Circle circle = new SimpleCircleBuilder().managed(false).radius(5).centerX(currentX).centerY(currentY)
			.build();
	private DoubleSummaryStatistics xStats = new DoubleSummaryStatistics();
	private DoubleSummaryStatistics yStats = new DoubleSummaryStatistics();

    public SVGChanger(StringProperty stringProperty) {
        path.bind(stringProperty);
	}

    private SVGChanger(String stringProperty) {
        path.set(stringProperty);
    }

	public String convertToRelative() {
		currentX.set(0);
		currentY.set(0);
		xStats = new DoubleSummaryStatistics();
		yStats = new DoubleSummaryStatistics();
		Pattern compile = Pattern.compile("([A-Za-z][^A-Za-z]+)");
		Matcher matcher = compile.matcher(path.get());
		matcher.reset();
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String svgCommand = matcher.group(1).trim();
			String replace = replaceCommand(svgCommand);
			matcher.appendReplacement(sb, replace.replaceAll("\\.0+(\\D)", "$1"));
			String currentPosition = String.format(Locale.ENGLISH, "(%.2f,%.2f)", currentX.get(), currentY.get());
			xStats.accept(currentX.get());
			yStats.accept(currentY.get());
			LOG.trace(currentPosition);
		}
		matcher.appendTail(sb);
        return sb.toString();

	}

	public Circle getCircle() {
		return circle;
	}

	public double getHeight() {
		return yStats.getMax() - yStats.getMin();
	}

	public double getScale() {
		return scale.get();
	}

	public double getWidth() {
		return xStats.getMax() - xStats.getMin();
	}

	public DoubleSummaryStatistics getXStats() {
		return xStats;
	}

	public DoubleSummaryStatistics getYStats() {
		return yStats;
	}

	public StringProperty pathProperty() {
		return path;
	}

	public DoubleProperty scaleProperty() {
		return scale;
	}

	public void setPath(final String value) {
		path.set(value);

	}

	public void setScale(final double s) {
		scale.set(s);
	}

	private double getCoodenate(String s, final String letter) {
		double coord = Double.parseDouble(s);
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
		return coord;
	}

	private String replaceArcCommand(final String[] split, final String substring, final int args) {
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
			double[] radius = Stream.of(split).skip(1L + i - args).limit(2L).mapToDouble(Double::parseDouble).toArray();
			String otherArgs = Stream.of(split).skip(3L + i - args).limit(3).collect(Collectors.joining(" "));
			String format = String.format(Locale.ENGLISH, "%s %.2f %.2f %s %.2f %.2f", substring.toLowerCase(),
					radius[0] * scale.get(), radius[1] * scale.get(), otherArgs, xCoord * scale.get(),
					yCoord * scale.get());
			cmd.append(format);
		}
		return cmd.toString();
	}

	private String replaceCommand(final String group) {
		String svgCommand = group.substring(0, 1);
		int args = SVGCommand.valueOf(svgCommand.toUpperCase()).getArgs();
        String[] commandParts = group.split("[\\s,[A-Za-z]]+|(?<=\\d)(?=-)");
		if (args == 1) {
            return replaceOneCommand(commandParts, svgCommand);
		}
		if (args == 0) {
			currentX.set(moveX);
			currentY.set(moveY);
			return group.toLowerCase();
		}
		if (args % 2 == 0) {
            return replaceEvenCommand(commandParts, svgCommand, args);
		}
		if (args == 7) {
            return replaceArcCommand(commandParts, svgCommand, args);
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
			String format = String.format(Locale.ENGLISH, "%.2f %.2f ", xCoord * scale.get(), yCoord * scale.get());
			cmd.append(format);
		}
		if (SVGCommand.M.name().equalsIgnoreCase(letter)) {
			moveX = currentX.get();
			moveY = currentY.get();
		}

		return String.format("%s%s", letter.toLowerCase(), cmd.toString());
	}

	private String replaceOneCommand(final String[] split, final String letter) {
		StringBuilder cmd = new StringBuilder();
		for (int i = 1; i < split.length; i += 2) {
			double coord = getCoodenate(split[i], letter);
			String format = String.format(Locale.ENGLISH, "%.2f ", coord * scale.get());
			cmd.append(format);
		}
		return String.format("%s%s", letter.toLowerCase(), cmd.toString());
	}

	public static void main(final String[] args) {
        String original = "M0,0 L 246.0 278.0 L 235.0 316.0 L 224.0 320.0"
            + " M 310.0 295.0 M 324.0 253.0 M 325.0 305.0 H 289.0 H 358.0 H 228.0"
            + " T 222.0 275.0 T 158.0 269.0 T 253.0 289.0 Q 329.0 343.0 249.0 249.0"
            + " Q 8.0 107.0 272.0 283.0 C 238.0 289.0 227.0 268.0 249.0 249.0"
            + " A -19 -32 -120.7 1 0 249.0 249.0 A -276 -190 -145.5 1 0 307.0 291.0 S 159.0 191.0 205.0 249.0"
            + " S 3.0 90.0 206.0 217.0 Z V 264.0 V 267.0 V 270.0 ";
		String convertToRelative = new SVGChanger(original).convertToRelative();
		LOG.info(convertToRelative);
		LOG.info(original);

	}

	private static boolean isUpperCase(final String substring) {
		return substring.matches("[A-Z]");
	}

}
