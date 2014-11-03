package org.twittersearch.app.search_engine;

import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.helper.TypeContainer;
import org.twittersearch.app.topic_modelling.TopicContainer;
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
        List<TopicContainer> topicsForExpansion = expand(query, topicPercentageThreshold, filePrefix);
        List<String[]> expandedQuery = new ArrayList<String[]>();

        for (TopicContainer topicForExpansion : topicsForExpansion) {
            expandedQuery.add(topicForExpansion.getTopWords(numOfTopWordsPerTopic));
        }

        return expandedQuery.toArray(new String[expandedQuery.size()][]);
    }

    public static List<TopicContainer> expand(String query, double topicPercentageThreshold, String filePrefix) {
        String[] processedQuery = processQuery(query);

        Map<String, TypeContainer> types = FileReaderHelper.readTypes(filePrefix);
        Map<Integer, String[]> topWords = FileReaderHelper.readTopWords(filePrefix);

        List<TopicContainer> topicsForExpansion = expandThroughTopicModel(processedQuery, types, topWords, topicPercentageThreshold);
        Map<String, String> stemmingDictionary = FileReaderHelper.readStemmingDictionary(filePrefix);

        for (TopicContainer topicForExpansion : topicsForExpansion) {
            topicForExpansion.unstemmTopWords(stemmingDictionary);
        }

        return topicsForExpansion;
    }

    private static String[] processQuery(String query) {
        String preprocessedQuery = preprocessQuery(query);
        String[] splitQuery = splitQuery(preprocessedQuery);
        return postprocessQuery(splitQuery);
    }

    // Important to have a list as return value to hold the order
    private static List<TopicContainer> expandThroughTopicModel(String[] query, Map<String, TypeContainer> types, Map<Integer,String[]> topWords, double topicPercentageThreshold) {
        List<TopicContainer> topicsForExpansion = new ArrayList<TopicContainer>();

        for (String queryElement : query) {
            TypeContainer typeContainer = types.get(queryElement);
            if (typeContainer != null) {

                Integer[] topicIndices = typeContainer.getBestTopics(topicPercentageThreshold);
                for (Integer topicIndex : topicIndices) {
                    TopicContainer topicForExpansion = new TopicContainer(topicIndex, topWords.get(topicIndex));
                    topicsForExpansion.add(topicForExpansion);
                }
            }
        }
        return topicsForExpansion;
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
