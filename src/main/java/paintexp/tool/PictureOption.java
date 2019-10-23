package paintexp.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

enum PictureOption {
    TRIANGLE("M 12,4 L 20,20 4,20 12,4z"),
    SCALENE("M 0,0 l 0,20 20,0 Z "),
    DIAMOND("M 20,0 l 10,10 -10,10 -10,-10 Z "),
    PENTAGON("M 10,0 L 0,7 L 4,19 L 16,19 L 20,7 Z"),
    HEXAGON("M 20,9 L 15,17.310350 5,17.310350 0,9 5,0.061711 15,0.061711 20,9 z"),
    ARROW_RIGHT("m 3,7 l10,0 0,-7 10,10 -10,10 0,-7 -10,0 z"),
    ARROW_LEFT("m 0,20 l10,10 0,-7 10,0 0,-7 -10,0 0,-7 z"),
    ARROW_UP("m 20,0 l10,10 -7,0 0,10 -7,0 0,-10 -7,0 z"),
    ARROW_DOWN("m 0,20 l0,10 -7,0 10,10 10,-10 -7,0 0,-10 z"),
    STAR_4("M 10 4 L 11.9385 9.8393 L 18 12 L 11.9385 14.1607 L 10 20 L 7.6171 14.1607 L 2 12 L 7.6171 9.8393 Z"),
    STAR_5("m10,0 2,6h6l-5,4 2,6-5-4-5,4 2-6-5-4h6z"),
    STAR_6("M10,0 l3,5 6,0 -3,5 3,5 -6,0 -3,5 -3,-5 -6,0 3,-5 -3,-5 6,0z"),
    HEART("M15.53 1A5.52 5.52 0 0 0 11 6 5.52 5.52 0 0 0 0 6C0 12.37 11 20 11 20s11-7.63 11-13.42"
        + "A5.53 5.53 0 0 0 15.53 1z"),;

    private static final int PREF_WIDTH = 20;

    private String path;
    private double width;
    private double height;

    PictureOption(final String path) {
        this.path = path;

    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        String string = super.toString().toLowerCase();
        Pattern compile = Pattern.compile("(^\\w|_\\w)");
        Matcher a = compile.matcher(string);
        StringBuffer sb = new StringBuffer();
        while (a.find()) {
            String group = a.group(1);
            if (group != null) {
                a.appendReplacement(sb, group.toUpperCase().replace('_', ' '));
            }
        }
        a.appendTail(sb);
        return sb.toString();
    }

    public SVGPath toSVG() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(getPath());
        svgPath.maxWidth(PREF_WIDTH);
        width = svgPath.getBoundsInLocal().getWidth();
        height = svgPath.getBoundsInLocal().getHeight();
        svgPath.setFill(Color.TRANSPARENT);
        svgPath.setStroke(Color.BLACK);
        svgPath.setLayoutX(0);
        svgPath.setLayoutY(0);
        svgPath.setScaleX(PREF_WIDTH / width);
        svgPath.setScaleY(PREF_WIDTH / height);
        return svgPath;
    }

}