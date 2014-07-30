package twitter_search.twitter_api_usage;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.twittersearch.app.twitter_api_usage.WordNetManager;

public class WordNetManagerTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGetHyponymsFor() throws Exception {
		WordNetManager wnManager = new WordNetManager();
		List<String> hyponyms = wnManager.getHyponymsFor("politics");
		System.out.println(hyponyms);
	}

}
