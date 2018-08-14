package imageexperimenting;

import org.slf4j.Logger;

import com.aspose.imaging.Color;
import com.aspose.imaging.Figure;
import com.aspose.imaging.HatchStyle;
import com.aspose.imaging.Pen;
import com.aspose.imaging.PointF;
import com.aspose.imaging.RectangleF;
import com.aspose.imaging.SizeF;

import simplebuilder.HasLogging;

public class ImageCreating {
    public static void main(String[] args) {
        String dataDir = "C:\\Users\\guilherme.hmedeiros\\Pictures\\";
        String nameFile = dataDir + "eu3.jpg";
        creating(dataDir, nameFile);

    }

    public static void creating(String dataDir, String nameFile) {
        Logger log = HasLogging.log();

        // For complete examples and data files, please go to
        // https://github.com/Muhammad-Adnan-Ahmad/Aspose.Imaging-for-Java
        // Create an instance of BmpCreateOptions and set its various properties
        com.aspose.imaging.imageoptions.BmpOptions createOptions = new com.aspose.imaging.imageoptions.BmpOptions();
        createOptions.setBitsPerPixel(24);

        // Create an instance of FileCreateSource and assign it to Source property
        createOptions.setSource(new com.aspose.imaging.sources.FileCreateSource(nameFile, false));

        // Create an instance of Image
        com.aspose.imaging.Image image = com.aspose.imaging.Image.create(createOptions, 500, 500);

        // Create and initialize an instance of Graphics
        com.aspose.imaging.Graphics graphics = new com.aspose.imaging.Graphics(image);

        // Clear the image surface with white color
        graphics.clear(Color.getWhite());

        // Create an instance of GraphicsPath
        com.aspose.imaging.GraphicsPath graphicspath = new com.aspose.imaging.GraphicsPath();

        // Create an instance of Figure
        com.aspose.imaging.Figure figure = new com.aspose.imaging.Figure();

        // Add Arc shape to the figure by defining boundary Rectangle
        figure.addShape(new com.aspose.imaging.shapes.ArcShape(new RectangleF(10, 10, 300, 300), 0, 45));

        // Add Arc Polygon shape to the figure by defining boundary Rectangle
        figure.addShape(new com.aspose.imaging.shapes.PolygonShape(
                new PointF[] { new PointF(150, 10), new PointF(150, 200), new PointF(250, 300), new PointF(350, 400) },
                true));

        // Add Arc Polygon shape to the figure by defining boundary Rectangle
        figure.addShape(new com.aspose.imaging.shapes.RectangleShape(
                new RectangleF(new PointF(250, 250), new SizeF(200, 200))));

        // Add figures to the GraphicsPath object
        graphicspath.addFigures(new Figure[] { figure });

        // Draw Path
        graphics.drawPath(new Pen(com.aspose.imaging.Color.getBlack(), 2), graphicspath);

        // Create an instance of HatchBrush and set its properties
        com.aspose.imaging.brushes.HatchBrush hatchbrush = new com.aspose.imaging.brushes.HatchBrush();
        hatchbrush.setBackgroundColor(com.aspose.imaging.Color.getBrown());
        hatchbrush.setForegroundColor(com.aspose.imaging.Color.getBlue());
        hatchbrush.setHatchStyle(HatchStyle.Vertical);

        // Fill path by supplying the brush and GraphicsPath objects
        graphics.fillPath(hatchbrush, graphicspath);

        // Save the final image.
        image.save();

        // Display Status.
        log.info("Processing completed successfully!");
    }
}
