package twitter_search.twitter_api_usage;

import twitter4j.Status;

import java.sql.*;

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
        if (tweetDoesExist(tweet.getId())) {
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

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTweet(Status tweet) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "UPDATE mandy_masterarbeit.twitter_tweet" +
                    "SET content = ?, " +
                    "user_id = ?, " +
                    "created_at = ?, " +
                    "reply_id = ?, " +
                    "language = ?, " +
                    "place_id = ?, " +
                    "retweeted = ?, " +
                    "retweeted_id = ?, " +
                    "truncated = ?, " +
                    "coordinates = pointfromtext('POINT(' || ? || ' ' || ? || ')',2029), " +
                    "WHERE id = ?");

            statement.setString(1, tweet.getText());
            statement.setLong(2, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(3, sqlDate);
            statement.setLong(4, tweet.getInReplyToStatusId());
            statement.setNString(5, tweet.getLang());
            if (tweet.getPlace() == null) {
                statement.setString(6, null);
            } else {
                statement.setString(6, tweet.getPlace().getId());
            }
            statement.setBoolean(7, tweet.isRetweet());
            if (tweet.isRetweet()) {
                statement.setLong(8, tweet.getRetweetedStatus().getId());
            }
            statement.setBoolean(9, tweet.isTruncated());
            if (tweet.getGeoLocation() != null) {
                statement.setDouble(10, tweet.getGeoLocation().getLatitude());
                statement.setDouble(11, tweet.getGeoLocation().getLongitude());
            }
            statement.setLong(12, tweet.getId());

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }

        return doesExist;
    }
}
