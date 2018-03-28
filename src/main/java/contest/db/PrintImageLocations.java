package contest.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * This is an example on how to get the x/y coordinates of image locations.
 *
 * @author Ben Litchfield
 */
public class PrintImageLocations extends PDFStreamEngine {
    /**
     * Default constructor.
     *
     * @throws IOException
     *             If there is an error loading text stripper properties.
     */
    public PrintImageLocations() throws IOException {
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    /**
     * This will print the documents data.
     *
     * @param args
     *            The command line arguments.
     *
     * @throws IOException
     *             If there is an error parsing the document.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            usage();
        } else {
            PDDocument document = null;
            try {
                document = PDDocument.load(new File(args[0]));
                PrintImageLocations printer = new PrintImageLocations();
                for (PDPage page : document.getPages()) {
                    printer.processPage(page);
                }
            } finally {
                if (document != null) {
                    document.close();
                }
            }
        }
    }

    /**
     * This is used to handle an operation.
     *
     * @param operator
     *            The operation to perform.
     * @param operands
     *            The list of arguments.
     *
     * @throws IOException
     *             If there is an error processing the operation.
     */
    int num = 0;

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
            ContestReader.save(num++, image2, image.getSuffix());
        }

    }

    /**
     * This will print the usage for this document.
     */
    private static void usage() {
        System.err.println("Usage: java " + PrintImageLocations.class.getName() + " <input-pdf>");
    }

}