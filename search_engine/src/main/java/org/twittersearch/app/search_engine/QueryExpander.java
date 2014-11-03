package org.twittersearch.app.search_engine;

import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.helper.TypeContainer;
import org.twittersearch.app.topic_modelling.TweetPreprocessor;

import java.io.FileInputStream;
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
        double topicPercentageThreshold = 0.05; //10%
        int numOfTopWordsPerTopic = 3;
        String date = "2014-10-21";
        String filePrefix = "trimmed_tm-200_" + date + "_wo_seeding";
        String[][] expandedQuery;
        if (args.length == 1 ) {
            expandedQuery = expand(args[1], topicPercentageThreshold, numOfTopWordsPerTopic, filePrefix);
        } else {
            expandedQuery = expand("politics", topicPercentageThreshold, numOfTopWordsPerTopic, filePrefix);
        }

        for (String[] queryTopicElement : expandedQuery) {
            for (String queryElement : queryTopicElement) {
                System.out.println(queryElement);
            }
            System.out.println("");
        }
    }

    // TODO: test with different topicPercentageThresholds - 10% is for sports probably to big
    public static String[][] expand(String query, double topicPercentageThreshold, int numOfTopWordsPerTopic, String filePrefix) {

        // Process Query
        String preprocessedQuery = preprocessQuery(query);
        String[] splitQuery = splitQuery(preprocessedQuery);
        String[] postprocessedQuery = postprocessQuery(splitQuery);

        Map<String, TypeContainer> types = FileReaderHelper.readTypes(filePrefix + "_type_topic_counts.results");
        Map<Integer, String[]> topWords = FileReaderHelper.readTopWords(filePrefix + "_top_words.results");

        String[][] expandedQuery = expandThroughTopicModel(postprocessedQuery, types, topWords, topicPercentageThreshold, numOfTopWordsPerTopic);

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

    private static String[][] expandThroughTopicModel(String[] query, Map<String, TypeContainer> types, Map<Integer,
                                                    String[]> topWords, double topicPercentageThreshold, int numOfTopWordsPerTopic) {
        List<String[]> expandedQuery = new ArrayList<String[]>();

        for (String queryElement : query) {
            //ToDo: add the query itself somehow, because only now it is normalized
            //expandedQuery.add(queryElement);

            TypeContainer typeContainer = types.get(queryElement);
            if (typeContainer != null) {

                Integer[] topicIndices = typeContainer.getBestTopics(topicPercentageThreshold);
                String[] topWordsForTopic;
                for (Integer topicIndex : topicIndices) {
                    String[] expandedTopicQuery = new String[numOfTopWordsPerTopic];
                    topWordsForTopic = topWords.get(topicIndex);
                    for (int j = 0; (j < topWordsForTopic.length) && (j < numOfTopWordsPerTopic); j++) {
                        expandedTopicQuery[j] = topWordsForTopic[j];
                    }
                    expandedQuery.add(expandedTopicQuery);
                }
            }
        }
        return expandedQuery.toArray(new String[expandedQuery.size()][]);
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
