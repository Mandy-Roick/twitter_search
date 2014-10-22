package org.twittersearch.app.search_engine;

import com.google.gson.*;
import org.twittersearch.app.twitter_api_usage.DBManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Created by Mandy Roick on 04.10.2014.
 */
public class TweetsToJSONConverter {

    public static void main(String[] args) {
        String date = "2014-10-04";
        DBManager dbManager = new DBManager();
        Map<Long,String> tweetsFromDate = dbManager.selectTweetContentsCreatedAt(date);
        writeTweetsToJSONFile(tweetsFromDate, date + "_tweets.json");

    }

    public static void writeTweetsToJSONFile(Map<Long,String> tweets, String fileName) {
        try {
            Writer jsonWriter = new FileWriter(fileName);

            JsonObject tweetsAsJson = new JsonObject();
            JsonObject tweetObject;

            //Stop This Try To Convert From MalletInputFile to JSON-Object, because all tweets from one day do not fit in Memory if JSON.

            for (Map.Entry<Long,String> tweet : tweets.entrySet()) {
                tweetObject = new JsonObject();
                tweetObject.addProperty("id", tweet.getKey());
                tweetObject.addProperty("content", tweet.getValue());
                //tweetsAsJson.
                //tweetsAsJson.add(tweetObject);
            }

        } catch (IOException e) {

            System.out.println("Could not open Json File for writing.");
            e.printStackTrace();
        }

    }

    public static String convertTweetToJSON(Long id, String content) {
        JsonObject tweetObject = new JsonObject();
        tweetObject.addProperty("id", id);
        tweetObject.addProperty("content", content);

        return tweetObject.toString();
    }

}
