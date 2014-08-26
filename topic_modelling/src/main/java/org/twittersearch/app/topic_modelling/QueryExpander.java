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

    public static void main(String[] args) {
        String[] expandedQuery;
        if (args.length == 1 ) {
            expandedQuery = expand(args[1]);
        } else {
            expandedQuery = expand("Warum #Neuland ein schönes Meme, aber eigentlich gar nicht lustig ist http://s.gilly.cat/12bQ6sj");
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

        Map<String, String[]> typeTopicCounts = readTopicModel("");
        
        return postprocessedQuery;
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
        return null;
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
