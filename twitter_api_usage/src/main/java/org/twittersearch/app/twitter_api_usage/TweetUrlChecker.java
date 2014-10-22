package org.twittersearch.app.twitter_api_usage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 18.08.2014.
 */

// Not working because of Twitter's Rate Limit
public class TweetUrlChecker {

    public static void main(String[] args) {
        collectAndWriteMissingUrlsToDB("2014-07-24");
    }

    public static void collectAndWriteMissingUrlsToDB(String date) {
        DBManager dbManager = new DBManager();
        System.out.println("Collect missing Urls.");
        Map<Long, List<String>> tweetsUrls = collectMissingUrls(dbManager, date);
        System.out.println("Write missing Urls to DB.");
        writeMissingUrlsToDB(tweetsUrls, dbManager);
    }

    private static Map<Long, List<String>> collectMissingUrls(DBManager dbManager, String date) {
        TwitterManager twitterManager = new TwitterManager();
        Map<Long, List<String>> tweetsUrls = new HashMap<Long, List<String>>();

        int counter = 0;
        Map<Long,String> tweets = dbManager.selectTweetContentsCreatedAt(date);
        System.out.println("Have to collect " + tweets.size() + " Tweets.");
        for(Long tweetId : tweets.keySet()) {
            System.out.println(counter);
            List<String> urls = twitterManager.getUrlsForTweet(tweetId);
            tweetsUrls.put(tweetId,urls);

            counter++;
        }

        return tweetsUrls;
    }

    private static void writeMissingUrlsToDB(Map<Long, List<String>> tweetsUrls, DBManager dbManager) {
        int counter = 0;
        for(Map.Entry<Long, List<String>> tweetUrls : tweetsUrls.entrySet()) {
            for(String url : tweetUrls.getValue()) {
                dbManager.writeUrlContentToDB(tweetUrls.getKey(), url, false);
            }

            if ((counter % 100) == 0) {
                System.out.println(counter + "Tweets are done.");
            }
            counter++;
        }
    }
}
