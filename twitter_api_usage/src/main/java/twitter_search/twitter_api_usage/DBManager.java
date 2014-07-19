package twitter_search.twitter_api_usage;

import twitter4j.Status;

import java.sql.*;
import org.postgis.*;

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

        //TODO: add Hashtags and url content
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
                    "retweeted = ?, " +
                    "retweeted_id = ?, " +
                    "truncated = ?, " +
                    "coordinates_x = ?, coordinates_y = ? " +
                    "place_id = ?, " +
                    "WHERE id = ?");

            statement.setString(1, tweet.getText());
            statement.setLong(2, tweet.getUser().getId());
            java.sql.Date sqlDate = new Date(tweet.getCreatedAt().getTime());
            statement.setDate(3, sqlDate);
            statement.setLong(4, tweet.getInReplyToStatusId());
            statement.setNString(5, tweet.getLang());

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
