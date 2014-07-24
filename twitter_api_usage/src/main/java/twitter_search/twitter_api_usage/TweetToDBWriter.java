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
        service.shutdown();
        this.urlFutureTextsForTweets.put(tweet, urlTexts);
    }

    public void collectUrlsAndCloseDB() {
        System.out.println("");
        System.out.println("------------------------------------------------------------");
        System.out.println(this.urlFutureTextsForTweets.size() + " Tweets with URLs have to be analyzed.");

        Map<Status, Map<String, Future<String>>> unfinishedUrlTextsForTweets = writeUrlsToDBWhichAreDone();

//        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : unfinishedUrlTextsForTweets.entrySet()) {
//            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
//                writeURLContentToDB(urlFutureTextsForTweet.getKey(), urlEntry.getKey(), urlEntry.getValue());
//            }
//        }
        //System.out.println("Missing URLs collected, but " + exceptionCounter + " URLs could not be visited.");
        try {
            this.dbManager.finalize();
            System.out.println("DBManager finalized.");
        } catch (Throwable throwable) {
            System.out.println("Could not finalize DBManager.");
            throwable.printStackTrace();
        }

        this.urlFutureTextsForTweets = unfinishedUrlTextsForTweets;
        this.dbManager = new DBManager();
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
    }

    private Map<Status, Map<String, Future<String>>> writeUrlsToDBWhichAreDone() {
        int exceptionCounter = 0;
        int doneCounter = 0;
        Map<Status, Map<String,Future<String>>> unfinishedUrlTextsForTweets = new HashMap<Status, Map<String, Future<String>>>();

        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : this.urlFutureTextsForTweets.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                if (urlEntry.getValue().isDone()) {
                    boolean noException = this.writeURLContentToDB(urlFutureTextsForTweet.getKey(), urlEntry.getKey(), urlEntry.getValue());
                    doneCounter++;
                    if (!noException) exceptionCounter++;
                } else {
                    Status currentTweet = urlFutureTextsForTweet.getKey();
                    if (unfinishedUrlTextsForTweets.containsKey(currentTweet)) {
                        unfinishedUrlTextsForTweets.get(currentTweet).put(urlEntry.getKey(), urlEntry.getValue());
                    } else {
                        Map<String, Future<String>> unfinishedUrlEntryMap = new HashMap<String, Future<String>>();
                        unfinishedUrlEntryMap.put(urlEntry.getKey(), urlEntry.getValue());
                        unfinishedUrlTextsForTweets.put(currentTweet, unfinishedUrlEntryMap);
                    }
                }
            }
        }

        System.out.println(doneCounter + " URLs collected, " + unfinishedUrlTextsForTweets.size() + "URLs still to go.");
        System.out.println("Missing URLs collected, but " + exceptionCounter + " URLs could not be visited.");
        return unfinishedUrlTextsForTweets;
    }

    private boolean writeURLContentToDB(Status tweet, String url, Future<String> urlContent) {
        boolean noException = true;
        try {
            this.dbManager.writeUrlContentToDB(tweet, url, urlContent.get());
        } catch (InterruptedException e) {
            noException = false;
            System.out.println("Url Thread for url " + url + " was interrupted.");
            System.out.println(e.toString());
            //e.printStackTrace();
        } catch (ExecutionException e) {
            noException = false;
            System.out.println("Url Thread for url " + url + " could not be executed.");
            System.out.println(e.toString());
            //e.printStackTrace();
        }
        return noException;
    }

    protected void finalize() throws Throwable {
        collectUrlsAndCloseDB();

        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : this.urlFutureTextsForTweets.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                this.writeURLContentToDB(urlFutureTextsForTweet.getKey(), urlEntry.getKey(), urlEntry.getValue());
            }
        }

        super.finalize();
    }

}
