package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 20.08.2014.
 */
public class QueryExpander {
    private static int numOfTopicsForExpansion = 3;
    private static int numOfTopWordsPerTopic = 3;

    public static void main(String[] args) {
        String[] expandedQuery;
        if (args.length == 1 ) {
            expandedQuery = expand(args[1]);
        } else {
            expandedQuery = expand("politics");
        }

        for (String queryElement : expandedQuery) {
            System.out.println(queryElement);
        }
    }

    public static String[] expand(String query) {
        // Process Query
        String preprocessedQuery = preprocessQuery(query);
        String[] splitQuery = splitQuery(preprocessedQuery);
        String[] postprocessedQuery = postprocessQuery(splitQuery);

        Map<String, String[]> typeTopicCounts = readTopicModel("trimmed_tm-50_2014-08-02_type_topic_counts.results");
        Map<Integer, String[]> topWords = readTopWords("trimmed_tm-50_2014-08-02_top_words.results");

        String[] expandedQuery = expandThroughTopicModel(postprocessedQuery, typeTopicCounts, topWords);

        return expandedQuery;
    }

    private static String[] expandThroughTopicModel(String[] query, Map<String, String[]> typeTopicCounts, Map<Integer, String[]> topWords) {
        List<String> expandedQuery = new ArrayList<String>();

        for (String queryElement : query) {
            expandedQuery.add(queryElement);

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
                        expandedQuery.add(topWordsForTopic[j]);
                    }
                    expandedQuery.add("");
                }

            }
        }
        return expandedQuery.toArray(new String[expandedQuery.size()]);
    }

    private static Map<Integer, String[]> readTopWords(String fileName) {
        Map<Integer, String[]> topWords = new HashMap<Integer, String[]>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ',', ' ');
            int topicIndex = 0;
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topWordsForTopic = Arrays.copyOfRange(nextLine, 3, nextLine.length);
                topWords.put(Integer.parseInt(nextLine[0]), topWordsForTopic);
                topicIndex++;
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return topWords;
    }

    private static Map<String, String[]> readTopicModel(String fileName) {
        Map<String,String[]> typeTopicCounts = new HashMap<String, String[]>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topicCounts = Arrays.copyOfRange(nextLine, 2, nextLine.length);
                typeTopicCounts.put(nextLine[1], topicCounts);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return typeTopicCounts;
    }

    private static String[] postprocessQuery(String[] splitQuery) {
        String[] postprocessedQuery = new String[splitQuery.length];

        for (int i = 0; i < splitQuery.length; i++) {
            postprocessedQuery[i] = StemmerPipe.stem(splitQuery[i]);
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
        String normalizedQuery = MalletInputFileCreator.normalizeTweetContent(query);
        return normalizedQuery;
    }
}
