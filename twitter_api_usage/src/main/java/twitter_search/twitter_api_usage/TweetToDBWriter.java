package twitter_search.twitter_api_usage;

import com.intellij.util.Url;
import twitter4j.Status;
import twitter4j.URLEntity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kleiner Klotz on 21.07.2014.
 */
public class TweetToDBWriter {
    DBManager dbManager;
    Map<Status, Map<String, Future<String>>> urlFutureTextsForTweets; // {tweet : {url1 : urlContent1, url2 : urlContent2,...},...}

    TweetToDBWriter() {
        this.dbManager = new DBManager();
        this.urlFutureTextsForTweets = new HashMap<Status, Map<String,Future<String>>>();
    }

    public void writeTweetToDB (Status tweet) {
        this.dbManager.writeTweetToDB(tweet);
        this.dbManager.writeHashtagsToDB(tweet);

        writeUrlsToDB(tweet);
    }

    public void writeUrlsToDB (Status tweet) {
        URLEntity[] urlEntities = tweet.getURLEntities();
        if (urlEntities.length == 0) return;
        final ExecutorService service = Executors.newFixedThreadPool(urlEntities.length);
        Map<String, Future<String>> urlTexts = new HashMap<String, Future<String>>();
        for(URLEntity urlEntity : urlEntities) {
            final Future<String> urlText = service.submit(new UrlCollector(urlEntity.getURL()));
            urlTexts.put(urlEntity.getURL(), urlText);
        }

        this.urlFutureTextsForTweets.put(tweet, urlTexts);
    }

    public void destroy() {
        System.out.println("Still " + this.urlFutureTextsForTweets.size() + " Tweets with URLs have to be analyzed.");
        int counter = 0;
        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : this.urlFutureTextsForTweets.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                try {
                    this.dbManager.writeUrlContentToDB(urlFutureTextsForTweet.getKey(), urlEntry.getKey(), urlEntry.getValue().get());
                } catch (InterruptedException e) {
                    System.out.println("Url Thread for url " + urlEntry.getKey() + " was interrupted.");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    System.out.println("Url Thread for url " + urlEntry.getKey() + " could not be executed.");
                    e.printStackTrace();
                }
            }
        }
        try {
            this.dbManager.finalize();
            System.out.println("Done :)");
        } catch (Throwable throwable) {
            System.out.println("Could not finalize DBManager.");
            throwable.printStackTrace();
        }
    }
}
