package ml;

import java.io.File;
import java.io.FileNotFoundException;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

public final class Word2VecExample {

    private static final String RAW_SENTENCES_TXT = "raw_sentences.txt";
    public static final String PATH_TO_SAVE_MODEL_TXT = "zip/pathToSaveModel.zip";
    private static final Logger LOG = HasLogging.log();

    private Word2VecExample() {
    }

    public static Word2Vec createWord2Vec() throws FileNotFoundException {
        File pathToSave = getPathToSave();
        if (pathToSave.exists()) {
            return WordVectorSerializer.readWord2VecModel(pathToSave.getAbsoluteFile());

        }

        File filePath = ResourceFXUtils.toFile(RAW_SENTENCES_TXT).getAbsoluteFile();
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
         * CommonPreprocessor will apply the following regex to each token:
         * [\d\.:,"'\(\)\[\]|/?!;]+ So, effectively all numbers, punctuation symbols and
         * some special symbols are stripped off. Additionally it forces lower case for
         * all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        final int seed = 42;
        Word2Vec vec = new Word2Vec.Builder().minWordFrequency(5).iterations(1).layerSize(10).seed(seed).windowSize(5)
                .allowParallelTokenization(true).iterate(iter).tokenizerFactory(t).build();
        vec.fit();

        LOG.info("Writing word vectors to text file....");
        WordVectorSerializer.writeWord2VecModel(vec, pathToSave);
        // Prints out the closest 10 words to "day". An example on what to do with these
        // Word Vectors.
        LOG.info("Closest Words:");
        return vec;
    }


    public static File getPathToSave() {
        return ResourceFXUtils.getOutFile(PATH_TO_SAVE_MODEL_TXT);
    }

}