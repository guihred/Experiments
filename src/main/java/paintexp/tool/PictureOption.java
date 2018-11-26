package paintexp.tool;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import utils.ConsumerEx;
import utils.HasLogging;

enum PictureOption {
    TRIANGLE("M 12,4 L 20,20 4,20 12,4z"),
    SCALENE("M 0,0 l 0,20 20,0 Z "),
    DIAMOND("M 20,0 l 10,10 -10,10 -10,-10 Z "),
    PENTAGON("M 10,0 L 0,7 L 4,19 L 16,19 L 20,7 Z"),
    HEXAGON("M 20,9 L 15,17.310350 5,17.310350 0,9 5,0.061711 15,0.061711 20,9 z"),
    ARROW_RIGHT("m 20,0 l10,0 0,-7 10,10 -10,10 0,-7 -10,0 z"),
    ARROW_LEFT("m 0,20 l10,10 0,-7 10,0 0,-7 -10,0 0,-7 z"),
    ARROW_UP("m 20,0 l10,10 -7,0 0,10 -7,0 0,-10 -7,0 z"),
    ARROW_DOWN("m 0,20 l0,10 -7,0 10,10 10,-10 -7,0 0,-10 z"),
    STAR_4("M 10 4 L 11.9385 9.8393 L 18 12 L 11.9385 14.1607 L 10 20 L 7.6171 14.1607 L 2 12 L 7.6171 9.8393 Z"),
    STAR_5("m10,0 2,6h6l-5,4 2,6-5-4-5,4 2-6-5-4h6z "),
    STAR_6("M10,0 L12.875,5.020 18.660,5 15.750,10 18.660,15 12.875,14.980 10,20 7.125,14.980 1.340,15 4.250,10 1.340,5 7.125,5.020 z"),
    HEART("M15.53 1A5.52 5.52 0 0 0 11 4 5.52 5.52 0 0 0 0 6.58C0 12.37 11 20 11 20s11-7.63 11-13.42A5.53 5.53 0 0 0 15.53 1z"),;

	private static final int PREF_WIDTH = 20;

    private String path;
    private double width;
    private double height;

    PictureOption(final String path) {
        this.path = path;

    }

    public String getCorrectedPath() {
		return correctPath(path, PREF_WIDTH);
	}

	public String getCorrectedPath(final int prefWidth) {
		return correctPath(path, prefWidth);
    }

    public double getHeight() {
        return height;
    }

    public String getPath() {
        return path;
    }

    public double getWidth() {
        return width;
    }
    public SVGPath toSVG() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(getCorrectedPath());
        svgPath.maxWidth(PREF_WIDTH);
        width = svgPath.getBoundsInLocal().getWidth();
        height = svgPath.getBoundsInLocal().getWidth();
        svgPath.setFill(Color.TRANSPARENT);
        svgPath.setStroke(Color.BLACK);
        svgPath.setLayoutX(0);
        svgPath.setLayoutY(0);
        svgPath.setScaleX(PREF_WIDTH / width);
        svgPath.setScaleY(PREF_WIDTH / height);
        return svgPath;
    }

	public static String correctPath(final String path, final int prefWidth) {
        Pattern compile = Pattern.compile("([\\d\\.]+)");
        Matcher a = compile.matcher(path);
        double max = 1;
        while (a.find()) {
            String group = a.group(1);
            max = Double.max(Double.parseDouble(group), max);
        }
        a.reset();
        StringBuffer sb = new StringBuffer();
        while (a.find()) {
            String group = a.group(1);
            double parseDouble = Double.parseDouble(group);
            int indexOf = group.indexOf('.');
            if (indexOf == -1) {
                indexOf = 0;
            } else {
                indexOf = group.length() - indexOf - 1;

            }
            String format = "%." + indexOf + "f";
			a.appendReplacement(sb, String.format(Locale.ENGLISH, format, parseDouble * prefWidth / max));
        }
        a.appendTail(sb);
        return sb.toString();
    }

    public static void main(final String[] args) {
        Stream.of(PictureOption.values()).filter(e -> e.path != null).forEach(ConsumerEx.makeConsumer(e -> {
            HasLogging.log().info("{}", e);
            HasLogging.log().info("{}", e.path);
			HasLogging.log().info("{}", correctPath(e.path, PREF_WIDTH));
        }));
    }

}