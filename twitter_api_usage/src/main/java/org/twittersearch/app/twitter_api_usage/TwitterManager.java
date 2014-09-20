package org.twittersearch.app.twitter_api_usage;

import org.twittersearch.app.twitter_api_usage.TweetIndexer;
import org.twittersearch.app.twitter_api_usage.TweetListener;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class TwitterManager 
{
	Twitter twitter;
	TwitterFactory tf;
	
	public TwitterManager() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("n5gjzGruXQcBlwZJ0Kj4UXyY7")
    	  .setOAuthConsumerSecret("tt35SEi66TDinub7lLcdewYAlv7G6tcOfDVzme5kKCQXRWGalb")
    	  .setOAuthAccessToken("2446857775-Ttl3gXob0CVO6Z0O7kHT5nM02DqG0krvzNJlyjZ")
    	  .setOAuthAccessTokenSecret("oEhkZmQ65NFHmPbMZwgZfYJTpuqQ2V1XX1yJt6Tge28nF");
    	this.tf = new TwitterFactory(cb.build());
    	this.twitter = this.tf.getInstance();
	}
	
	public void searchFor(String queryString) throws TwitterException {
		Query query = new Query(queryString);//source:twitter4j yusukey");
	    QueryResult result = this.twitter.search(query);
	    for (Status status : result.getTweets()) {
	        System.out.println(status.getId() + " : @" + status.getUser().getScreenName() + ":" + status.getText() + " +Language: " + status.getLang());
	    }
	    Status tweet = this.twitter.showStatus(Long.parseLong("297136541426397184"));
	    System.out.println("@" + tweet.getUser().getScreenName() + ":" + tweet.getText() + " +Language: " + tweet.getLang());
	}
	
	public void openTweet(Long id) throws TwitterException {
		Status tweet = this.twitter.showStatus(id);
		//System.out.println(tweet.getScopes().toString());
		TweetIndexer ti = new TweetIndexer();
		ti.resolveUrls(tweet.getURLEntities());
	}

    public List<String> getUrlsForTweet(Long id) {
        List<String> urls = new LinkedList<String>();
        try {
            Status tweet = this.twitter.showStatus(id);
            URLEntity[] urlEntities = tweet.getURLEntities();
            for(URLEntity urlEntity : urlEntities) {
                urls.add(urlEntity.getURL());
            }

        } catch (TwitterException e) {
            System.out.println("Could not read Tweet with ID: " + id);
            e.printStackTrace();
        }

        return urls;
    }
	
	public static void main(String[] args) throws TwitterException {
        //TwitterManager tm = new TwitterManager();
        //tm.openTweet(491999333449871360L);

		ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("n5gjzGruXQcBlwZJ0Kj4UXyY7")
    	  .setOAuthConsumerSecret("tt35SEi66TDinub7lLcdewYAlv7G6tcOfDVzme5kKCQXRWGalb")
    	  .setOAuthAccessToken("2446857775-Ttl3gXob0CVO6Z0O7kHT5nM02DqG0krvzNJlyjZ")
    	  .setOAuthAccessTokenSecret("oEhkZmQ65NFHmPbMZwgZfYJTpuqQ2V1XX1yJt6Tge28nF");
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        TweetListener listener = new TweetListener();
        twitterStream.addListener(listener);
        twitterStream.sample();
        //listener.search("office");
    }
}
