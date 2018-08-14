package contest.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import simplebuilder.HasLogging;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * @author Ben Litchfield
 */
public class PrintImageLocations extends PDFStreamEngine implements HasLogging {
    int num = 0;
    List<PDFImage> images = new ArrayList<>();
    private int pageNumber;

    public PrintImageLocations() {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

	public List<PDFImage> processPage(PDPage page, int pageNumber1) throws IOException {
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
            File save = save(num++, image2, image.getSuffix());

            Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

            // position in user space units. 1 unit = 1/72 inch at 72 dpi
            float translateX = ctmNew.getTranslateX();
            float translateY = ctmNew.getTranslateY();
            // raw size in pixels

            PDFImage pdfImage = new PDFImage();
            pdfImage.file = save;
            pdfImage.x = translateX;
            pdfImage.y = translateY - ctmNew.getScalingFactorY();
            pdfImage.pageNumber = pageNumber;
            images.add(pdfImage);
            getLogger().trace("{}", image);
            getLogger().trace("{} at ({},{}) page {}", pdfImage.file, pdfImage.x, pdfImage.y, pageNumber);

        }

    }

    public static File save(Object numb, BufferedImage image, String ext) {

        File file = new File("teste" + numb + "." + ext);
        try {
            ImageIO.write(image, ext, file); // ignore returned boolean
        } catch (IOException e) {
            HasLogging.log().error("Write error for " + file.getPath() + ": " + e.getMessage(), e);
        }
        return file;
    }

}
class PDFImage{
    File file;
    float x;
    float y;
    int pageNumber;
}
