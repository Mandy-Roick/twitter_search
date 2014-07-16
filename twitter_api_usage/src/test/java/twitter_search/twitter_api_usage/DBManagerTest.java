package twitter_search.twitter_api_usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by kleiner Klotz on 16.07.2014.
 */
public class DBManagerTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

	@Test
	public void testConnect() throws Exception {
		DBManager dbm = new DBManager();
		dbm.connect();
	}
}
