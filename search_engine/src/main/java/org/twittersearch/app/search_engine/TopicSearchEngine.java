package org.twittersearch.app.search_engine;

import org.twittersearch.app.topic_modelling.TopicModelBuilder;
import org.twittersearch.app.twitter_api_usage.TwitterManager;
import twitter4j.TwitterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        String[][] expandedQuery = expandQuery(query);
        searchForTweets(expandedQuery);
    }

    public static String[][] expandQuery(String query) {
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
        String[][] expandedQuery = QueryExpander.expand(query, 5, 3, filePrefix);

        return expandedQuery;
    }

    public static String[][] expandQueryForGivenDate(String query, String date) {

        //String filePrefix = TopicModelBuilder.learnTopicModel(calendarOfYesterday);
        String filePrefix = "trimmed_tm-200_" + date;
        String[][] expandedQuery = QueryExpander.expand(query, 5, 3, filePrefix);

        return expandedQuery;
    }

    private static void searchForTweets(String[][] expandedQuery) {
        TwitterManager twitterManager = new TwitterManager();
        for (String[] topicQuery : expandedQuery) {
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

    private static void searchForTweetsViaES(String[][] expandedQuery) {
        ElasticSearchManager esManager = new ElasticSearchManager();
        for (String[] topicQuery : expandedQuery) {
            String twitterQuery = "";
            for (String queryElement : topicQuery) {
                twitterQuery += queryElement + " ";
            }
            System.out.println("--------------------" + twitterQuery + "-------------------------------");
            esManager.searchFor(twitterQuery);
        }
    }

    public static Calendar getDateOfYesterday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, -1); // subtracting 1 day
        return c;
    }
}
