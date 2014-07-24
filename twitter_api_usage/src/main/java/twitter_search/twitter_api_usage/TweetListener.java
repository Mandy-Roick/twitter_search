package twitter_search.twitter_api_usage;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TweetListener implements StatusListener {
	//private TweetIndexer ti;
    private TweetToDBWriter dbWriter;
	private int counter = 1;
	
	public TweetListener() {
        this.dbWriter = new TweetToDBWriter();
	}

    public void onStatus(Status status) {
    	if(status.getLang().equals("en")) {
    		//this.ti.addTweet(status);
            this.dbWriter.writeTweetToDB(status);
    		
    		if((counter % 100) == 0) {
        		System.out.println(counter + " Tweet: " + status.getText());
        	}
    		counter++;

            if((counter % 1000) == 0) {
                //System.out.println("10,000 :)");
                try {
                    this.dbWriter.collectUrlsAndCloseDB();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                //this.dbWriter = new TweetToDBWriter();
                //System.exit(1);

            }
    	}
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
    
    public void search(String query) {
    	//ti.search(query);
    }
}
