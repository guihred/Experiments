package image;

import com.aspose.imaging.*;
import com.aspose.imaging.brushes.HatchBrush;
import com.aspose.imaging.imageoptions.BmpOptions;
import com.aspose.imaging.shapes.ArcShape;
import com.aspose.imaging.shapes.PolygonShape;
import com.aspose.imaging.shapes.RectangleShape;
import com.aspose.imaging.sources.FileCreateSource;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ImageCreating {
    private static final Logger LOG = HasLogging.log(ImageCreating.class);

    public static void creating(String nameFile) {
        // For complete examples and data files, please go to
        // https://github.com/Muhammad-Adnan-Ahmad/Aspose.Imaging-for-Java
        // Create an instance of BmpCreateOptions and set its various properties
        BmpOptions createOptions = new BmpOptions();
        createOptions.setBitsPerPixel(24);

        // Create an instance of FileCreateSource and assign it to Source property
        createOptions.setSource(new FileCreateSource(nameFile, false));

        // Create an instance of Image
        Image image = Image.create(createOptions, 500, 500);

        // Create and initialize an instance of Graphics
        Graphics graphics = new Graphics(image);

        // Clear the image surface with white color
        graphics.clear(Color.getWhite());

        // Create an instance of GraphicsPath
        GraphicsPath graphicspath = new GraphicsPath();

        // Create an instance of Figure
        Figure figure = new Figure();

        // Add Arc shape to the figure by defining boundary Rectangle
        figure.addShape(new ArcShape(new RectangleF(10, 10, 300, 300), 0, 45));

        // Add Arc Polygon shape to the figure by defining boundary Rectangle
        figure.addShape(new PolygonShape(
                new PointF[] { new PointF(150, 10), new PointF(150, 200), new PointF(250, 300), new PointF(350, 400) },
                true));

        // Add Arc Polygon shape to the figure by defining boundary Rectangle
        figure.addShape(new RectangleShape(
                new RectangleF(new PointF(250, 250), new SizeF(200, 200))));

        // Add figures to the GraphicsPath object
        graphicspath.addFigures(new Figure[] { figure });

        // Draw Path
        graphics.drawPath(new Pen(Color.getBlack(), 2), graphicspath);

        // Create an instance of HatchBrush and set its properties
        HatchBrush hatchbrush = new HatchBrush();
        hatchbrush.setBackgroundColor(Color.getBrown());
        hatchbrush.setForegroundColor(Color.getBlue());
        hatchbrush.setHatchStyle(HatchStyle.Vertical);

        // Fill path by supplying the brush and GraphicsPath objects
        graphics.fillPath(hatchbrush, graphicspath);

        // Save the final image.
        image.save();

        // Display Status.
        LOG.info("Processing completed successfully!");
    }

    public static void main(String[] args) {
        String dataDir = ResourceFXUtils.getUserFolder("Pictures").getAbsolutePath();
        String nameFile = dataDir + "\\eu3.jpg";
        creating(nameFile);

    }
}
