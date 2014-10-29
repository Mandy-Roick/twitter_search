package twitter_search;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.twittersearch.app.evaluation.SpinaCorpusReader;

public class SpinaCorpusReaderTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testConstructor() throws Exception {
		String fileNameIndex = "C:\\Users\\kleiner Klotz\\Documents\\HPI\\4. Semester\\Masterarbeit\\Evaluation\\entityProfiling_ORM_Twitter_aspects_dataset\\entityProfiling_ORM_Twitter_aspects_dataset\\aspects_queries_ids.tsv";
		String fileNameGoldstandard = "C:\\Users\\kleiner Klotz\\Documents\\HPI\\4. Semester\\Masterarbeit\\Evaluation\\entityProfiling_ORM_Twitter_aspects_dataset\\entityProfiling_ORM_Twitter_aspects_dataset\\aspects_goldstandard_qrels";
		String fileNameAnnotations = "C:\\Users\\kleiner Klotz\\Documents\\HPI\\4. Semester\\Masterarbeit\\Evaluation\\entityProfiling_ORM_Twitter_aspects_dataset\\entityProfiling_ORM_Twitter_aspects_dataset\\aspects_terms_annotation.tsv";
		SpinaCorpusReader reader = new SpinaCorpusReader(fileNameIndex, fileNameGoldstandard, fileNameAnnotations);
	}

}
