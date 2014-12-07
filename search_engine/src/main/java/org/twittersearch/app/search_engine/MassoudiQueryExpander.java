package org.twittersearch.app.search_engine;

import org.twittersearch.app.topic_modelling.TweetPreprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 02.12.2014.
 */
public class MassoudiQueryExpander {

    public static void main(String[] args) {
        int k = 10;
        String date = "2014-10-20";
        String[] expandedQuery;

//        if (args.length == 1 ) {
//            expandedQuery = expand(args[1], date, k, 50);
//        } else {
//            expandedQuery = expand("politics", date, k, 50);
//        }

//        for (String queryElement : expandedQuery) {
//            System.out.print(queryElement + " ");
//        }
    }

    public static String expand(String query, String date, int k, int cutOffFrequency) {
        String fileName = "massoudi_" + query + "_" + date + ".results";
        File massoudiExpandedQuery = new File(fileName);
        if (massoudiExpandedQuery.exists()) {
            return readMassoudiExpandedQueryFile(fileName);
        }

        String[] processedQuery = processQuery(query);
        Set<String> stopWords = readStopWords("stop_lists/stop_words_mysql.txt");
        List<MassoudiExpansionTerm> termsOccurringWithQuery = searchForExpansionTerms(processedQuery, date, stopWords);

        Set<String> expandedQuerySet = new HashSet<String>();
        for (String queryTerm : processedQuery) {
            expandedQuerySet.add(queryTerm);
        }

        for (MassoudiExpansionTerm term : termsOccurringWithQuery) {
            if (expandedQuerySet.size() >= k) break;
            if (term.getOverallFrequency() > cutOffFrequency) {
                expandedQuerySet.add(term.getTerm());
            }
        }

        String expandedQuery = "";
        for (String queryTerm : expandedQuerySet) {
            expandedQuery += queryTerm + " ";
        }

        return expandedQuery;
    }

    private static Set<String> readStopWords(String fileName) {
        Set<String> result = new HashSet<String>();
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not read stop words.");
            e.printStackTrace();
        }
        return result;
    }

    private static String readMassoudiExpandedQueryFile(String fileName) {
        String inputLine;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            inputLine = br.readLine();
            return inputLine;
        } catch (java.io.IOException e) {
            System.out.println("Could not read MassoudiExpandedQuery.");
            e.printStackTrace();
        }
        return "";
    }

    private static List<MassoudiExpansionTerm> searchForExpansionTerms(String[] query, String date, Set<String> stopWords) {
        ElasticSearchManager esManager = new ElasticSearchManager();

        //esManager.addToIndex(date);

        List<String> tweetsContainingQueryTerm = esManager.getTweetsContainingQueryTerm(query, date);
        Map<String, MassoudiExpansionTerm> terms = new HashMap<String, MassoudiExpansionTerm>();
        Set<String> currentCooccurringTerms;
        for (String tweet : tweetsContainingQueryTerm) {
            String[] tweetWords = processQuery(tweet);
            //String[] tweetWords = splitString(tweet);
            // this creation of a set first is needed to not count co-occurrences double if the word appears more than once in the tweet.
            currentCooccurringTerms = new HashSet<String>();
            for (String tweetWord : tweetWords) {
                currentCooccurringTerms.add(tweetWord);
            }
            for (String cooccurringTerm : currentCooccurringTerms) {
                if (stopWords.contains(cooccurringTerm)) continue;

                MassoudiExpansionTerm term = terms.get(cooccurringTerm);
                if (term == null) {
                    term = new MassoudiExpansionTerm(cooccurringTerm);
                    terms.put(cooccurringTerm,term);
                } else {
                    term.increaseCooccurrenceCount();
                }
            }
        }

        long numberOfDocuments = esManager.numberOfDocuments(date);
        for (MassoudiExpansionTerm term : terms.values()) {
            long documentFrequency = esManager.documentFrequency(term.getTerm(), date);
            term.setOverallFrequency(documentFrequency);
            term.setScore(calculateMassoudiScore(term,numberOfDocuments));
        }

        List<MassoudiExpansionTerm> sortedTerms = new ArrayList<MassoudiExpansionTerm>(terms.values());
        Collections.sort(sortedTerms);
        return sortedTerms;
    }

    private static String[] processQuery(String query) {
        String preprocessedQuery = preprocessQuery(query);
        String[] splitQuery = splitString(preprocessedQuery);
        return splitQuery;
    }

    private static String preprocessQuery(String query) {
        String normalizedQuery = TweetPreprocessor.normalizeTweetContent(query);
        String normalizedLowerCaseQuery = normalizedQuery.toLowerCase();
        return normalizedLowerCaseQuery;
    }

    private static String[] splitString(String preprocessedQuery) {
        //Pattern splitPattern = Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}");
        Pattern splitPattern = Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}]+\\p{L}");
        Matcher splitMatcher = splitPattern.matcher(preprocessedQuery);
        List<String> splitQuery = new ArrayList<String>();

        while(splitMatcher.find()) {
            splitQuery.add(splitMatcher.group());
        }

        return splitQuery.toArray(new String[splitQuery.size()]);
    }

    private static double calculateMassoudiScore(MassoudiExpansionTerm term, long numberOfDocuments) {
        double score = term.getCooccurrenceCount() * Math.log((numberOfDocuments/(double) term.getOverallFrequency()));
        return score;
    }
}
