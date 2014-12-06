package org.twittersearch.app.evaluation;

import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.commons.lang.ArrayUtils;
import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.search_engine.ElasticSearchManager;
import org.twittersearch.app.search_engine.MassoudiQueryExpander;
import org.twittersearch.app.search_engine.SearchAnswer;
import org.twittersearch.app.search_engine.TopicSearchEngine;
import org.twittersearch.app.helper.TopicContainer;
import org.twittersearch.app.topic_modelling.TopicModelBuilder;
import org.twittersearch.app.twitter_api_usage.DBManager;
import org.twittersearch.app.twitter_api_usage.TweetObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Mandy Roick.
 */
public class Evaluator
{

    public static void main( String[] args ) {
        int numberOfSampledTweets = 1000;

        Evaluator evaluator = new Evaluator();

        String[] queries = {"politics", "ukraine", "ebola", "sports", "economy", "basketball", "baseball", "marketing", "music", "hip hop"};
        boolean samplingNotOnlyExperts = true;
        boolean positionNotPrecision = true;

        for (String query : queries) {
            evaluator.evaluateTopicBasedSearch(query, numberOfSampledTweets, samplingNotOnlyExperts);
        }
        //evaluator.evaulateTopicModel(query);
    }

    private static int calculateNumberOfExpertTweets(String query, List<SearchAnswer> answers) {
        int numberOfMatchingTweets = 0;
        for (SearchAnswer answer : answers) {
            if (answer.getEvaluationFlag().equals(query)) {
                numberOfMatchingTweets++;
            }
            //System.out.println(relevantTweet);
        }
        return numberOfMatchingTweets;
    }

    private static List<Integer> calculatePositionsOfExpertTweets(String query, List<SearchAnswer> answers) {
        List<Integer> positions = new ArrayList<Integer>();
        for (SearchAnswer answer : answers) {
            String currentEvaluationFlag = answer.getEvaluationFlag();
            if (currentEvaluationFlag != null && currentEvaluationFlag.equals(query)) {
                positions.add(answer.getPosition());
            }
            //System.out.println(relevantTweet);
        }
        Collections.sort(positions);
        return positions;
    }

    private DBManager dbManager;
    private ElasticSearchManager esManager;

    public Evaluator() {
        this.dbManager = new DBManager();
        this.esManager = new ElasticSearchManager();
    }

    public void evaluateTopicBasedSearch(String query, int numberOfSampledTweets, boolean samplingNotOnlyExperts) {
        String date = "2014-10-21";
        String dateTM = "2014-10-20";
        // 1. Sample Tweets
        Collection<TweetObject> sampledTweets;
        if (samplingNotOnlyExperts) {
            sampledTweets = sampleTweets(date, query, numberOfSampledTweets);
        } else {
            sampledTweets = dbManager.selectTweetsCreatedAtWithEvaluationFlagNotNull(date);
        }
        List<String> sampledTweetsIndices = new ArrayList<String>();
        for (TweetObject sampledTweet : sampledTweets) {
            sampledTweetsIndices.add(String.valueOf(sampledTweet.getId()));
        }

        // 2. Search for sample via Elastic Search
        System.out.println(query);
        String filePrefix = "answers\\evaluation_" + query + "_" + sampledTweets.size();

        // 2.1. Baseline simple query search
        List<SearchAnswer> baseLineAnswers = TopicSearchEngine.searchForTweetsViaESInSample(query, this.esManager, sampledTweetsIndices);
        EvaluationResult baseLineEvaluation = evaluateResult(query, baseLineAnswers, sampledTweets.size(), date, samplingNotOnlyExperts);
        System.out.println(baseLineEvaluation);
        printAnswers("baseline", baseLineAnswers, query, sampledTweets.size());

        // 2.2. Massoudi algorithm as competitor
        String massoudisExpandedQuery = MassoudiQueryExpander.expand(query, dateTM, 10, 50);
        printMassoudiExpandedQuery(query, date, massoudisExpandedQuery);
        System.out.println("Massoudi Expanded Query: " + massoudisExpandedQuery);
        List<SearchAnswer> massoudiAnswers = TopicSearchEngine.searchForTweetsViaESInSample(massoudisExpandedQuery, this.esManager, sampledTweetsIndices);
        EvaluationResult massoudiEvaluation = evaluateResult(query, massoudiAnswers, sampledTweets.size(), date, samplingNotOnlyExperts);
        System.out.println(massoudiEvaluation);
        printAnswers("massoudi", massoudiAnswers, query, sampledTweets.size());

        String[][] expandedQuery = TopicSearchEngine.expandQueryForGivenDate(query, dateTM);
        List<SearchAnswer> topicBasedAnswers = TopicSearchEngine.searchForTweetsViaESInSample(expandedQuery, this.esManager, sampledTweetsIndices);
        EvaluationResult topicBasedEvaluation = evaluateResult(query, topicBasedAnswers, sampledTweets.size(), date, samplingNotOnlyExperts);
        System.out.println(topicBasedEvaluation);
        printAnswers("topic-based", topicBasedAnswers, query, sampledTweets.size());

        printEvaluations(filePrefix, baseLineEvaluation, massoudiEvaluation, topicBasedEvaluation);

        //data which would be good to write to files:
        // - THE QUERY EXPANSION FROM MASSOUDI because it is always the same
        // - found tweets probably one file for each algorithm massoudi_answers_query -> first line expansion then results?, how many tweets found?, ...
        // - positions of relevant tweets for each algorithm for each query
        //For random additional results use sampleRandomly with starting set
        //TODO: print evaluations to file; Add MAP to Evaluation (print also the positions of relevant tweets)
    }

    private void printEvaluations(String filePrefix, EvaluationResult baseLineEvaluation, EvaluationResult massoudiEvaluation, EvaluationResult topicBasedEvaluation) {
        try {
            PrintWriter evaluationWriter = new PrintWriter(filePrefix + ".results");
            evaluationWriter.println("baseline, " + baseLineEvaluation);
            evaluationWriter.println("massoudi, " + massoudiEvaluation);
            evaluationWriter.println("topic-based, " + topicBasedEvaluation);
            evaluationWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not write evaluation results.");
            e.printStackTrace();
        }
    }

    private void printAnswers(String algorithm, List<SearchAnswer> answers, String query, int sampleSize) {
        try {
            PrintWriter answerWriter = new PrintWriter("answers\\" + algorithm + "_" + query + "_" + sampleSize + ".results");
            for (SearchAnswer answer : answers) {
                answerWriter.println(answer);
            }
            answerWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not write answers for algorithm: " + algorithm);
            e.printStackTrace();
        }
    }

    private void printMassoudiExpandedQuery(String query, String date, String massoudisExpandedQuery) {
        try {
            PrintWriter massoudiExpandedQueryWriter = new PrintWriter("massoudi_" + query + "_" + date + ".results");
            massoudiExpandedQueryWriter.println(massoudisExpandedQuery);
            massoudiExpandedQueryWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not write massoudi expanded query");
            e.printStackTrace();
        }
    }

    private EvaluationResult evaluateResultFMeasure(String query, List<SearchAnswer> answers, int numberOfAllTweets, String date) {
        Map<String, Integer> evaluationFlagCounts = dbManager.selectCountOfEvaluationFlags(date);
        int realPositives = evaluationFlagCounts.get(query);
        int TP = calculateNumberOfExpertTweets(query, answers);
        int FP = answers.size() - TP;
        int FN = realPositives - TP;
        int TN = numberOfAllTweets - TP - FP - FN;

        return new EvaluationResult(TP, TN, FP, FN);
    }

    private EvaluationResult evaluateResultPositions(String query, List<SearchAnswer> answers, int numberOfAllTweets, String date) {
        Map<String, Integer> evaluationFlagCounts = dbManager.selectCountOfEvaluationFlags(date);
        List<Integer> positions = calculatePositionsOfExpertTweets(query, answers);

        return new EvaluationResult(positions);
    }

    private EvaluationResult evaluateResult(String query, List<SearchAnswer> answers, int numberOfAllTweets, String date, boolean withoutFmeasure) {
        Map<String, Integer> evaluationFlagCounts = dbManager.selectCountOfEvaluationFlags(date);

        List<Integer> positions = calculatePositionsOfExpertTweets(query, answers);
        if (withoutFmeasure) {
            return new EvaluationResult(positions);
        } else {
            int realPositives = evaluationFlagCounts.get(query);
            int TP = calculateNumberOfExpertTweets(query, answers);
            int FP = answers.size() - TP;
            int FN = realPositives - TP;
            int TN = numberOfAllTweets - TP - FP - FN;
            return new EvaluationResult(TP, TN, FP, FN, positions);
        }
    }

    public double evaulateTopicModel(String query) {

        // 1. Find relevant tweets and topic index for those
        String date = "2014-10-21";
        String dayBefore = "2014-10-20";
        String filePrefix = "trimmed_tm-" + 200 + "_" + dayBefore;
        //esManager.addToIndex(date);

        Map<Integer, TopicContainer> topics = FileReaderHelper.readTopWords(filePrefix);
        List<TopicContainer> expandedQuery = TopicSearchEngine.expandQueryForGivenDateWithTopicIndices(query, dayBefore);
        // TODO: return only the content of tweets
        Map<TopicContainer, List<String>> relevantTweets = TopicSearchEngine.searchForTweetsViaES(expandedQuery, date, this.esManager, 3);

        // 2. calculateTopicInferencing for relevant tweets
        for (Map.Entry<TopicContainer, List<String>> topicTweets : relevantTweets.entrySet()) {
            double[][] sampledDistributions = calculateTopicInferencing(filePrefix, topicTweets.getValue());
            int topicId = topicTweets.getKey().getId();
            System.out.println(topicTweets.getKey());
            for (double[] sampledDistribution : sampledDistributions) {
                System.out.print(sampledDistribution[topicId]);
                List<Double> sampledList = Arrays.asList(ArrayUtils.toObject(sampledDistribution));
                Double max = Collections.max(sampledList);

                System.out.print(" max: " + max + "maxTopic: " + topics.get(sampledList.indexOf(max)) + "\n");
            }
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
}
