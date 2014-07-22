package twitter_search.twitter_api_usage;

import java.io.IOException;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TweetListener implements StatusListener {
	//private TweetIndexer ti;
    private TweetToDBWriter dbWriter;
	private int counter;
	
	public TweetListener() {
        this.dbWriter = new TweetToDBWriter();
//		try {
//			String folder = "C:\\Users\\kleiner Klotz\\Documents\\HPI\\4. Semester\\Masterarbeit\\Evaluation\\Corpus\\";
//			this.ti = new TweetIndexer(folder + "index", folder + "metadata.csv");
//		} catch (IOException e) {
//			System.out.println("Index file could not be open.");
//			e.printStackTrace();
//		}
	}

    public void onStatus(Status status) {
    	if(status.getLang().equals("en") && counter < 1000) {
    		//this.ti.addTweet(status);
            this.dbWriter.writeTweetToDB(status);
    		counter++;
    		
    		if((counter % 10) == 0) {
        		System.out.println(counter + " Tweet: " + status.getText());
        	}
    		
    	}
    	if(counter == 1000) {
    		System.out.println("closed :)");
            try {
                this.dbWriter.destroy();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            //this.ti.closeWriter();
            System.exit(1);
    		//counter++;
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
