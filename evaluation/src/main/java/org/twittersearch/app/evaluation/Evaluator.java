package org.twittersearch.app.evaluation;

import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.search_engine.ElasticSearchManager;
import org.twittersearch.app.search_engine.TopicSearchEngine;
import org.twittersearch.app.topic_modelling.TopicContainer;
import org.twittersearch.app.topic_modelling.TopicModelBuilder;
import org.twittersearch.app.twitter_api_usage.DBManager;
import org.twittersearch.app.twitter_api_usage.TweetObject;

import java.io.IOException;
import java.util.*;

/**
 * Created by Mandy Roick.
 */
public class Evaluator
{

    public static void main( String[] args ) {
        String query = "baseball";
        int numberOfSampledTweets = 100000;

        //esManager.addToIndex("2014-10-10");
        Evaluator evaluator = new Evaluator();
        evaluator.evaluateTopicBasedSearch(query, numberOfSampledTweets);
    }

    private static int calculateNumberOfExpertTweets(String query, List<String> relevantTweets) {
        int numberOfMatchingTweets = 0;
        for (String relevantTweet : relevantTweets) {
            if (relevantTweet.contains("evaluation_flag=" + query)) {
                numberOfMatchingTweets++;
            }
            //System.out.println(relevantTweet);
        }
        return numberOfMatchingTweets;
    }

    private DBManager dbManager;
    private ElasticSearchManager esManager;

    public Evaluator() {
        this.dbManager = new DBManager();
        this.esManager = new ElasticSearchManager();
    }

    public void evaluateTopicBasedSearch(String query, int numberOfSampledTweets) {
        // 1. Sample Tweets
        Set<TweetObject> sampledTweets = sampleTweets("2014-10-21", query, numberOfSampledTweets);
        List<String> sampledTweetsIndices = new ArrayList<String>();
        for (TweetObject sampledTweet : sampledTweets) {
            sampledTweetsIndices.add(String.valueOf(sampledTweet.getId()));
        }

        // 2. Search for sample via Elastic Search
        List<String> relevantTweets1 = TopicSearchEngine.searchForTweetsViaESInSample(query, this.esManager, sampledTweetsIndices);
        int numberOfFoundTweets1 = relevantTweets1.size();
        int numberOfMatchingTweets1 = calculateNumberOfExpertTweets(query, relevantTweets1);
        System.out.println(numberOfMatchingTweets1 + " have flag " + query + " " + numberOfFoundTweets1 + " have not.");

        System.out.println("------------------------------------------------");

        String[][] expandedQuery = TopicSearchEngine.expandQueryForGivenDate(query, "2014-10-20");
        List<String> relevantTweets = TopicSearchEngine.searchForTweetsViaESInSample(expandedQuery, this.esManager, sampledTweetsIndices);

        int numberOfFoundTweets = relevantTweets.size();
        int numberOfMatchingTweets = calculateNumberOfExpertTweets(query, relevantTweets);
        System.out.println(numberOfMatchingTweets + " have flag " + query + " " + numberOfFoundTweets + " have not.");
    }

    public double evaulateTopicModel(String query) {

        // 1. Find relevant tweets and topic index for those
        String date = "2014-10-21";
        //esManager.addToIndex(date);
        List<TopicContainer> expandedQuery = TopicSearchEngine.expandQueryForGivenDateWithTopicIndices(query, "2014-10-20");
        // TODO: restrict the elastic search to one day
        Map<TopicContainer, List<String>> relevantTweets = TopicSearchEngine.searchForTweetsViaES(expandedQuery, this.esManager, 3);

        // 2. calculateTopicInferencing for relevant tweets
        String filePrefix = "trimmed_tm-" + 200 + "_" + date;
        for (Map.Entry<TopicContainer, List<String>> topicTweets : relevantTweets.entrySet()) {
            double[][] sampledDistributions = calculateTopicInferencing(filePrefix, topicTweets.getValue());
            // sampledDistribution auswerten --> trifft es das Topic aus TopicContainer
        }

        // 3. add up all results and give a final double value as evaluation

        return 0.0;
    }

    public double[][] calculateTopicInferencing(String filePrefix, List<String> relevantTweets) {
        List<double[]> topicDistributions = new LinkedList<double[]>();
        TopicInferencer topicInferencer = FileReaderHelper.readTopicInferencer(filePrefix);
        int numberOfIteration = 500;
        int thinning = 200;
        int burnin = 200;

        Iterator<Instance> inputIterator1 = new ArrayIterator(relevantTweets.toArray(new Object[relevantTweets.size()]));
        Iterator<Instance> inputIterator2 = new ArrayIterator(relevantTweets.toArray(new Object[relevantTweets.size()]));
        try {
            InstanceList tweetInstances = TopicModelBuilder.createInstanceList(inputIterator1, inputIterator2, filePrefix);

            for (Instance tweetInstance : tweetInstances) {
                double[] sampledDistribution = topicInferencer.getSampledDistribution(tweetInstance, numberOfIteration, thinning, burnin);
                System.out.println(sampledDistribution);
                topicDistributions.add(sampledDistribution);
            }

        } catch (IOException e) {
            System.out.println("Could not create instance list from relevant tweets.");
            e.printStackTrace();
        }

        return topicDistributions.toArray(new double[topicDistributions.size()][]);
    }

    public Set<TweetObject> sampleTweets(String date, String evaluationFlag, int numOfTweets) {
        Set<TweetObject> sampledTweets = new HashSet<TweetObject>();

        List<TweetObject> tweetsWithEvaluationFlag = dbManager.selectTweetsCreatedAtWithEvaluationFlag(date, evaluationFlag);
        System.out.println(tweetsWithEvaluationFlag.size() + " tweets have the flag " + evaluationFlag + ".");
        List<TweetObject> tweetsFromDate = new ArrayList<TweetObject>(dbManager.selectTweetsCreatedAt(date).values());

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
