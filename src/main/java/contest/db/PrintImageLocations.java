package contest.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import utils.HasLogging;
import utils.ResourceFXUtils;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * @author Ben Litchfield
 */
public class PrintImageLocations extends PDFStreamEngine implements HasLogging {
	private static final Logger LOG = HasLogging.log();
	private int num;
    private List<PDFImage> images = new ArrayList<>();
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
            File save = save(pageNumber, num++, image2, image.getSuffix());

            Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();

            // position in user space units. 1 unit = 1/72 inch at 72 dpi
            float translateX = ctmNew.getTranslateX();
            float translateY = ctmNew.getTranslateY();
            // raw size in pixels

            PDFImage pdfImage = new PDFImage();
            pdfImage.file = save;
            pdfImage.x = translateX;
            pdfImage.y = translateY - ctmNew.getScalingFactorY();
            pdfImage.pageN = pageNumber;
            images.add(pdfImage);
            getLogger().trace("{} at ({},{}) page {}", pdfImage.file, pdfImage.x, pdfImage.y, pageNumber);

        }

    }

    public static File save(int pageNumber, Object numb, BufferedImage image, String ext) {

        String string = "jpx".equals(ext) ? "jpg" : Objects.toString(ext, "png");
        URL url = ResourceFXUtils.toURL("out");
        File file = new File(new File(url.getFile()), pageNumber + "-" + numb + "." + string);
        try {
            ImageIO.write(image, string, file); // ignore returned boolean
        } catch (IOException e) {
			LOG.error("Write error for " + file.getPath() + ": " + e.getMessage(), e);
        }
        return file;
    }

    public static class PDFImage implements HasImage {
        protected File file;
        protected float x;
        protected float y;
        protected int pageN;

        @Override
        public void appendImage(String image) {
            // DOES NOTHING
        }

        @Override
        public String getImage() {
            return file.getName();
        }

        @Override
        public boolean matches(String s0) {
            return false;
        }

        @Override
        public void setImage(String image) {
            file = new File(image);
        }

        @Override
        public String toString() {
            return file != null ? file.getName() : "";
        }
    }

}
