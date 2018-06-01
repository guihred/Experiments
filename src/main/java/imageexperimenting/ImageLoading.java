package imageexperimenting;

import com.aspose.imaging.Color;
import com.aspose.imaging.Image;
import com.aspose.imaging.RasterCachedImage;
import com.aspose.imaging.RasterImage;
import com.aspose.imaging.Rectangle;
import com.aspose.imaging.fileformats.bmp.BmpImage;
import com.aspose.imaging.fileformats.jpeg.JpegCompressionColorMode;
import com.aspose.imaging.fileformats.jpeg.JpegCompressionMode;
import com.aspose.imaging.fileformats.jpeg.JpegImage;
import com.aspose.imaging.fileformats.tiff.enums.TiffExpectedFormat;
import com.aspose.imaging.imageoptions.BmpOptions;
import com.aspose.imaging.imageoptions.JpegOptions;
import com.aspose.imaging.imageoptions.PngOptions;
import com.aspose.imaging.imageoptions.TiffOptions;
import com.aspose.imaging.sources.FileCreateSource;

public class ImageLoading {
    public static void main(String[] args) {
        String dataDir = "C:\\Users\\guilherme.hmedeiros\\Pictures\\";
        String nameFile = dataDir + "eu3.jpg";
        String svgFile = dataDir + "Video_game.svg";

        // createThumnails(dataDir, nameFile);
        convertSVG(dataDir, svgFile);

    }

    static void convertSVG(String dataDir, String nameFile) {
        // TODO Auto-generated method stub
        Image image = Image.load(nameFile);

        // Create an instance of PNG options
        PngOptions pngOptions = new PngOptions();

        // Save the results to disk
        image.save(dataDir + "ConvertingSVGToRasterImages_out.png", pngOptions);
    }

    static void exporting(String dataDir, String nameFile) {
        // Export to BMP file format using the default options
        Image image = Image.load(nameFile);
        image.save(dataDir + "ExportImageToDifferentFormats_out.bmp", new com.aspose.imaging.imageoptions.BmpOptions());

        // Export to JPEG file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.jpeg",
                new com.aspose.imaging.imageoptions.JpegOptions());

        // Export to PNG file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.png", new com.aspose.imaging.imageoptions.PngOptions());

        // Export to TIFF file format using the default options
        image.save(dataDir + "ExportImageToDifferentFormats_out.tiff", new TiffOptions(TiffExpectedFormat.Default));

        // Display Status.
        System.out.println("Image exported to BMP, JPG, PNG and TIFF formats successfully!");
    }

    static void grayScale(String dataDir, String nameFile) {
        Image original = Image.load( nameFile);
        try {
            JpegOptions jpegOptions = new JpegOptions();
            jpegOptions.setColorType(JpegCompressionColorMode.Grayscale);
            jpegOptions.setCompressionType(JpegCompressionMode.Progressive);
            original.save(dataDir + "result.jpg", jpegOptions);
        } finally {
            original.dispose();
        }
    }

    private static void cropImage(String dataDir, String nameFile) {

        RasterImage rasterImage = (RasterImage) Image.load(nameFile);
        // setting for image data to be cashed
        rasterImage.cacheData();

        // Create an instance of Rectangle class and define X,Y and Width, height of the
        // rectangle.
        Rectangle destRect = new Rectangle(200, 200, 300, 300);

        // Save output image by passing output file name, image options and rectangle
        // object.
        rasterImage.save(dataDir + "ExpandandCropImages_out.jpg", new JpegOptions(), destRect);
    }

    static void bradleyThreshold(String dataDir, String nameFile) {
        String sourcepath = nameFile;
        String outputPath = dataDir + "UseBradleythresholding_out.png";

        // Load an existing image.
        com.aspose.imaging.fileformats.png.PngImage objimage = (com.aspose.imaging.fileformats.png.PngImage) com.aspose.imaging.Image
                .load(sourcepath);

        // Define threshold value
        double threshold = 0.15;

        // Call BinarizeBradley method and pass the threshold value as parameter
        objimage.binarizeBradley(threshold);

        // Save the output image
        objimage.save(outputPath);
    }

    static void grayScaling(String dataDir, String nameFile) {

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

    static void binarize(String dataDir, String nameFile) {
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

    static void createThumnails(String dataDir, String nameFile) {
        JpegImage image = (JpegImage) Image.load(nameFile);

        // Get the image thumbnail information and save it in an instance of
        // JpegImage
        JpegImage thumbnail = (JpegImage) image.getExifData().getThumbnail();

        // Retrieve the thumbnail bitmap information/Pixels in an array of type
        // Color
        Color[] pixels = thumbnail.loadPixels(new Rectangle(0, 0, thumbnail.getWidth(), thumbnail.getHeight()));

        // To save the thumbnail as BMP image, create an instance of BmpOptions
        BmpOptions bmpOptions = new BmpOptions();

        // Set file source in which the results will be stores; last Boolean
        // parameter denotes isTemporal
        bmpOptions.setSource(new FileCreateSource(dataDir + "RetrieveThumbnailBitmapInformation_out.jpg", false));

        // Create a BmpImage while using the instance of BmpOptions and
        // providing resultant dimensions
        BmpImage bmpImage = (BmpImage) Image.create(bmpOptions, thumbnail.getWidth(), thumbnail.getHeight());

        // Copy the thumbnail pixels onto the newly created canvas
        bmpImage.savePixels(bmpImage.getBounds(), pixels);

        // Save the results
        bmpImage.save();
    }
}