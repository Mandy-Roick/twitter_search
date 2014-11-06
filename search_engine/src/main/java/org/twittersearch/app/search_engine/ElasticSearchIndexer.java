package org.twittersearch.app.search_engine;

import au.com.bytecode.opencsv.CSVReader;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.twittersearch.app.twitter_api_usage.DBManager;
import org.twittersearch.app.twitter_api_usage.TweetObject;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mandy Roick on 06.10.2014.
 */
public class ElasticSearchIndexer {

    public static void indexFromInputFile(String date, Client client) {
        String inputFileName = "mallet_input_file_" + date + ".csv";

        try {
            CSVReader inputFileReader = new CSVReader(new FileReader(inputFileName), '\t', '\"');

            String[] nextLine;
            String json;
            IndexResponse indexResponse;
            int counter = 0;
            while ((nextLine = inputFileReader.readNext()) != null) {
                json = TweetsToJSONConverter.convertTweetToJSON(Long.parseLong(nextLine[0]), nextLine[2]);
                indexResponse = client.prepareIndex("twitter", "tweet", nextLine[0]).setSource(json).execute().actionGet();
                if(indexResponse.isCreated()) {
                    if((counter % 1000) == 0) {
                        System.out.println(counter + ": " + indexResponse.getId());
                    }
                    counter++;
                }
                //ToDo: evaluate indexResponse (.isCreated() = false means it was updated, is this interesting for us?)
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not read mallet input file.");
            e.printStackTrace();
        }


    }

    public static void indexFromDB(String date, Client client) {
        DBManager dbManager = new DBManager();
        Map<Long, TweetObject> tweets = dbManager.selectTweetsCreatedAt(date);
        System.out.println(tweets.size() + " tweets have to be indexed.");

        String json;
        IndexResponse indexResponse;
        int counter = 0;
        for (TweetObject tweet : tweets.values()) {
            json = tweet.toJson();
            indexResponse = client.prepareIndex("twitter", "tweet", tweet.getId().toString()).setSource(json).execute().actionGet();
            //if(indexResponse.isCreated()) {
                if((counter % 10000) == 0) {
                    System.out.println(counter + ": " + indexResponse.getId());
                }
                counter++;
            //}
        }

    }

    public static void indexFromDBWithUrls(String date, Client client) {
        DBManager dbManager = new DBManager();
        Map<Long, TweetObject> tweets = dbManager.selectTweetsCreatedAt(date);
        Set<Long> tweetsWithUrlContents = dbManager.selectTweetsWithUrlContentCreatedAt(date);
        ElasticSearchManager esManager = new ElasticSearchManager();
        Long[] tweetsWhichNeedUrlContent = esManager.tweetsInESWithoutUrlContent(tweetsWithUrlContents);

        System.out.println(tweetsWhichNeedUrlContent.length + " tweets which need url content.");

        String json;
        IndexResponse indexResponse;
        int counter = 0;
        TweetObject tweet;
        for (Long tweetId : tweetsWhichNeedUrlContent) {
            tweet = tweets.get(tweetId);
            tweets.remove(tweetId);

            if (tweet != null) {
                List<String> urlContents = dbManager.selectUrlContentsForTweet(tweetId);
                for (String urlContent : urlContents) {
                    tweet.addUrlContent(cleanUrlContent(urlContent));
                }
                //tweet.addUrlContents(urlContents);
            }

            json = tweet.toJson();
            indexResponse = client.prepareIndex("twitter", "tweet", Long.toString(tweetId)).setSource(json).execute().actionGet();
            //if(indexResponse.isCreated()) {
                if((counter % 100) == 0) {
                    System.out.println(counter + ": " + indexResponse.getId());
                }
                counter++;
            //}
        }

    }

    private static String cleanUrlContent(String urlContent) {
        String cleanContent = "";
        Document jsoupDocument = Jsoup.parse(urlContent);
        cleanContent += jsoupDocument.title();
        cleanContent += " " + jsoupDocument.text();
        return cleanContent;
    }
}
