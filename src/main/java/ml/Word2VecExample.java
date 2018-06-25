package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simplebuilder.HasLogging;

public class Word2VecExample implements HasLogging {

    private static final String RAW_SENTENCES_TXT = "raw_sentences.txt";
    private static final String PATH_TO_SAVE_MODEL_TXT = "pathToSaveModel.zip";
    static Logger log = LoggerFactory.getLogger(Word2VecExample.class);


    public static void fit() throws FileNotFoundException {
        Word2Vec word2Vec = createWord2Vec();

        SentenceIterator iterator = new BasicLineIterator(RAW_SENTENCES_TXT);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        word2Vec.setTokenizerFactory(tokenizerFactory);
        word2Vec.setSentenceIterator(iterator);

        log.info("Word2vec uptraining...");

        word2Vec.fit();

    }
    public static Word2Vec createWord2Vec() throws FileNotFoundException {
        if (new File(PATH_TO_SAVE_MODEL_TXT).exists()) {
            return WordVectorSerializer.readWord2VecModel(PATH_TO_SAVE_MODEL_TXT);

        }

        File filePath = Paths.get(RAW_SENTENCES_TXT).toFile().getAbsoluteFile();
        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(10)
                .seed(42)
                .windowSize(5)
                .allowParallelTokenization(true)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();
        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");
        WordVectorSerializer.writeWord2VecModel(vec, PATH_TO_SAVE_MODEL_TXT);
        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");
        return vec;
    }

}