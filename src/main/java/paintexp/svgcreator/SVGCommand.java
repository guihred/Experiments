package paintexp.svgcreator;

import java.util.Locale;
import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

public enum SVGCommand {
    M("Move To", 2),
    Z("Close Path", 0),
    H("Horizontal Line To", 1),
    V("Vertical Line To", 1),
    L("Line To", 2),
    T("Smooth Quadratic Bezier Curve To", 2),
    Q("Quadratic Bezier Curve To", 4),
    S("Smooth Curve To", 4),
    C("Curve To", 6),
    A("Elliptical Arc", 7);
    private String commandName;
    private final int args;

    SVGCommand(String name, int args) {
        commandName = name;
        this.args = args;
    }

    public int getArgs() {
        return args;
    }

    public String getFormat(String content2, double eventX, double eventY, ObservableList<Point2D> points2,
        Point2D lastPoints) {
        Point2D point2d = points2.get(0);
        double initialX = point2d.getX();
        double initialY = point2d.getY();
        double y = Math.max(0, eventY);
        double x = Math.max(0, eventX);
        switch (args) {
            case 0:
                return String.format(Locale.ENGLISH, "%s %s ", content2, name());
            case 1:
                double length = this == SVGCommand.V ? y : x;
                return String.format(Locale.ENGLISH, "%s %s %.1f ", content2, name(), length);
            case 2:
                return String.format(Locale.ENGLISH, "%s %s %.1f %.1f ", content2, name(), x, y);
            case 4:
                return String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f ", content2, name(), x, y, initialX,
                    initialY);
            case 6:
                return getCommandFor6(x, y, initialX, initialY, content2, points2);
            case 7:
                return getArcCommand(x, y, initialX, initialY, content2, points2, lastPoints);
            default:
                return content2;
        }
    }

    @Override
    public String toString() {
        return Objects.toString(commandName, super.toString());
    }

    private String getArcCommand(final double x, final double y, final double initialX, final double initialY,
        String content, ObservableList<Point2D> points, Point2D lastPoints) {
        double d = initialX - lastPoints.getX();
        double rx = x - initialX + d / 2;
        double f = initialY - lastPoints.getY();
        double ry = y - initialY + f / 2;
        int largeArc = rx * rx + ry * ry > (d * d + f * f) / 4 ? 1 : 0;
        Point2D e2 = new Point2D(initialX - d / 2, initialY - f / 2);
        if (!points.contains(e2)) {
            points.add(e2);
        }
        double degrees = Math.toDegrees(Math.atan2(ry, rx));
        double m = (x - initialX) * (lastPoints.getY() - initialY) - (y - initialY) * (lastPoints.getX() - initialX);
        int sweepFlag = m < 0 ? 1 : 0;
        return String.format(Locale.ENGLISH, "%s %s %.0f %.0f %.1f %d %d %.1f %.1f ", content, name(), rx, ry, degrees,
            largeArc, sweepFlag, initialX, initialY);
    }

    private String getCommandFor6(final double x, final double y, final double initialX, final double initialY,
        String content, ObservableList<Point2D> points) {
        double secondX = points.size() > 1 ? points.get(1).getX() : x;
        double secondY = points.size() > 1 ? points.get(1).getY() : y;
        return String.format(Locale.ENGLISH, "%s %s %.1f %.1f %.1f %.1f %.1f %.1f ", content, name(), secondX, secondY,
            x, y, initialX, initialY);
    }

}