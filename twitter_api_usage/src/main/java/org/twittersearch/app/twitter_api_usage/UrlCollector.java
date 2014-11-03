package org.twittersearch.app.twitter_api_usage;

import twitter4j.Status;
import twitter4j.URLEntity;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Mandy Roick on 07.08.2014.
 */
public class UrlCollector {

    public static void main(String[] args) {
        int numberOfThreads = 3;
        String date = "2014-10-20";
        if(args.length >= 1) {
            numberOfThreads = Integer.valueOf(args[0]);
        }
        if(args.length >= 2) {
            date = args[1];
        }

        collectMissingUrlContentsForDate(date, numberOfThreads);
    }

    public static void collectMissingUrlContents(int numberOfThreads) {
        DBManager dbManager = new DBManager();
        Map<Long, List<String>> tweetsUrls = dbManager.selectTweetsUrlsWithoutText();

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = new ThreadPoolExecutor(numberOfThreads, numberOfThreads+5, 30,
                                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(tweetsUrls.size()*2), threadFactory);//Executors.newFixedThreadPool(numberOfThreads);


        Map<Long, Map<String, Future<String>>> urlFutureTextsForTweets = startUrlThreads(tweetsUrls, executorService);
        collectUrlThreads(urlFutureTextsForTweets, dbManager);

        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted Exception during final termination of threads:");
            System.out.println(e.toString());
            //e.printStackTrace();
        }

        System.out.println("Second run of collecting URLs.");
        collectUrlThreads(urlFutureTextsForTweets, dbManager);
    }

    public static void collectMissingUrlContentsForDate(String date, int numberOfThreads) {
        DBManager dbManager = new DBManager();
        Map<Long, List<String>> tweetsUrls = dbManager.selectTweetsUrlsWithoutTextForDate(date);

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = new ThreadPoolExecutor(numberOfThreads, numberOfThreads+5, 30,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(tweetsUrls.size()*2), threadFactory);//Executors.newFixedThreadPool(numberOfThreads);


        Map<Long, Map<String, Future<String>>> urlFutureTextsForTweets = startUrlThreads(tweetsUrls, executorService);
        collectUrlThreads(urlFutureTextsForTweets, dbManager);

        while (!urlFutureTextsForTweets.isEmpty()) {
            try {
                System.out.println("-------------------- Wait for a minute. ---------------------");
                TimeUnit.MINUTES.sleep(10);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            collectUrlThreads(urlFutureTextsForTweets, dbManager);
        }


//        executorService.shutdown();
//        try {
//            executorService.awaitTermination(30, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            System.out.println("Interrupted Exception during final termination of threads:");
//            System.out.println(e.toString());
//            //e.printStackTrace();
//        }
//
//        System.out.println("Second run of collecting URLs.");
//        collectUrlThreads(urlFutureTextsForTweets, dbManager);
    }

    private static Map<Long, Map<String, Future<String>>> startUrlThreads(Map<Long, List<String>> tweetsUrls, ExecutorService executorService) {
        Map<Long, Map<String, Future<String>>> urlFutureTextsForTweets = new HashMap<Long, Map<String, Future<String>>>();
        for (Map.Entry<Long, List<String>> tweetUrls : tweetsUrls.entrySet()) {
            Long tweetId = tweetUrls.getKey();
            Map<String, Future<String>> urlsContents = new HashMap<String, Future<String>>();
            for (String url : tweetUrls.getValue()) {
                final Future<String> urlContent = executorService.submit(new UrlCrawler(url));
                urlsContents.put(url, urlContent);
            }
            urlFutureTextsForTweets.put(tweetId, urlsContents);
        }

        return  urlFutureTextsForTweets;
    }

    private static void collectUrlThreads(Map<Long, Map<String, Future<String>>> urlFutureTextsForTweets, DBManager dbManager) {
        System.out.println("Collect " + urlFutureTextsForTweets.size() + " Tweets with Urls.");
        int counter = 1;
        int exceptionCounter = 0;
        for (Map.Entry<Long, Map<String,Future<String>>> urlFutureTextsForTweet : urlFutureTextsForTweets.entrySet()) {
            for (Map.Entry<String, Future<String>> urlContent : urlFutureTextsForTweet.getValue().entrySet()) {
                if (urlContent.getValue().isDone()) {
                    if (!writeURLContentToDB(urlFutureTextsForTweet.getKey(), urlContent.getKey(), urlContent.getValue(), dbManager)) {
                        exceptionCounter++;
                    }
                }
            }

            if((counter % 100) == 0) {
                System.out.println("");
                System.out.println("------------------------------------------------------------");
                System.out.println(counter + " Tweets done, " + exceptionCounter + " Exceptions.");
                System.out.println("------------------------------------------------------------");
                System.out.println("");
            }
            counter++;
        }
        counter++;
    }

    private static boolean writeURLContentToDB(Long tweetId, String url, Future<String> urlContent, DBManager dbManager) {
        boolean noException = true;
        try {
            dbManager.writeUrlContentToDB(tweetId, url, true, urlContent.get());
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

    DBManager dbManager;
    ExecutorService executorService;
    Map<Status, Map<String, Future<String>>> urlFutureTextsForTweets; // {tweet : {url1 : urlContent1, url2 : urlContent2,...},...}
    Map<Status, Map<String, Future<String>>> unfinishedUrlFutureTexts;

    UrlCollector() {
        this.dbManager = new DBManager();
        this.urlFutureTextsForTweets = new HashMap<Status, Map<String,Future<String>>>();
        this.unfinishedUrlFutureTexts = new HashMap<Status, Map<String,Future<String>>>();
        this.executorService = Executors.newFixedThreadPool(20);
    }

    UrlCollector(int numberOfThreads) {
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
            final Future<String> urlText = this.executorService.submit(new UrlCrawler(urlEntity.getURL()));
            urlTexts.put(urlEntity.getURL(), urlText);
        }
        this.urlFutureTextsForTweets.put(tweet, urlTexts);
    }

    public void collectUrls() {
        System.out.println("");

        System.out.println("---------------------------  collect URLs  ---------------------------------");
        System.out.println(this.urlFutureTextsForTweets.size() + " Tweets with URLs have to be analyzed.");
        Map<Status, Map<String, Future<String>>> currentlyUnfinishedUrlFutureTexts = writeUrlsToDBWhichAreDone(this.urlFutureTextsForTweets);
        this.urlFutureTextsForTweets = currentlyUnfinishedUrlFutureTexts;

        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
    }

    public void collectUrlsAndCloseDB() {
        System.out.println("");
        System.out.println("---------------------------  collect and cancel unfinished URLs  ---------------------------------");

        try {
            // Sleep for 30 seconds to allow the urls threads to gather the missing urls.
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("Could not sleep.");
            e.printStackTrace();
        }

        System.out.println(this.unfinishedUrlFutureTexts.size() + " Tweets with URLs have to be analyzed from last round.");
        writeUrlsToDBWhichAreDone(this.unfinishedUrlFutureTexts);
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
                    boolean noException = this.writeURLContentToDB(urlFutureTextsForTweet.getKey().getId(), urlEntry.getKey(), urlEntry.getValue(), this.dbManager);
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

    protected void finalize() throws Throwable {
        collectUrlsAndCloseDB();
        waitForUrlsWhichAreNotDone(this.unfinishedUrlFutureTexts);

        this.executorService.shutdown();
        super.finalize();
    }

    private void waitForUrlsWhichAreNotDone(Map<Status, Map<String, Future<String>>> urlFutureTexts) {
        for (Map.Entry<Status, Map<String,Future<String>>> urlFutureTextsForTweet : urlFutureTexts.entrySet()) {
            for (Map.Entry<String, Future<String>> urlEntry : urlFutureTextsForTweet.getValue().entrySet()) {
                this.writeURLContentToDB(urlFutureTextsForTweet.getKey().getId(), urlEntry.getKey(), urlEntry.getValue(), this.dbManager);
            }
        }
    }

}
