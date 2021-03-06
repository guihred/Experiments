package extract;

import static org.apache.commons.io.FileUtils.contentEquals;
import static utils.ex.PredicateEx.makeTest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * @author Ben Litchfield
 */
public class PrintImageLocations extends PDFStreamEngine {
    private static final String OUT_FOLDER = "pdf";
    private static final Logger LOG = HasLogging.log();
    private List<PdfImage> images = new ArrayList<>();

    private int num;

    private int pageNumber;
    private File pdfFile;

    public PrintImageLocations(File file) {
        pdfFile = file;
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    public List<PdfImage> processPage(PDPage page, int pageNumber1) throws IOException {
        pageNumber = pageNumber1;
        int size = images.size();
        super.processPage(page);
        if (images.size() > size) {
            return images.subList(size, images.size());
        }
        return Collections.emptyList();
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if (!"Do".equals(operation)) {
            super.processOperator(operator, operands);
            return;
        }

        RunnableEx.ignore(() -> getImage(operands));
    }

    private void getImage(List<COSBase> operands) throws IOException {
        COSName objectName = (COSName) operands.get(0);

        PDXObject xobject = getResources().getXObject(objectName);
        if (xobject instanceof PDFormXObject) {
            PDFormXObject form = (PDFormXObject) xobject;
            showForm(form);
            return;
        }
        if (xobject instanceof PDImageXObject) {
            PDImageXObject image = (PDImageXObject) xobject;
            BufferedImage image2 = image.getImage();
            File save = save( num++, image2, image.getSuffix());

            Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

            // position in user space units. 1 unit = 1/72 inch at 72 dpi
            float translateX = ctmNew.getTranslateX();
            float translateY = ctmNew.getTranslateY();
            // raw size in pixels

            PdfImage pdfImage = new PdfImage();
            pdfImage.setFile(save);
            pdfImage.setX(translateX);
            pdfImage.setY(translateY - ctmNew.getScalingFactorY());
            pdfImage.setPageN(pageNumber);
            images.add(pdfImage);
            LOG.trace("{} at ({},{}) page {}", pdfImage.getFile(), pdfImage.getX(), pdfImage.getY(), pageNumber);

        }
    }

    private File save(Object number, BufferedImage image, String ext) {
        File outFile = ResourceFXUtils.getOutFile(OUT_FOLDER);
        if (!outFile.exists()) {
            outFile.mkdir();
        }
        String extension = "jpx".equals(ext) || "tiff".equals(ext) ? "jpg" : Objects.toString(ext, "png");
        File file = new File(outFile,
            pdfFile.getName().replace(".pdf", "") + pageNumber + "-" + number + "." + extension);
        return SupplierEx.get(() -> {
            ImageIO.write(image, extension, file); // ignore returned boolean
            Optional<File> findFirst = Stream.of(outFile.listFiles()).filter(e -> e.getName().endsWith(extension))
                .filter(e -> !e.equals(file)).filter(makeTest(f -> contentEquals(file, f))).findFirst();
            if (findFirst.isPresent()) {
                RunnableEx.ignore(() -> Files.deleteIfExists(file.toPath()));
                return findFirst.get();
            }
            return null;
        }, file);
    }

}
