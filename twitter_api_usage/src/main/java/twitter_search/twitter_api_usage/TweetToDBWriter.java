package twitter_search.twitter_api_usage;

import twitter4j.Status;
import twitter4j.URLEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Mandy Roick on 21.07.2014.
 */
public class TweetToDBWriter {
    DBManager dbManager;
    ExecutorService executorService;
    Map<Status, Map<String, Future<String>>> urlFutureTextsForTweets; // {tweet : {url1 : urlContent1, url2 : urlContent2,...},...}
    Map<Status, Map<String, Future<String>>> unfinishedUrlFutureTexts;

    TweetToDBWriter() {
        this.dbManager = new DBManager();
        this.urlFutureTextsForTweets = new HashMap<Status, Map<String,Future<String>>>();
        this.unfinishedUrlFutureTexts = new HashMap<Status, Map<String,Future<String>>>();
        this.executorService = Executors.newFixedThreadPool(20);
    }

    TweetToDBWriter(int numberOfThreads) {
        this.dbManager = new DBManager();
        this.urlFutureTextsForTweets = new HashMap<Status, Map<String,Future<String>>>();
        this.unfinishedUrlFutureTexts = new HashMap<Status, Map<String,Future<String>>>();
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void writeTweetToDB (Status tweet) {
        this.dbManager.writeTweetToDB(tweet);
        this.dbManager.writeHashtagsToDB(tweet);

        writeUrlsToDB(tweet);
    }

    public void writeUrlsToDB (Status tweet) {
        URLEntity[] urlEntities = tweet.getURLEntities();
        if (urlEntities.length == 0) return;

        Map<String, Future<String>> urlTexts = new HashMap<String, Future<String>>();
        for(URLEntity urlEntity : urlEntities) {
            final Future<String> urlText = this.executorService.submit(new UrlCollector(urlEntity.getURL()));
            urlTexts.put(urlEntity.getURL(), urlText);
        }
        this.urlFutureTextsForTweets.put(tweet, urlTexts);
    }

    public void collectUrlsAndCloseDB() {
        System.out.println("");
        System.out.println("---------------------------  collect and cancel unfinished URLs  ---------------------------------");

        System.out.println(this.unfinishedUrlFutureTexts.size() + " Tweets with URLs have to be analyzed from last round.");
        Map<Status, Map<String, Future<String>>> unfinishedUrlTextsForTweets = writeUrlsToDBWhichAreDone(this.unfinishedUrlFutureTexts);
        cancelUnfinishedUrlTexts(this.unfinishedUrlFutureTexts);

        System.out.println("---------------------------  collect URLs  ---------------------------------");
        System.out.println(this.urlFutureTextsForTweets.size() + " Tweets with URLs have to be analyzed.");
        this.unfinishedUrlFutureTexts = writeUrlsToDBWhichAreDone(this.urlFutureTextsForTweets);

        try {
            this.dbManager.finalize();
            System.out.println("DBManager finalized.");
        } catch (Throwable throwable) {
            System.out.println("Could not finalize DBManager.");
            throwable.printStackTrace();
        }

        this.urlFutureTextsForTweets.clear();
        this.dbManager = new DBManager();
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
    }

    private void cancelUnfinishedUrlTexts(Map<Status, Map<String, Future<String>>> unfinishedUrlFutureTexts) {
        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : unfinishedUrlFutureTexts.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                urlEntry.getValue().cancel(true);
            }
        }
    }

    private Map<Status, Map<String, Future<String>>> writeUrlsToDBWhichAreDone(Map<Status, Map<String,Future<String>>> currentUrlTextsForTweets) {
        int exceptionCounter = 0;
        int doneCounter = 0;
        Map<Status, Map<String,Future<String>>> unfinishedUrlTextsForTweets = new HashMap<Status, Map<String, Future<String>>>();

        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : currentUrlTextsForTweets.entrySet()) {
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
        waitForUrlsWhichAreNotDone();

        this.executorService.shutdown();
        super.finalize();
    }

    private void waitForUrlsWhichAreNotDone() {
        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : this.urlFutureTextsForTweets.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                this.writeURLContentToDB(urlFutureTextsForTweet.getKey(), urlEntry.getKey(), urlEntry.getValue());
            }
        }
    }

}
