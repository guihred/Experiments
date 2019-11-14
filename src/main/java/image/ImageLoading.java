package image;

import com.aspose.imaging.Image;
import com.aspose.imaging.RasterCachedImage;
import com.aspose.imaging.RasterImage;
import com.aspose.imaging.Rectangle;
import com.aspose.imaging.fileformats.jpeg.JpegCompressionColorMode;
import com.aspose.imaging.fileformats.jpeg.JpegCompressionMode;
import com.aspose.imaging.fileformats.tiff.enums.TiffExpectedFormat;
import com.aspose.imaging.imageoptions.BmpOptions;
import com.aspose.imaging.imageoptions.JpegOptions;
import com.aspose.imaging.imageoptions.PngOptions;
import com.aspose.imaging.imageoptions.TiffOptions;
import org.slf4j.Logger;
import utils.HasLogging;

public final class ImageLoading {
    private static final Logger LOG = HasLogging.log();

    private ImageLoading() {
    }

    public static void binarize(String dataDir, String nameFile) {
        // For complete examples and data files, please go to
        // https://github.com/aspose-imaging/Aspose.Imaging-for-Java

        // Load an image in an instance of Image
        Image image = Image.load(nameFile);

        // Cast the image to RasterCachedImage
        RasterCachedImage rasterCachedImage = (RasterCachedImage) image;
        // Check if image is cached
        if (!rasterCachedImage.isCached()) {
            // Cache image if not already cached
            rasterCachedImage.cacheData();
        }
        // Binarize image with pre defined fixed threshold
        rasterCachedImage.binarizeFixed((byte) 100);
        // Save the resultant image
        rasterCachedImage.save(dataDir + "BinarizationWithFixedThreshold_out.jpg");
    }

    public static void bradleyThreshold(String dataDir, String nameFile) {
        String sourcepath = nameFile;
        String outputPath = dataDir + "UseBradleythresholding_out.png";

        // Load an existing image.
        com.aspose.imaging.fileformats.png.PngImage objimage = (com.aspose.imaging.fileformats.png.PngImage) Image
            .load(sourcepath);

        // Define threshold value
        final double threshold = 0.15;

        // Call BinarizeBradley method and pass the threshold value as parameter
        objimage.binarizeBradley(threshold);

        // Save the output image
        objimage.save(outputPath);
    }

    /**
     * Converts file in name file to PNG
     * 
     * @param dataDir
     * @param svgFile
     */
    public static void convertSVG(String dataDir, String svgFile) {
        Image image = Image.load(svgFile);

        // Create an instance of PNG options
        PngOptions pngOptions = new PngOptions();

        // Save the results to disk
        image.save(dataDir + "ConvertingSVGToRasterImages_out.png", pngOptions);
    }



    public static void cropImage(String dataDir, String nameFile) {

        RasterImage rasterImage = (RasterImage) Image.load(nameFile);
        // setting for image data to be cashed
        rasterImage.cacheData();

        // Create an instance of Rectangle class and define X,Y and Width, height of the
        // rectangle.
        final Rectangle destRect = new Rectangle(200, 200, 300, 300);

        // Save output image by passing output file name, image options and rectangle
        // object.
        rasterImage.save(dataDir + "ExpandandCropImages_out.jpg", new JpegOptions(), destRect);
    }

    public static void exporting(String dataDir, String nameFile) {
        // Export to BMP file format using the default options
        Image image = Image.load(nameFile);
        image.save(dataDir + "ExportImageToDifferentFormats_out.bmp", new BmpOptions());
        // Export to JPEG file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.jpeg", new JpegOptions());
        // Export to PNG file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.png", new PngOptions());
        // Export to TIFF file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.tiff", new TiffOptions(TiffExpectedFormat.Default));
        // Display Status.
        LOG.info("Image exported to BMP, JPG, PNG and TIFF formats successfully!");
    }

    public static void grayScale(String dataDir, String nameFile) {
        Image original = Image.load(nameFile);
        try (JpegOptions jpegOptions = new JpegOptions()) {
            jpegOptions.setColorType(JpegCompressionColorMode.Grayscale);
            jpegOptions.setCompressionType(JpegCompressionMode.Progressive);
            original.save(dataDir + "result.jpg", jpegOptions);
        } finally {
            original.dispose();
        }
    }

    public static void grayScaling(String dataDir, String nameFile) {

        Image image = Image.load(nameFile);
        // Cast the image to RasterCachedImage
        RasterCachedImage rasterCachedImage = (RasterCachedImage) image;
        // Check if image is cached
        if (!rasterCachedImage.isCached()) {
            // Cache image if not already cached
            rasterCachedImage.cacheData();
        }
        // Transform image to its grayscale representation
        rasterCachedImage.grayscale();
        // Save the resultant image
        rasterCachedImage.save(dataDir + "Grayscaling_out.jpg");

    }

}
