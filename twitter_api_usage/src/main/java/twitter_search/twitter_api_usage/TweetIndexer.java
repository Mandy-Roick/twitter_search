package twitter_search.twitter_api_usage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import twitter4j.Status;
import twitter4j.URLEntity;
import au.com.bytecode.opencsv.CSVWriter;

public class TweetIndexer {
	private String indexLocation;
	private IndexWriter indexWriter;
	CSVWriter csvWriter;
	private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
	
	public TweetIndexer() {
		
	}
	
	public TweetIndexer(String indexLocation, String csvLocation) throws IOException {
		this.csvWriter = new CSVWriter(new FileWriter(csvLocation), '\t');
		this.indexLocation = indexLocation;
		FSDirectory dir = FSDirectory.open(new File (this.indexLocation));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, this.analyzer);
	    this.indexWriter = new IndexWriter(dir, config);
	}

	public void addTweet(Status tweet) {
		this.addTweetToIndex(tweet);
		this.addTweetToCsv(tweet);
	}
	
	private void addTweetToIndex(Status tweet) {
		org.apache.lucene.document.Document document = new Document();
		document.add(new TextField("content", tweet.getText(), Field.Store.YES));
		document.add(new LongField("id", tweet.getId(), Field.Store.YES));
		this.resolveUrls(tweet.getURLEntities());
		try {
			this.indexWriter.addDocument(document);
		} catch (IOException e) {
			System.out.println("Lucene index could not be written.");
			e.printStackTrace();
		}
		
		//TODO: add content of URLs
	}
	
	public void resolveUrls(URLEntity[] urls) {		
		System.out.println(urls.length);
		org.jsoup.nodes.Document urlDoc;
		for(int i = 0; i < urls.length; i++) {			
			try {
				urlDoc = Jsoup.connect(urls[i].getURL()).get();
				Elements paragraphs = urlDoc.select("p");//ul?, title, h1
				Elements titles = urlDoc.select("title");
				Elements h1 = urlDoc.select("h1");
				
				//Elements ul = doc.select("ul");
				//System.out.println("Paragraphs: " + paragraphs.text());
				//System.out.println("Title: " + titles.text());
				//System.out.println("h1: " + h1.text());
				//System.out.println("ul: " + ul.text());
//				System.out.println(urls[i].getURL());
				
//				url = new URL(urls[i].getURL());
//				URLConnection connection = url.openConnection();
//				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				String inputLine;
//	            while ((inputLine = br.readLine()) != null) {
//	                   System.out.println(inputLine);
//	            }
//	            br.close();
			} catch (IOException e) {
				System.out.println("Could not open Url-connection for " + urls[i].getURL());
				e.printStackTrace();
			}
		}
	}
	private void addTweetToCsv(Status tweet) {
		String[] line = new String[5];
		line[0] = String.valueOf(tweet.getId());
		line[1] = String.valueOf(tweet.getUser().getId());
		line[2] = String.valueOf(tweet.getCreatedAt());
		line[3] = String.valueOf(tweet.getHashtagEntities());
		line[4] = String.valueOf(tweet.getFavoriteCount());
		line[5] = String.valueOf(tweet.getPlace().getId());
		//tweet.getLang();
		//tweet.getRetweetCount();
		//tweet.getScopes();
	}
	
	public void closeWriter() {
	    try {
	    	this.csvWriter.close();
			this.indexWriter.close();
		} catch (IOException e) {
			System.out.println("Could not close index or csv.");
			e.printStackTrace();
		}
	}
	
	public void search(String query) {
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(this.indexLocation)));
			IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
		    
		    Query q;
			try {
				q = new QueryParser(Version.LUCENE_40, "content", this.analyzer).parse(query);
		        searcher.search(q, collector);
		        ScoreDoc[] hits = collector.topDocs().scoreDocs;

		        // 4. display results
		        System.out.println("Found " + hits.length + " hits.");
		        for(int i=0;i<hits.length;++i) {
		          int docId = hits[i].doc;
		          org.apache.lucene.document.Document d = searcher.doc(docId);
		          System.out.println((i + 1) + ". " + d.get("id") + " score=" + hits[i].score);
		        }
			    
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//reader.close();

		} catch (IOException e) {
			System.out.println("Could not read index file.");
			e.printStackTrace();
		}
		
	}
}
