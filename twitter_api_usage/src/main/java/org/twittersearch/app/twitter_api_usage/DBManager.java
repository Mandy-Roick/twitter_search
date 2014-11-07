package org.twittersearch.app.twitter_api_usage;

import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by Mandy Roick on 14.07.2014. according to example by Maximilian Jenders
 */
public class DBManager {
    private Connection connection;

    private String password = "";
    private String user = "";

    public DBManager() {
        connect();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(
                            //"jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/max?searchpath=mandy_masterarbeit",
                            "jdbc:postgresql://seschat.hpi.uni-potsdam.de:5432/roick?searchpath=mandy_masterarbeit",
                            this.user, this.password);
        } catch (SQLException e) {
            System.out.println("Could not connect to PostgreSQL-Database.");
            e.printStackTrace();
        }
    }

    protected void finalize() throws Throwable {
        this.connection.close();
        super.finalize();
    }

    public void writeTweetToDB(Status tweet) {
        //if (tweetDoesExist(tweet.getId())) {
        //    updateTweet(tweet);
        //} else {
        //    createTweet(tweet);
        //}
        createTweet(tweet);
    }

    public void writeTweetForEvalToDB(Status tweet, String topic) {
        if (tweetDoesExist(tweet.getId())) {
            updateTweetForEval(tweet, topic);
        } else {
            createTweetForEval(tweet, topic);
        }
    }

    private boolean tweetDoesExist(long tweetId) {
        boolean doesExist = false;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM mandy_masterarbeit.twitter_tweet WHERE id = " + tweetId);

            if(result.next()) {
                doesExist = true;
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not test whether Tweet exists in DB!");
            e.printStackTrace();
        }

        return doesExist;
    }

    private void updateTweet(Status tweet) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "UPDATE mandy_masterarbeit.twitter_tweet " +
                    "SET content = ?, " +
                    "user_id = ?, " +
                    "created_at = ?, " +
                    "reply_id = ?, " +
                    "language = ?, " +
                    "retweeted = ?, " +
                    "retweeted_id = ?, " +
                    "truncated = ?, " +
                    "coordinates_x = ?, coordinates_y = ?, " +
                    "place_id = ? " +
                    "WHERE id = ?");

            statement.setString(1, tweet.getText());
            statement.setLong(2, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(3, sqlDate);
            statement.setLong(4, tweet.getInReplyToStatusId());
            statement.setString(5, tweet.getLang());

            statement.setBoolean(6, tweet.isRetweet());
            if (tweet.isRetweet()) {
                statement.setLong(7, tweet.getRetweetedStatus().getId());
            } else {
                statement.setNull(7, Types.BIGINT);
            }
            statement.setBoolean(8, tweet.isTruncated());
            if (tweet.getGeoLocation() != null) {
                statement.setDouble(9, tweet.getGeoLocation().getLongitude());
                statement.setDouble(10, tweet.getGeoLocation().getLatitude());
            } else {
                statement.setNull(9, Types.DOUBLE);
                statement.setNull(10, Types.DOUBLE);
            }

            if (tweet.getPlace() == null) {
                statement.setNull(11, Types.VARCHAR);
            } else {
                statement.setString(11, tweet.getPlace().getId());
            }


            statement.setLong(12, tweet.getId());

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTweetForEval(Status tweet, String topic) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "UPDATE mandy_masterarbeit.twitter_tweet " +
                    "SET evaluation_flag = ? " +
                    "WHERE id = ?");

            statement.setString(1, topic);
            statement.setLong(2, tweet.getId());

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTweet(Status tweet) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet(id, content, user_id, created_at, " +
                    "reply_id, language, retweeted, retweeted_id, truncated, place_id, coordinates_x, coordinates_y)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?)");

            statement.setLong(1, tweet.getId());
            statement.setString(2, tweet.getText());
            statement.setLong(3, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(4, sqlDate);
            statement.setLong(5, tweet.getInReplyToStatusId());
            statement.setString(6, tweet.getLang());

            statement.setBoolean(7, tweet.isRetweet());
            if (tweet.isRetweet()) {
                statement.setLong(8, tweet.getRetweetedStatus().getId());
            } else {
                statement.setNull(8, Types.BIGINT);
            }
            statement.setBoolean(9, tweet.isTruncated());
            if (tweet.getPlace() == null) {
                statement.setNull(10, Types.VARCHAR);
            } else {
                statement.setString(10, tweet.getPlace().getId());
            }
            if (tweet.getGeoLocation() != null) {
                statement.setDouble(11, tweet.getGeoLocation().getLongitude());
                statement.setDouble(12, tweet.getGeoLocation().getLatitude());
            } else {
                statement.setNull(11, Types.DOUBLE);
                statement.setNull(12, Types.DOUBLE);
            }

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Tweet could not be saved to DB for Tweet " + tweet.getText());
            e.printStackTrace();
        }
    }

    private void createTweetForEval(Status tweet, String topic) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet(id, content, user_id, created_at, reply_id, " +
                    "language, retweeted, retweeted_id, truncated, place_id, coordinates_x, coordinates_y, evaluation_flag)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?)");

            statement.setLong(1, tweet.getId());
            statement.setString(2, tweet.getText());
            statement.setLong(3, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(4, sqlDate);
            statement.setLong(5, tweet.getInReplyToStatusId());
            statement.setString(6, tweet.getLang());

            statement.setBoolean(7, tweet.isRetweet());
            if (tweet.isRetweet()) {
                statement.setLong(8, tweet.getRetweetedStatus().getId());
            } else {
                statement.setNull(8, Types.BIGINT);
            }
            statement.setBoolean(9, tweet.isTruncated());
            if (tweet.getPlace() == null) {
                statement.setNull(10, Types.VARCHAR);
            } else {
                statement.setString(10, tweet.getPlace().getId());
            }
            if (tweet.getGeoLocation() != null) {
                statement.setDouble(11, tweet.getGeoLocation().getLongitude());
                statement.setDouble(12, tweet.getGeoLocation().getLatitude());
            } else {
                statement.setNull(11, Types.DOUBLE);
                statement.setNull(12, Types.DOUBLE);
            }

            statement.setString(13, topic);

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Tweet could not be saved to DB for Tweet " + tweet.getText());
            e.printStackTrace();
        }
    }

    //---------------------------------    Hashtags    ------------------------------------//

    public void writeHashtagsToDB(Status tweet) {
        HashtagEntity[] hashtags = tweet.getHashtagEntities();
        for (HashtagEntity hashtag : hashtags) {
            //if (!hashtagForTweetDoesExist(tweet.getId(), hashtag.getText())) {
                createHashtagForTweet(tweet, hashtag);
            //}
        }
    }

    private boolean hashtagForTweetDoesExist(long tweetId, String hashtagText) {
        boolean doesExist = false;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM mandy_masterarbeit.twitter_tweet_hashtag WHERE (hashtag = '" +
                    hashtagText + "') AND (tweet = " + tweetId + ")");

            if(result.next()) {
                doesExist = true;
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not test whether hashtag and tweet does exist in DB");
            e.printStackTrace();
        }

        return doesExist;
    }

    private void createHashtagForTweet(Status tweet, HashtagEntity hashtag) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet_hashtag(hashtag, tweet)" +
                    "VALUES (?, ?)");
            statement.setString(1, hashtag.getText());
            statement.setLong(2, tweet.getId());
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Hashtags could not be saved to DB for Hashtag " + hashtag.getText());
            e.printStackTrace();
        }
    }

    //---------------------------------    URLs      ------------------------------------//

    public void writeUrlContentToDB(long tweetId, String url, boolean hasText) {
        //if(!urlForTweetDoesExist(tweetId, url)) {
            createUrlForTweet(tweetId, url, hasText);
        //}
    }

    public void writeUrlContentToDB(long tweetId, String url, boolean hasText, String urlContent) {
        if(!urlForTweetDoesExist(tweetId, url)) {
            createUrlForTweet(tweetId, url, hasText, urlContent);
        } else {
            updateUrlForTweet(tweetId, url, hasText, urlContent);
        }
    }

    private boolean urlForTweetDoesExist(long tweetId, String url) {
        boolean doesExist = false;
        try {
            Statement statement = this.connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM mandy_masterarbeit.twitter_tweet_url WHERE (url = '" +
                    url + "') AND (tweet = " + tweetId + ")");

            if(result.next()) {
                doesExist = true;
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not test whether url and tweet does exist in DB");
            e.printStackTrace();
        }

        return doesExist;
    }

    private void updateUrlForTweet(long tweetId, String url, boolean hasText, String urlContent) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "UPDATE mandy_masterarbeit.twitter_tweet_url " +
                    "SET has_text = ?, " +
                    "content = ? " +
                    "WHERE (url = ?) AND (tweet = ?)");

            statement.setBoolean(1, hasText);
            statement.setString(2, urlContent);
            statement.setString(3, url);
            statement.setLong(4, tweetId);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUrlForTweet(long tweetId, String url, boolean hasText) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet_url(url, has_text, tweet)" +
                    "VALUES (?, ?, ?)");
            statement.setString(1, url);
            statement.setBoolean(2, hasText);
            statement.setLong(3, tweetId);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("UrlContent could not be saved to DB for URL " + url);
            System.out.println(e.toString());
            //e.printStackTrace();
        }
    }

    private void createUrlForTweet(long tweetId, String url, boolean hasText, String urlContent) {
        try {
            PreparedStatement statement = this.connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet_url(url, has_text, content, tweet)" +
                    "VALUES (?, ?, ?, ?)");
            statement.setString(1, url);
            statement.setBoolean(2, hasText);
            statement.setString(3, urlContent);
            statement.setLong(4, tweetId);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("UrlContent could not be saved to DB for URL " + url);
            System.out.println(e.toString());
            //e.printStackTrace();
        }
    }

    //---------------------------------    SELECT     ------------------------------------//

    public Map<Long,String> selectTweetContentsCreatedAt(String date) {
        Map<Long, String> tweetIdToContent = new HashMap<Long, String>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id, content FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE created_at = '" + date + "'");

            while(result.next()) {
                long tweetId = result.getLong(1);
                String tweetContent = result.getString(2);
                tweetIdToContent.put(tweetId, tweetContent);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are created at: " + date + "!");
            e.printStackTrace();
        }
        return tweetIdToContent;
    }

    public Map<Long, TweetObject> selectTweetsCreatedAt(String date) {
        Map<Long, TweetObject> tweets = new HashMap<Long, TweetObject>(); // ArrayList because users of this function need random access for sampling
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id, content, evaluation_flag FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE created_at = '" + date + "'");

            TweetObject currentTweet;
            while(result.next()) {
                long tweetId = result.getLong(1);
                String tweetContent = result.getString(2);
                String evaluation_flag = result.getString(3);
                currentTweet = new TweetObject(tweetId, tweetContent, evaluation_flag, date);
                tweets.put(tweetId, currentTweet);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are created at: " + date + "!");
            e.printStackTrace();
        }
        return tweets;
    }

    public String[] selectHashtagsForTweet(Long tweetId) {
        List<String> hashtags = new LinkedList<String>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT hashtag FROM mandy_masterarbeit.twitter_tweet_hashtag " +
                    "WHERE tweet = '" + tweetId + "'");

            while(result.next()) {
                hashtags.add(result.getString(1));
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select hashtags from DB for tweet: " + tweetId + "!");
            e.printStackTrace();
        }
        return hashtags.toArray(new String[hashtags.size()]);
    }

    public Map<Long, List<String>> selectTweetsAndHashtagsCreatedAt(String date) {
        Map<Long, List<String>> tweetsHashtags = new HashMap<Long, List<String>>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT tweet,hashtag FROM " +
                    "(SELECT id FROM mandy_masterarbeit.twitter_tweet WHERE created_at = '" + date + "') tweet " +
                    "INNER JOIN mandy_masterarbeit.twitter_tweet_hashtag hashtag ON tweet.id = hashtag.tweet");

            while(result.next()) {
                Long tweetId = result.getLong(1);
                List<String> hashtags = tweetsHashtags.get(tweetId);
                if(hashtags == null) {
                    hashtags = new LinkedList<String>();
                }
                hashtags.add(result.getString(2));
                tweetsHashtags.put(tweetId, hashtags);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select hashtags and tweets from DB for tweet!");
            e.printStackTrace();
        }
        return tweetsHashtags;
    }

    public Set<Long> selectTweetsWithUrlContentCreatedAt(String date) {
        Set<Long> tweetsWithUrlContents = new HashSet<Long>();

        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT tweet FROM " +
                    "((SELECT id FROM mandy_masterarbeit.twitter_tweet WHERE created_at = '" + date + "') tweet " +
                    "INNER JOIN mandy_masterarbeit.twitter_tweet_url url ON tweet.id = url.tweet)" +
                    "WHERE (has_text = 't')");

            while(result.next()) {
                tweetsWithUrlContents.add(result.getLong(1));
            }
        } catch (SQLException e) {
            System.out.println("Could not select url contents and tweets from DB for date " + date + "!");
            e.printStackTrace();
        }

        return tweetsWithUrlContents;
    }

    public List<String> selectUrlContentsForTweet(Long tweetId) {
        List<String> urlContents = new LinkedList<String>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT content FROM mandy_masterarbeit.twitter_tweet_url " +
                    "WHERE (tweet = " + tweetId + ") AND (has_text = 't')");

            while (result.next()) {
                urlContents.add(result.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Could not select url contents for tweet id " + tweetId + "!");
            e.printStackTrace();
        }

        return urlContents;
    }

    public Map<Long, List<String>> selectTweetsUrlsWithoutText() {
        Map<Long, List<String>> tweetsUrls = new HashMap<Long, List<String>>();

        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT url, tweet FROM mandy_masterarbeit.twitter_tweet_url " +
                                                      "WHERE (has_text = 'f')");

            while(result.next()) {
                Long tweetId = result.getLong(2);
                List<String> urls = tweetsUrls.get(tweetId);
                if(urls == null) {
                    urls = new LinkedList<String>();
                }
                String url = result.getString(1);
                urls.add(url);
                tweetsUrls.put(tweetId, urls);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select urls without text!");
            e.printStackTrace();
        }

        return tweetsUrls;
    }

    public Map<Long, List<String>> selectTweetsUrlsWithoutTextForDate(String date) {
        Map<Long, List<String>> tweetsUrls = new HashMap<Long, List<String>>();

        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT url, tweet FROM " +
                    "((SELECT id FROM mandy_masterarbeit.twitter_tweet WHERE created_at = '" + date + "') tweet " +
                    "INNER JOIN mandy_masterarbeit.twitter_tweet_url url ON tweet.id = url.tweet)" +
                    "WHERE (has_text = 'f')");

            while(result.next()) {
                Long tweetId = result.getLong(2);
                List<String> urls = tweetsUrls.get(tweetId);
                if(urls == null) {
                    urls = new LinkedList<String>();
                }
                String url = result.getString(1);
                urls.add(url);
                tweetsUrls.put(tweetId, urls);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select urls without text!");
            e.printStackTrace();
        }

        return tweetsUrls;
    }

    public List<CrawlUrl> selectUrlsWithoutTextForDate(String date) {
        List<CrawlUrl> urls = new ArrayList<CrawlUrl>();

        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT url, tweet FROM " +
                    "((SELECT id FROM mandy_masterarbeit.twitter_tweet WHERE created_at = '" + date + "') tweet " +
                    "INNER JOIN mandy_masterarbeit.twitter_tweet_url url ON tweet.id = url.tweet)" +
                    "WHERE (has_text = 'f')");

            while(result.next()) {
                urls.add(new CrawlUrl(result.getString(1), result.getLong(2)));
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select urls without text!");
            e.printStackTrace();
        }

        return urls;
    }

    public Long selectHighestIdFromDate(String date) {
        Long highestId = 1L;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("Select MAX(id) from mandy_masterarbeit.twitter_tweet Where created_at = '" + date + "'");

            if(result.next()) {
                highestId = result.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return highestId;
    }

    public Map<Long,String> selectTweetsAboveID(Long startingID) {
        Map<Long, String> tweetIdToContent = new HashMap<Long, String>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id, content FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE id > " + startingID + "");

            while(result.next()) {
                long tweetId = result.getLong(1);
                String tweetContent = result.getString(2);
                tweetIdToContent.put(tweetId, tweetContent);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are above id: " + startingID + "!");
            e.printStackTrace();
        }
        return tweetIdToContent;
    }

    public Long selectHighestIdFromUser(Long userId) {
        Long highestId = 1L;
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("Select MAX(id) from mandy_masterarbeit.twitter_tweet Where user_id = '" + userId + "'");

            if(result.next()) {
                highestId = result.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return highestId;
    }

    public Map<Long,  List<String>> selectTweetsHashtagsAboveID(Long startingID) {
        Map<Long,  List<String>> tweetsHashtags = new HashMap<Long,  List<String>>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT tweet,hashtag FROM " +
                    "mandy_masterarbeit.twitter_tweet_hashtag WHERE tweet > " + startingID + "");

            while(result.next()) {
                Long tweetId = result.getLong(1);
                List<String> hashtags = tweetsHashtags.get(tweetId);
                if(hashtags == null) {
                    hashtags = new LinkedList<String>();
                }
                hashtags.add(result.getString(2));
                tweetsHashtags.put(tweetId, hashtags);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are above id: " + startingID + "!");
            e.printStackTrace();
        }
        return tweetsHashtags;
    }

    // select tweets from day from expertusers for topic x
    // use tweetObject as return value to include urls etc
    public List<TweetObject> selectTweetsCreatedAtWithEvaluationFlag(String date, String evaluationFlag) {
        List<TweetObject> tweets = new LinkedList<TweetObject>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id, content, evaluation_flag FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE (created_at = '" + date + "') AND (evaluation_flag = '" + evaluationFlag + "')");

            TweetObject currentTweet;
            while(result.next()) {
                long tweetId = result.getLong(1);
                String tweetContent = result.getString(2);
                String evaluation_flag = result.getString(3);
                currentTweet = new TweetObject(tweetId, tweetContent, evaluation_flag, date);
                tweets.add(currentTweet);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are created at: " + date + "!");
            e.printStackTrace();
        }
        return tweets;
    }

    public List<TweetObject> selectTweetsCreatedAtWithEvaluationFlagNotNull(String date) {
        List<TweetObject> tweets = new LinkedList<TweetObject>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT id, content, evaluation_flag FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE (created_at = '" + date + "') AND NOT (evaluation_flag = 'null')");

            TweetObject currentTweet;
            while(result.next()) {
                long tweetId = result.getLong(1);
                String tweetContent = result.getString(2);
                String evaluation_flag = result.getString(3);
                currentTweet = new TweetObject(tweetId, tweetContent, evaluation_flag, date);
                tweets.add(currentTweet);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are created at: " + date + "!");
            e.printStackTrace();
        }
        return tweets;
    }

    public Map<String, Integer> selectCountOfEvaluationFlags(String date) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT evaluation_flag, Count(*) FROM mandy_masterarbeit.twitter_tweet " +
                    "WHERE (created_at = '" + date + "') AND NOT (evaluation_flag = 'null') GROUP BY evaluation_flag");

            while(result.next()) {
                String evaluationFlag = result.getString(1);
                Integer count = result.getInt(2);
                counts.put(evaluationFlag, count);
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println("Could not select tweets from DB which are created at: " + date + "!");
            e.printStackTrace();
        }
        return counts;
    }
}
