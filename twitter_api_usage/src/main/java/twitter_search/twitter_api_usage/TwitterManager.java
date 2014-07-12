package twitter_search.twitter_api_usage;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

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
	
	public static void main(String[] args) throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("n5gjzGruXQcBlwZJ0Kj4UXyY7")
    	  .setOAuthConsumerSecret("tt35SEi66TDinub7lLcdewYAlv7G6tcOfDVzme5kKCQXRWGalb")
    	  .setOAuthAccessToken("2446857775-Ttl3gXob0CVO6Z0O7kHT5nM02DqG0krvzNJlyjZ")
    	  .setOAuthAccessTokenSecret("oEhkZmQ65NFHmPbMZwgZfYJTpuqQ2V1XX1yJt6Tge28nF");
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        
        TweetListener listener = new TweetListener();
        twitterStream.addListener(listener);
        //twitterStream.sample();
        listener.search("office");
    }
}
