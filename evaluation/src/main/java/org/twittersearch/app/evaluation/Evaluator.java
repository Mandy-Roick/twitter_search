package org.twittersearch.app.evaluation;

import org.twittersearch.app.search_engine.ElasticSearchManager;
import org.twittersearch.app.search_engine.TopicSearchEngine;
import org.twittersearch.app.twitter_api_usage.DBManager;
import org.twittersearch.app.twitter_api_usage.TweetObject;

import java.util.*;

/**
 * Created by Mandy Roick.
 */
public class Evaluator
{

    public static void main( String[] args ) {
        String query = "politics";
        int numberOfSampledTweets = 10000;

        ElasticSearchManager esManager = new ElasticSearchManager();
        //esManager.addToIndex("2014-10-21");
        Evaluator evaluator = new Evaluator();
        Set<TweetObject> sampledTweets = evaluator.sampleTweets("2014-10-21", query, numberOfSampledTweets);
        List<String> sampledTweetsIndices = new ArrayList<String>();
        for (TweetObject sampledTweet : sampledTweets) {
            sampledTweetsIndices.add(String.valueOf(sampledTweet.getId()));
        }
        esManager.searchForInSample(query, sampledTweetsIndices);

        System.out.println("------------------------------------------------");

        String[][] expandedQuery = TopicSearchEngine.expandQueryForGivenDate(query, "2014-10-20");
        List<String> relevantTweets = TopicSearchEngine.searchForTweetsViaESInSample(expandedQuery, esManager, sampledTweetsIndices);

        int numberOfFoundTweets = relevantTweets.size();
        int numberOfMatchingTweets = calculateNumberOfExpertTweets(query, relevantTweets);
        System.out.println(numberOfMatchingTweets + " have flag " + query + " " + numberOfFoundTweets + " have not.");

    }

    private static int calculateNumberOfExpertTweets(String query, List<String> relevantTweets) {
        int numberOfMatchingTweets = 0;
        for (String relevantTweet : relevantTweets) {
            if (relevantTweet.contains("evaluation_flag=" + query)) {
                numberOfMatchingTweets++;
            }
            System.out.println(relevantTweet);
        }
        return numberOfMatchingTweets;
    }

    DBManager dbManager;

    public Evaluator() {
        this.dbManager = new DBManager();
    }

    public Set<TweetObject> sampleTweets(String date, String evaluationFlag, int numOfTweets) {
        Set<TweetObject> sampledTweets = new HashSet<TweetObject>();

        List<TweetObject> tweetsWithEvaluationFlag = dbManager.selectTweetsCreatedAtWithEvaluationFlag(date, evaluationFlag);
        System.out.println(tweetsWithEvaluationFlag.size() + " tweets have the flag " + evaluationFlag + ".");
        List<TweetObject> tweetsFromDate = dbManager.selectTweetsCreatedAt(date);

        Random random = new Random();
        // sample randomly and add to set which contains already the tweets with evaluation flag
        sampledTweets = sampleRandomly(tweetsFromDate, new HashSet<TweetObject>(tweetsWithEvaluationFlag), numOfTweets, random);

        return sampledTweets;
    }

    // Trial and Error sampling
    public static <T> Set<T> sampleRandomly(List<T> items, int sizeOfSample, Random random) {
        Set<T> sample = new HashSet<T>();
        while (sample.size() < sizeOfSample) {
            int randomPosition = random.nextInt(items.size());
            sample.add(items.get(randomPosition));
        }
        return sample;
    }

    // Trial and Error sampling
    public static <T> Set<T> sampleRandomly(List<T> items, Set<T> startingSet, int sizeOfSample, Random random) {
        while (startingSet.size() < sizeOfSample) {
            int randomPosition = random.nextInt(items.size());
            startingSet.add(items.get(randomPosition));
        }
        return startingSet;
    }

	public static double recall() {
		return 0;
		
	}

}
