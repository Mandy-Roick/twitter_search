package org.twittersearch.app.twitter_api_usage;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * Created by Mandy Roick on 24.11.2014.
 */
public class TweetLanguageListener implements StatusListener {
    //private TweetIndexer ti;
    private TweetToDBWriter dbWriter;
    private int counter = 1;

    public TweetLanguageListener() {
        this.dbWriter = new TweetToDBWriter();
    }

    public void onStatus(Status status) {
        //this.ti.addTweet(status);
        this.dbWriter.writeTweetLanguageToDB(status);

        if ((counter % 100) == 0) {
            System.out.println(counter + " Tweet: " + status.getLang());
        }

        //if((counter % 1000) == 0) {
        //    this.dbWriter.closeDB();
        //}

        counter++;

    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
    }

    public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    public void onStallWarning(StallWarning warning) {
        System.out.println("Got stall warning:" + warning);
    }

    public void onException(Exception ex) {
        ex.printStackTrace();
    }

}
