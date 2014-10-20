package org.twittersearch.app.topic_modelling;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mandy Roick on 04.08.2014.
 */
public class StemmerPipe extends Pipe {

    private Map<String, Map<String, Integer>> stemmingDictionary;

    public StemmerPipe() {
        this.stemmingDictionary = new HashMap<String, Map<String, Integer>>();
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
        Map<String, Integer> originalWords = this.stemmingDictionary.get(stemmedWord);
        if (originalWords == null) {
            originalWords = new HashMap<String, Integer>();
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
        Map<String, Integer> originalWords = this.stemmingDictionary.get(stemmedWord);
        if (originalWords == null) {
            // return null and do not add to Map, obwohl, eigentlich sollte der Fall nicht eintreten
            return stemmedWord;
        }

        Map.Entry<String, Integer> originalWordWithHighestCount =
                Collections.max(originalWords.entrySet(), new Comparator<Map.Entry<String, Integer>>() {
                                                                @Override
                                                                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                                                                    return o1.getValue() > o2.getValue() ? 1 : -1;
                                                                }
                                                            });
        return originalWordWithHighestCount.getKey();
    }

    public static String stem(String input) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(input);
        stemmer.stem();
        return stemmer.getCurrent();
    }
}
