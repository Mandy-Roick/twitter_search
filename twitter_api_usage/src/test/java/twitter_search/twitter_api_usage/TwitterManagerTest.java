package twitter_search.twitter_api_usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twitter4j.TwitterException;

public class TwitterManagerTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
//	@Test
//	public void testSearchFor() throws Exception {
//		TwitterManager twitterManager = new TwitterManager();
//		try {
//			twitterManager.searchFor("Politik");
//		} catch (TwitterException te) {
//			System.out.println(te.getMessage());
//		}
//	}
	
	@Test
	public void testOpenTweet() {
		TwitterManager twitterManager = new TwitterManager();
		try {
			twitterManager.openTweet(Long.parseLong("487207030785310720"));
		} catch (TwitterException te) {
			System.out.println(te.getMessage());
		}
	}
	
}
