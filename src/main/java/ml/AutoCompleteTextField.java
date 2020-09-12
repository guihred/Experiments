package ml;


import static utils.ex.SupplierEx.nonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import ml.data.AutocompleteField;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import utils.ex.RunnableEx;

public class AutoCompleteTextField extends AutocompleteField {
    private Word2Vec word2Vec;
    public AutoCompleteTextField() {
        RunnableEx.remap(() -> {
            word2Vec = Word2VecExample.createWord2Vec();
            VocabCache<VocabWord> vocab = word2Vec.getVocab();
            Collection<String> words = vocab.words();
            setEntries(new TreeSet<>(words));
            filteredEntries.addAll(entries);
        }, "File Not Found");
    }

    public AutoCompleteTextField(SortedSet<String> entrySet, Word2Vec word2Vec) {
        setEntries(nonNull(entrySet, new TreeSet<>()));
        filteredEntries.addAll(entries);
        this.word2Vec = word2Vec;
    }

    @Override
    protected Collection<String> searchResult(String s) {
        if (word2Vec.hasWord(s)) {
            return word2Vec.wordsNearestSum(s, maxEntries + 1);
        }
        return Collections.emptyList();
    }

}