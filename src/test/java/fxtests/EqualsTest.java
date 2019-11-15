package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.ResourceFXUtils.getOutFile;

import contest.db.Contest;
import contest.db.ContestQuestion;
import contest.db.ContestQuestionAnswer;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.PointCh4;
import ex.j9.ch4.PrimaryColor;
import ex.j9.ch4.RectangleCh4;
import extract.PdfImage;
import gaming.ex01.SnakeSquare;
import gaming.ex03.SlidingPuzzleSquare;
import gaming.ex04.TronSquare;
import gaming.ex13.SolitaireCard;
import gaming.ex16.MadCell;
import gaming.ex16.MadEdge;
import gaming.ex16.MadEdgeDistance;
import gaming.ex21.City;
import gaming.ex21.PlayerColor;
import graphs.EdgeElement;
import graphs.Vertex;
import graphs.entities.EdgeDistancePack;
import graphs.entities.Linha;
import graphs.entities.Ponto;
import japstudy.JapaneseLesson;
import japstudy.LessonPK;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import org.junit.Test;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class EqualsTest extends AbstractTestExecution {
    @Test
    public void testPdfImage() {
        measureTime("PdfImage.equals", () -> {
            Path firstPathByExtension = ResourceFXUtils.getFirstPathByExtension(getOutFile(), "png");
            PdfImage pdfImage = new PdfImage();
            File file = firstPathByExtension.toFile();
            pdfImage.appendImage("");
            pdfImage.getFile();
            pdfImage.toString();
            pdfImage.setFile(file);
            pdfImage.toString();
            pdfImage.getImage();
            pdfImage.getPageN();
            pdfImage.getX();
            pdfImage.getY();
            pdfImage.matches("");
            pdfImage.setImage(file.getAbsolutePath());
            pdfImage.setPageN(0);
            pdfImage.setX(0);
            pdfImage.setY(0);
            pdfImage.toString();
        });
    }

    @Test
    public void testPoints() {
        measureTime("Test.equals", () -> {
            Set<Object> equalsTest = new LinkedHashSet<>(getList());
            Set<Object> equalsTest2 = new LinkedHashSet<>(getList());
            equalsTest.forEach(e -> equalsTest2.stream().anyMatch(a -> Objects.equals(a, e)));
            equalsTest.toString();
        });
    }

    private static List<Object> getList() {
        Vertex v = new Vertex(5);
        List<Object> asList = Arrays.asList(new PointCh4(2, 4), new LabeledPoint("Oi", 3, 5), PrimaryColor.RED,
            new ContestQuestion(), new SlidingPuzzleSquare(2), new TronSquare(),
            new EdgeDistancePack(new Linha(new Ponto(2, 4, null), new Ponto(2, 4, null)), 5), new MadCell(2),
            new RectangleCh4(new PointCh4(2, 4), 3, 5), new EdgeElement(v, null, 2), new EdgeElement(v, v, 5),
            new Contest(), new LessonPK(), new MadEdge(null, null), new MadEdgeDistance(null, 2F), new SnakeSquare(),
            new JapaneseLesson(), new ContestQuestionAnswer(), new SolitaireCard(null, null),
            new City(PlayerColor.BLUE));
        Collections.shuffle(asList);
        return asList;
    }
}
