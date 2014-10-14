package org.twittersearch.app.topic_modelling;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Mandy Roick on 04.08.2014.
 */
public class StemmerPipe extends Pipe {

    private Map<String, TreeMap<String, Integer>> stemmingDictionary;

    public StemmerPipe() {
        this.stemmingDictionary = new HashMap<String, TreeMap<String, Integer>>();
    }

    @Override public Instance pipe(Instance carrier) {
        PorterStemmer stemmer = new PorterStemmer();
        TokenSequence in = (TokenSequence) carrier.getData();

        String originalWord;
        String stemmedWord;
        for (Token token : in) {
            originalWord = token.getText();

            stemmer.setCurrent(originalWord);
            stemmer.stem();
            stemmedWord = stemmer.getCurrent();
            token.setText(stemmedWord);

            //if(!stemmedWord.equals(originalWord)) {
                updateStemmingDictionary(originalWord, stemmedWord);
            //}
        }

        return carrier;
    }

    private void updateStemmingDictionary(String originalWord, String stemmedWord) {
        TreeMap<String, Integer> originalWords = this.stemmingDictionary.get(stemmedWord);
        if (originalWords == null) {
            originalWords = new TreeMap<String, Integer>();
        }
        Integer originalWordCount = originalWords.get(originalWord);
        if (originalWordCount == null) {
            originalWordCount = 1;
        } else {
            originalWordCount = originalWordCount +1;
        }
        originalWords.put(originalWord, originalWordCount);
        this.stemmingDictionary.put(stemmedWord, originalWords);
    }

    public Map<String, String> getFinalStemmingDictionary() {
        Map<String, String> result = new HashMap<String, String>();
        for (String stemmedWord : this.stemmingDictionary.keySet()) {
            result.put(stemmedWord,getBestOriginalWord(stemmedWord));
        }
        return result;
    }

    private String getBestOriginalWord(String stemmedWord) {
        TreeMap<String, Integer> originalWords = this.stemmingDictionary.get(stemmedWord);
        if (originalWords == null) {
            return stemmedWord;
        }
        return originalWords.firstKey();
    }

    public static String stem(String input) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(input);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
