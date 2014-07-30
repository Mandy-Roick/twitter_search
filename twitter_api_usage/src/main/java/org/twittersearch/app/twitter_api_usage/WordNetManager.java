package org.twittersearch.app.twitter_api_usage;

import java.util.LinkedList;
import java.util.List;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WordNetManager {
	WordNetDatabase database;
	
	public WordNetManager() {
		System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");
		this.database = WordNetDatabase.getFileInstance();
		
	}
	
	public List<String> getHyponymsFor(String input) {
		List<String> resultingHyponyms = new LinkedList<String>();
		
		NounSynset synset; 
		NounSynset[] hyponyms; 
		NounSynset[] topics;
		Synset[] synsets = this.database.getSynsets(input, SynsetType.NOUN); 
		for (int i = 0; i < synsets.length; i++) { 
			synset = (NounSynset)(synsets[i]); 
			topics = synset.getTopics();
			if (topics.length > 0) {
				System.out.println(topics[0].toString());
			}
		    hyponyms = synset.getHyponyms(); 
		    for (int j = 0; j < hyponyms.length; j++) {
		    	resultingHyponyms.add(hyponyms[j].toString());
		    }
		    //System.err.println(nounSynset.getWordForms()[0] + 
		    //        ": " + nounSynset.getDefinition() + ") has " + hyponyms.length + " hyponyms"); 
		}
		return resultingHyponyms;
	}

}
