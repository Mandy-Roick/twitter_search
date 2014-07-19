package twitter_search.twitter_api_usage;

import twitter4j.Status;

import java.sql.*;

/**
 * Created by Mandy Roick on 14.07.2014.
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
                            "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/max?searchpath=mandy_masterarbeit",
                            this.user, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void finalize() throws Throwable {
        this.connection.close();
        super.finalize();
    }

    public void writeTweetToDB(Status tweet) {
        if (tweetExists(tweet.getId())) {
            updateTweet(tweet);
        } else {
            createTweet(tweet);
        }
    }

    private void createTweet(Status tweet) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet(id, content, user_id, created_at, " +
                    "reply_id, language, place_id, retweeted; retweeted_id, truncated, coordinates)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, pointfromtext('POINT(' || ? || ' ' || ? || ')',2029))");
                    // This last magic number is a SRID. TODO: replace by real SRID from Twitter.

            statement.setLong(1, tweet.getId());
            statement.setString(2, tweet.getText());
            statement.setLong(3, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(4, sqlDate);
            statement.setLong(5, tweet.getInReplyToStatusId());
            statement.setNString(6, tweet.getLang());
            if (tweet.getPlace() == null) {
                statement.setString(7, null);
            } else {
                statement.setString(7, tweet.getPlace().getId());
            }
            statement.setBoolean(8, tweet.isRetweet());
            if (tweet.isRetweet()) {
                statement.setLong(9, tweet.getRetweetedStatus().getId());
            }
            statement.setBoolean(10, tweet.isTruncated());
            if (tweet.getGeoLocation() != null) {
                statement.setDouble(11, tweet.getGeoLocation().getLatitude());
                statement.setDouble(12, tweet.getGeoLocation().getLongitude());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTweet(Status tweet) {
    }

    private boolean tweetExists(long id) {
        return false;
    }
}
