package org.twittersearch.app.topic_modelling;

import org.twittersearch.app.helper.FileReaderHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 11.10.2014.
 */
public class TopicMatcher {

    public static void main(String[] args) {
        Map<Integer, String[]> topWords1 = FileReaderHelper.readTopWords("trimmed_tm-200_2014-10-05_top_words.results");
        Map<Integer, String[]> topWords2 = FileReaderHelper.readTopWords("trimmed_tm-200_2014-10-04_top_words.results");
        Map<Integer, Integer> matching = matchTopics(topWords1, topWords2);

        for (Map.Entry<Integer, Integer> entry : matching.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
    }

    public static Map<Integer, Integer> matchTopics(Map<Integer, String[]> topics1, Map<Integer, String[]> topics2) {
        Map<Integer,Integer> mapping = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, String[]> topic1 : topics1.entrySet()) {
            Map.Entry<Integer, String[]> bestMatch = null;
            double bestMatchEvaluation = 0;
            double currentMatchEvaluation;
            for (Map.Entry<Integer, String[]> topic2 : topics2.entrySet()) {
                currentMatchEvaluation = compareTopics(topic1, topic2);
                if ((bestMatch == null) || (currentMatchEvaluation > bestMatchEvaluation)) {
                    bestMatch = topic2;
                    bestMatchEvaluation = currentMatchEvaluation;
                }
            }

            mapping.put(topic1.getKey(), bestMatch.getKey());
        }

        return mapping;
    }

    private static double compareTopics(Map.Entry<Integer, String[]> topic1, Map.Entry<Integer, String[]> topic2) {
        int matchingWords = 0;
        for ( String word1 : topic1.getValue()) {
            for (String word2 : topic2.getValue()) {
                if (word1.equals(word2)) matchingWords++;
            }
        }
        return matchingWords;
    }


}
