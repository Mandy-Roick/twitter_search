package org.twittersearch.app.twitter_api_usage;

import au.com.bytecode.opencsv.CSVReader;
import com.sun.jna.platform.win32.Netapi32Util;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mandy Roick on 29.09.2014.
 */
public class UserCrawler {

    private TwitterApiCustodian twitterApiCustodian;
    private DBManager dbManager;

    public static void main(String[] args) {
        String userIdsFileName = "input_data/userIds_politics.csv";
        UserCrawler crawler = new UserCrawler();

        if (args.length == 1) {
            userIdsFileName = args[0];
        } else {
            System.out.println("First argument should be filename of csv containing user ids. Now using filename: " + userIdsFileName);
        }

        crawler.crawlUsers(userIdsFileName);

    }

    public void crawlUsers(String userIdsFileName) {
        try {
            CSVReader csvReader = new CSVReader(new FileReader(userIdsFileName), '\t', '\"', 1);
            List<String[]> users = csvReader.readAll();

            Long currentUserId;
            Long highestCrawledId;

            for (String[] user : users) {
                currentUserId = Long.parseLong(user[1]);
                highestCrawledId = dbManager.selectHighestIdFromUser(currentUserId);
                crawlAllUserTweets(currentUserId, highestCrawledId, user[2]);
            }

        } catch (IOException e) {
            System.out.println("Could not read CSV input file.");
            e.printStackTrace();
        }
    }

    public UserCrawler() {
        this.twitterApiCustodian = TwitterApiCustodian.getInstance();
        this.dbManager = new DBManager();
    }

    /***    According to example by Maximilian Jenders
     * Crawls all user tweets for a user. No tweets with an ID smaller (i.e., being tweeted before) the
     * greatesCrawledID will be crawled.
     * After crawling, the statistics for the user are updated
     * @param userID the userID
     * @param greatestCrawledID the latest tweetID
     */
    public void crawlAllUserTweets(long userID, long greatestCrawledID, String topic) {
        int page = 1;
        long maxID = 0L;
        ArrayList<Status> collectedStatuses = new ArrayList<Status>();
        ResponseList<Status> statuses;

        //page, count, sinceID
        Paging paging = new Paging(page, 200, greatestCrawledID);

        statuses = getStatuses(userID, paging);

        //account protected?
        if (statuses == null) return;

        for (Status s : statuses) {
            collectedStatuses.add(s);
            maxID = Math.max(maxID, s.getId());
        }

        while (statuses.size() > 1) {
            page++;
            paging = new Paging(page, 200, greatestCrawledID, maxID);
            statuses = getStatuses(userID, paging);

            //account protected?
            if (statuses == null) return;

            for (Status s : statuses) {
                collectedStatuses.add(s);
                maxID = Math.max(maxID, s.getId());
            }
        }

        for (Status status : collectedStatuses) {
            if (status.getLang().equals("en")) {
                this.dbManager.writeTweetForEvalToDB(status, topic);
            }
        }

        System.out.println("Done with User: " + userID + ", number of tweets collected: " + collectedStatuses.size());
    }

    /***
     * Makes a single API call to retieve user statuses
     * @param userID the userID
     * @param paging the paging used
     * @return
     */
    public ResponseList<Status> getStatuses(long userID, Paging paging) {
        ResponseList<Status> statuses = null;
        TwitterWrapper twitterWrapper = this.twitterApiCustodian.getFreeTwitterWrapper("/statuses/user_timeline");

        while (statuses == null) {
            try {
                statuses = twitterWrapper.getTwitter().getUserTimeline(userID, paging);
                twitterWrapper.logApiCall("/statuses/user_timeline");
                return statuses;
            } catch (TwitterException e) {
                twitterWrapper.logApiCall("/statuses/user_timeline");
                if (e.getStatusCode() == 401) {
                    //account protected
                    return null;
                }
                handleTwitterException(e);
            }
        }
        return statuses;
    }

    /***
     * Exception handling
     */
    private void handleTwitterException(TwitterException e) {
        System.out.println("Twitter Exception: " + e.getErrorCode() + " " + e.getMessage() + " (" + e.getStatusCode() + ").");
    }


}
