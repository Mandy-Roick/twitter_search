package org.twittersearch.app.search_engine;

import org.twittersearch.app.topic_modelling.TweetPreprocessor;

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

        if (args.length == 1 ) {
            expandedQuery = expand(args[1], date, k, 50);
        } else {
            expandedQuery = expand("politics", date, k, 50);
        }

        for (String queryElement : expandedQuery) {
            System.out.print(queryElement + " ");
        }
    }

    public static String[] expand(String query, String date, int k, int cutOffFrequency) {
        String[] processedQuery = processQuery(query);

        List<MassoudiExpansionTerm> termsOccurringWithQuery = searchForExpansionTerms(processedQuery, date);

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

        return expandedQuerySet.toArray(new String[k]);
    }

    private static List<MassoudiExpansionTerm> searchForExpansionTerms(String[] query, String date) {
        ElasticSearchManager esManager = new ElasticSearchManager();

        //esManager.addToIndex(date);

        List<String> tweetsContainingQueryTerm = esManager.getTweetsContainingQueryTerm(query, date);
        Map<String, MassoudiExpansionTerm> terms = new HashMap<String, MassoudiExpansionTerm>();
        Set<String> currentCooccurringTerms;
        for (String tweet : tweetsContainingQueryTerm) {
            String[] tweetWords = processQuery(tweet);
            //String[] tweetWords = splitString(tweet);
            currentCooccurringTerms = new HashSet<String>();
            for (String tweetWord : tweetWords) {
                currentCooccurringTerms.add(tweetWord);
            }
            for (String cooccurringTerm : currentCooccurringTerms) {
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
