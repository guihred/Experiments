package paintexp.svgcreator;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.HasLogging;

public class SVGChanger {
    private static final Logger LOG = HasLogging.log();

    double currentX = 0;
    double currentY = 0;
    final String path;

    public SVGChanger(String path) {
        this.path = path;
    }

    public String convertToRelative() {
        Pattern compile = Pattern.compile("([A-Za-z][^A-Za-z]+)");
        Matcher matcher = compile.matcher(path);
        matcher.reset();
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String svgCommand = matcher.group(1).trim();
            LOG.info(svgCommand);
            String replace = replaceCommand(svgCommand);
            LOG.info(replace);
            matcher.appendReplacement(sb, replace);
            String currentPosition = String.format(Locale.ENGLISH, "(%.2f,%.2f)", currentX, currentY);
            LOG.info(currentPosition);
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    private  boolean isUpperCase(final String substring) {
        return substring.matches("[A-Z]");
    }

    private  String replaceCommand(String group) {
        String svgCommand = group.substring(0, 1);
        int args = SVGCommand.valueOf(svgCommand.toUpperCase()).getArgs();
        String[] split = group.split("[\\s,[A-Za-z]]+|(?<=\\d)(?=-)");
        if (args == 1) {
            return replaceOneCommand(split, svgCommand);
        }
        if (args % 2 == 0) {
            return replaceEvenCommand(split, svgCommand, args);
        }
        if (args == 7) {
            return replaceOddCommand(split, svgCommand, args);
        }
        return group;
    }

    private  String replaceEvenCommand(final String[] split, final String substring, final int args) {
        StringBuilder cmd = new StringBuilder();
        for (int i = 2; i < split.length; i += 2) {
            double xCoord = Double.parseDouble(split[i - 1]);
            double yCoord = Double.parseDouble(split[i]);
            if (isUpperCase(substring)) {
                double rx = currentX;
                double ry = currentY;
                if (i % args == 0) {
                    currentX = xCoord;
                }
                xCoord = xCoord - rx;
                if (i % args == 0) {
                    currentY = yCoord;
                }
                yCoord = yCoord - ry;
            } else {
                if (i % args == 0) {
                    currentX += xCoord;
                    currentY += yCoord;
                }
            }
            String format = String.format(Locale.ENGLISH, "%.1f %.1f ", xCoord, yCoord);
            cmd.append(format);
        }
        return String.format("%s%s", substring.toLowerCase(), cmd.toString());
    }

    private  String replaceOddCommand(final String[] split, final String substring, final int args) {
        StringBuilder cmd = new StringBuilder();
        for (int i = args; i < split.length; i += args) {
            double xCoord = Double.parseDouble(split[i - 1]);
            double yCoord = Double.parseDouble(split[i]);
            if (isUpperCase(substring)) {
                double rx = currentX;
                double ry = currentY;
                currentX = xCoord;
                xCoord = xCoord - rx;
                currentY = yCoord;
                yCoord = yCoord - ry;
            } else {
                currentX += xCoord;
                currentY += yCoord;
            }
            String otherArgs = Stream.of(split).skip(1L + i - args).limit(args - 2L).collect(Collectors.joining(" "));
            String format = String.format(Locale.ENGLISH, "%s%s %.1f %.1f", substring.toLowerCase(), otherArgs, xCoord,
                    yCoord);
            cmd.append(format);
        }
        return cmd.toString();
    }

    private  String replaceOneCommand(final String[] split, final String substring) {
        StringBuilder cmd = new StringBuilder();
        for (int i = 1; i < split.length; i += 2) {
            double coord = Double.parseDouble(split[i]);
            boolean vertical = "v".equals(substring);
            boolean horizontal = !vertical;
            if (isUpperCase(substring)) {
                double rx = currentX;
                double ry = currentY;
                currentX = vertical ? currentX : coord;
                currentY = horizontal ? currentY : coord;
                coord -= vertical ? ry : rx;
            } else {
                currentX += vertical ? 0 : coord;
                currentY += horizontal ? 0 : coord;
            }
            String format = String.format(Locale.ENGLISH, " %.1f ", coord);
            cmd.append(format);
        }
        return String.format("%s%s", substring.toLowerCase(), cmd.toString());
    }

    public static void main(final String[] args) {
        String original = "M10,0 l3,5 6,0 -3,5 3,5 -6,0 -3,5 -3,-5 -6,0 3,-5 -3,-5 6,0z";
        String convertToRelative = new SVGChanger(original).convertToRelative();
        LOG.info(convertToRelative);
        LOG.info(original);

    }

}
