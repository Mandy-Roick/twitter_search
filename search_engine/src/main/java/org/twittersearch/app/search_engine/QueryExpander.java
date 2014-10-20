package org.twittersearch.app.search_engine;

import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.topic_modelling.TweetPreprocessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twittersearch.app.topic_modelling.StemmerPipe.*;

/**
 * Created by Mandy Roick on 20.08.2014.
 */
public class QueryExpander {

    public static void main(String[] args) {
        int numOfTopicsForExpansion = 3;
        int numOfTopWordsPerTopic = 3;
        String date = "2014_10_15";
        String[][] expandedQuery;
        if (args.length == 1 ) {
            expandedQuery = expand(args[1], numOfTopicsForExpansion, numOfTopWordsPerTopic, date);
        } else {
            expandedQuery = expand("politics", numOfTopicsForExpansion, numOfTopWordsPerTopic, date);
        }

        for (String[] queryTopicElement : expandedQuery) {
            for (String queryElement : queryTopicElement) {
                System.out.println(queryElement);
            }
            System.out.println("");
        }
    }

    public static String[][] expand(String query, int numOfTopicsForExpansion, int numOfTopWordsPerTopic, String filePrefix) {

        // Process Query
        String preprocessedQuery = preprocessQuery(query);
        String[] splitQuery = splitQuery(preprocessedQuery);
        String[] postprocessedQuery = postprocessQuery(splitQuery);

        Map<String, String[]> typeTopicCounts = FileReaderHelper.readTopicModel(filePrefix + "_type_topic_counts.results");
        Map<Integer, String[]> topWords = FileReaderHelper.readTopWords(filePrefix + "_top_words.results");

        String[][] expandedQuery = expandThroughTopicModel(postprocessedQuery, typeTopicCounts, topWords, numOfTopicsForExpansion, numOfTopWordsPerTopic);

        try {
            FileInputStream fis = new FileInputStream(filePrefix + "_stemming_dictionary.results");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map<String, String> stemmingDictionary = (Map<String, String>) ois.readObject();

            String originalWord;
            for (int i = 0; i < expandedQuery.length; i++) {
                for (int j = 0; j < expandedQuery[i].length; j++) {
                    originalWord = stemmingDictionary.get(expandedQuery[i][j]);
                    if (originalWord != null) {
                        expandedQuery[i][j] = originalWord;
                    }
                }
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not read stemming dictionary from file.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Could not read stemming dictionary because of class incompatibilities.");
            e.printStackTrace();
        }


        return expandedQuery;
    }

    private static String[][] expandThroughTopicModel(String[] query, Map<String, String[]> typeTopicCounts, Map<Integer,
                                                    String[]> topWords, int numOfTopicsForExpansion, int numOfTopWordsPerTopic) {
        String[][] expandedQuery = new String[numOfTopicsForExpansion][numOfTopWordsPerTopic];

        for (String queryElement : query) {
            //ToDo: add the query itself somehow, because only now it is normalized
            //expandedQuery.add(queryElement);

            String[] topicCounts = typeTopicCounts.get(queryElement);
            if (topicCounts != null) {

                String topicCount;
                String[] topicIndexString;
                String[] topWordsForTopic;
                for (int i = 0; (i < topicCounts.length) && (i < numOfTopicsForExpansion); i++) {
                    topicCount = topicCounts[i];
                    topicIndexString = topicCount.split(":");
                    int topicIndex = Integer.parseInt(topicIndexString[0]);
                    topWordsForTopic = topWords.get(topicIndex);
                    for (int j = 0; (j < topWordsForTopic.length) && (j < numOfTopWordsPerTopic); j++) {
                        expandedQuery[i][j] = topWordsForTopic[j];
                    }
                }

            }
        }
        return expandedQuery;
    }

    private static String[] postprocessQuery(String[] splitQuery) {
        String[] postprocessedQuery = new String[splitQuery.length];

        for (int i = 0; i < splitQuery.length; i++) {
            postprocessedQuery[i] = stem(splitQuery[i]);
        }
        return postprocessedQuery;
    }

    private static String[] splitQuery(String preprocessedQuery) {
        Pattern splitPattern = Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}");
        Matcher splitMatcher = splitPattern.matcher(preprocessedQuery);
        List<String> splitQuery = new ArrayList<String>();

        while(splitMatcher.find()) {
            splitQuery.add(splitMatcher.group());
        }

        return splitQuery.toArray(new String[splitQuery.size()]);
    }

    private static String preprocessQuery(String query) {
        String normalizedQuery = TweetPreprocessor.normalizeTweetContent(query);
        String normalizedLowerCaseQuery = normalizedQuery.toLowerCase();
        return normalizedLowerCaseQuery;
    }
}
