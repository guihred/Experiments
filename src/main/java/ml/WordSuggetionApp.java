package ml;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;

public class WordSuggetionApp extends Application {

    @Override
    public void start(Stage theStage) throws Exception {
        theStage.setTitle("Word Suggestion Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
        theStage.setScene(theScene);
        Word2Vec word2Vec = Word2VecExample.createWord2Vec();
        VocabCache<VocabWord> vocab = word2Vec.getVocab();
        Collection<String> words = vocab.words();
        SortedSet<String> entrySet = new TreeSet<>(words);
        root.getChildren().add(new AutoCompleteTextField(entrySet, word2Vec));

        theStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
