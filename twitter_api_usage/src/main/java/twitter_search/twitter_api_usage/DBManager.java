package twitter_search.twitter_api_usage;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;

/**
 * Created by Mandy Roick on 14.07.2014. according to example by Maximilian Jenders
 */
public class DBManager {
    private Connection connection;

    private String password = "9lBZzYDpVDEGjhOPysab";
    private String user = "roick";

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

        writeHashtagsToDB(tweet);
        writeUrlContentToDB(tweet);
    }

    private void writeHashtagsToDB(Status tweet) {
        HashtagEntity[] hashtags = tweet.getHashtagEntities();
        for (HashtagEntity hashtag : hashtags) {
            if (!hashtagForTweetDoesExist(tweet.getId(), hashtag.getText())) {
                createHashtagForTweet(tweet, hashtag);
            }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void writeUrlContentToDB(Status tweet) {
        URLEntity[] urls = tweet.getURLEntities();
        URL url;
        for (URLEntity urlEntitiy : urls) {
            if(!urlForTweetDoesExist(tweet.getId(), urlEntitiy.getText())) {
                createUrlForTweet(tweet, urlEntitiy);
            }
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
            e.printStackTrace();
        }

        return doesExist;
    }

    private void createUrlForTweet(Status tweet, URLEntity urlEntitiy) {
        PreparedStatement statement = null;
        try {
            URL url = new URL(urlEntitiy.getURL());
            URLConnection urlConnection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String urlText = "";
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                urlText += inputLine;
            }
            br.close();
            statement = this.connection.prepareStatement("" +
                    "INSERT INTO mandy_masterarbeit.twitter_tweet_url(url, content, tweet)" +
                    "VALUES (?, ?, ?)");
            statement.setString(1, urlEntitiy.getText());
            statement.setString(2, urlText);
            statement.setLong(3, tweet.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
