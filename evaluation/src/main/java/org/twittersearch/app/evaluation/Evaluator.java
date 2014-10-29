package org.twittersearch.app.evaluation;

import org.twittersearch.app.twitter_api_usage.DBManager;
import org.twittersearch.app.twitter_api_usage.TweetObject;

import java.util.*;

/**
 * Created by Mandy Roick.
 */
public class Evaluator
{

    public static void main( String[] args ) {
        Evaluator evaluator = new Evaluator();
        Set<TweetObject> sampledTweets = evaluator.sampleTweets("2014-10-20", "politics", 1000);
        for (TweetObject tweet : sampledTweets) {
            System.out.println(tweet);
        }
    }

    DBManager dbManager;

    public Evaluator() {
        this.dbManager = new DBManager();
    }

    public Set<TweetObject> sampleTweets(String date, String evaluationFlag, int numOfTweets) {
        Set<TweetObject> sampledTweets = new HashSet<TweetObject>();

        List<TweetObject> tweetsWithEvaluationFlag = dbManager.selectTweetsCreatedAtWithEvaluationFlag(date, evaluationFlag);

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
