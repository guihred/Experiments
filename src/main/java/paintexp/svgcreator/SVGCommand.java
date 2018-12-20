package paintexp.svgcreator;

import java.util.Objects;

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

    @Override
    public String toString() {
        return Objects.toString(commandName, super.toString());
    }
}