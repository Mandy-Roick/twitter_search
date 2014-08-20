package org.twittersearch.app.twitter_api_usage;

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

    TweetToDBWriter() {
        this.dbManager = new DBManager();
    }

    public void writeTweetToDB (Status tweet) {
        this.dbManager.writeTweetToDB(tweet);
        this.dbManager.writeHashtagsToDB(tweet);

        writeUrlsToDB(tweet);
    }

    public void writeUrlsToDB (Status tweet) {
        URLEntity[] urlEntities = tweet.getURLEntities();
        if (urlEntities.length == 0) return;

        for(URLEntity urlEntity : urlEntities) {
            this.dbManager.writeUrlContentToDB(tweet.getId(), urlEntity.getURL(), false);
        }
    }

    public void closeDB() {
        System.out.println("---------------------------  close DB for cleanup  ---------------------------------");
        try {
            this.dbManager.finalize();
            System.out.println("DBManager finalized.");
        } catch (Throwable throwable) {
            System.out.println("Could not finalize DBManager.");
            throwable.printStackTrace();
        }

        this.dbManager = new DBManager();
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
    }

    protected void finalize() throws Throwable {
        try {
            this.dbManager.finalize();
            System.out.println("DBManager finalized.");
        } catch (Throwable throwable) {
            System.out.println("Could not finalize DBManager.");
            throwable.printStackTrace();
        }
        super.finalize();
    }

}
