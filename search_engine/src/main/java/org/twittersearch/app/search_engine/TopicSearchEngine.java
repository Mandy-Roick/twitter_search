package org.twittersearch.app.search_engine;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.twittersearch.app.helper.TopicContainer;
import org.twittersearch.app.topic_modelling.TopicModelBuilder;
import org.twittersearch.app.twitter_api_usage.TwitterManager;
import twitter4j.TwitterException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Mandy Roick on 10.10.2014.
 */
public class TopicSearchEngine {

    public static void main(String[] args) {
        String query = "politics";
        if (args.length == 1) {
            query = args[1];
        }
        //String[][] expandedQuery = expandQueryForGivenDate(query, "2014-10-08");
        //searchForTweetsViaES(expandedQuery);

        Map<Double, String[]> expandedQuery = expandQueryForGivenDate(query, "2014-12-06");
        searchForTweets(expandedQuery);
    }

    public static Map<Double, String[]> expandQuery(String query) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendarOfYesterday = Calendar.getInstance();
        calendarOfYesterday.setTime(new Date()); // Now use today date.
        calendarOfYesterday.add(Calendar.DATE, -1); // subtracting 1 day
        String date = sdf.format(calendarOfYesterday.getTime());

        //TwitterManager tm = new TwitterManager();
        //String[] politics = new String[5];
        //try {
            //tm.searchFor("politics");
        //} catch (TwitterException e) {
            //e.printStackTrace();
        //}

        String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        Map<Double, String[]> expandedQuery = QueryExpander.expand(query, 0.05, 5, filePrefix);

        return expandedQuery;
    }

    public static Map<Double, String[]> expandQueryForGivenDate(String query, String date) {

        //String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        String filePrefix = "trimmed_tm-200_" + date;
        Map<Double, String[]> expandedQuery = QueryExpander.expand(query, 0.05, 3, filePrefix);

        return expandedQuery;
    }

    public static Map<Double, String[]> expandQueryForGivenDateWithFilePrefix(String query, String date, String filePrefix) {

        //String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        Map<Double, String[]> expandedQuery = QueryExpander.expand(query, 0.05, 10, filePrefix + date);

        return expandedQuery;
    }

    public static Map<Double, String[]> expandQueryForGivenDateWithFilePrefix(String query, String date, String filePrefix,
                                                                              double topicPercentageThreshold, int numOfTopWordsPerTopic) {

        //String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        Map<Double, String[]> expandedQuery = QueryExpander.expand(query, topicPercentageThreshold, numOfTopWordsPerTopic, filePrefix + date);

        return expandedQuery;
    }

    public static List<TopicContainer> expandQueryForGivenDateWithTopicIndices(String query, String date) {

        //String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        String filePrefix = "trimmed_tm-200_" + date;
        List<TopicContainer> expandedQuery = QueryExpander.expand(query, 0.05, filePrefix);

        return expandedQuery;
    }

    public static void searchForTweets(Map<Double, String[]> expandedQuery) {
        TwitterManager twitterManager = new TwitterManager();
        for (String[] topicQuery : expandedQuery.values()) {
            try {
                String twitterQuery = "";
                for (String queryElement : topicQuery) {
                    twitterQuery += queryElement + " ";
                }
                System.out.println("--------------------" + twitterQuery + "-------------------------------");
                twitterManager.searchFor(twitterQuery);
            } catch (TwitterException e) {
                System.out.println("Could not search for expanded Query in Twitter.");
                e.printStackTrace();
            }
        }
    }

    // TODO: write less duplicated Code (next method does nearly the same, except for esManager.searchForInSample()
    public static List<String> searchForTweetsViaES(String[][] expandedQuery, ElasticSearchManager esManager) {
        List<String> tweets = new ArrayList<String>();

        SearchHits searchHits;
        for (String[] topicQuery : expandedQuery) {
            String twitterQuery = "";
            for (String queryElement : topicQuery) {
                twitterQuery += queryElement + " ";
            }
            System.out.println("--------------------" + twitterQuery + "-------------------------------");
            searchHits = esManager.searchFor(twitterQuery);

            for (SearchHit searchHit : searchHits) {
                tweets.add(searchHit.getSource().toString());
                System.out.println(searchHit.getSource());
            }
        }

        return tweets;
    }

    public static Map<TopicContainer, List<String>> searchForTweetsViaES(List<TopicContainer> topicsForExpansion, String date,
                                                                         ElasticSearchManager esManager, int numberOfTopWords) {
        Map<TopicContainer, List<String>> relevantTweets = new HashMap<TopicContainer, List<String>>();

        SearchHits searchHits;
        for (TopicContainer topicForExpansion : topicsForExpansion) {
            List<String> currentTweets = new LinkedList<String>();

            String[] topicQuery = topicForExpansion.getTopWords(numberOfTopWords);

            String twitterQuery = "";
            for (String queryElement : topicQuery) {
                twitterQuery += queryElement + " ";
            }
            System.out.println("--------------------" + twitterQuery + "-------------------------------");
            searchHits = esManager.searchFor(twitterQuery, date);

            for (SearchHit searchHit : searchHits) {
                Map<String, Object> fields = searchHit.getSource();
                String tweetContent = fields.get("content").toString();
                currentTweets.add(tweetContent);
                System.out.println(tweetContent);
            }

            relevantTweets.put(topicForExpansion, currentTweets);
        }

        return relevantTweets;
    }

    public static List<SearchAnswer> searchForTweetsViaESInSample(Map<Double, String[]> expandedQuery, ElasticSearchManager esManager, List<String> sampledTweets) {
        Map<String, TopicBasedSearchAnswer> tweets = new HashMap<String,TopicBasedSearchAnswer>();

        SearchHits searchHits;
        for (Map.Entry<Double,String[]> topicQuery : expandedQuery.entrySet()) {
            String query = concatenateQuery(topicQuery.getValue());
            System.out.println("--------------------" + query + "-------------------------------");
            searchHits = esManager.searchForInSample(query, sampledTweets);

            for (SearchHit searchHit : searchHits) {
                String tweetId = searchHit.getId();
                double searchScore = searchHit.getScore();
                double topicScore = topicQuery.getKey();
                double score = 2*searchHit.getScore() + topicQuery.getKey();
                TopicBasedSearchAnswer answer = new TopicBasedSearchAnswer(tweetId, searchHit.getSource().toString(), searchHit.sourceAsMap(), score, query, searchScore, topicScore);
                if (tweets.containsKey(tweetId)) {
                    TopicBasedSearchAnswer duplicateAnswer = tweets.get(tweetId);
                    //if (duplicateAnswer.compareTo(answer) < 0) {
                    answer.updateScore(duplicateAnswer.getScore());
                    //}
                }
                tweets.put(searchHit.getId(),answer);
                //System.out.println(searchHit.getSource());
            }
        }

        List<SearchAnswer> answers = new ArrayList<SearchAnswer>(tweets.values());
        Collections.sort(answers);
        int rank = 1;
        for (SearchAnswer answer : answers) {
            answer.setFinalRank(rank);
            rank++;
        }

        return answers;
    }

    public static List<SearchAnswer> searchForTweetsViaESInSample(String query, ElasticSearchManager esManager, List<String> sampledTweets) {
        List<SearchAnswer> tweets = new ArrayList<SearchAnswer>();

        SearchHits searchHits;
        searchHits = esManager.searchForInSample(query, sampledTweets);

        int rank = 0;
        for (SearchHit searchHit : searchHits) {
            SearchAnswer answer = new SearchAnswer(searchHit.getId(), searchHit.getSource().toString(), searchHit.sourceAsMap(), rank);
            tweets.add(answer);
            rank++;
            //System.out.println(searchHit.getSource());
        }

        return tweets;
    }

    public static String concatenateQuery(String[] queryArray) {
        String query = "";
        for (String queryTerm : queryArray) {
            query += queryTerm + " ";
        }
        return query;
    }

    public static List<SearchAnswer> searchForTweetsViaESInSample(String[] expandedQuery, ElasticSearchManager esManager, List<String> sampledTweets) {
        String query = "";
        for (String queryTerm : expandedQuery) {
            query += queryTerm + " ";
        }

        return searchForTweetsViaESInSample(query, esManager, sampledTweets);
    }

    public static Calendar getDateOfYesterday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, -1); // subtracting 1 day
        return c;
    }
}
