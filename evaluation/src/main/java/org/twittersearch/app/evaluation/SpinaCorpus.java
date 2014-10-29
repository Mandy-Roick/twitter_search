package org.twittersearch.app.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinaCorpus {
	Map<String, String> index;
	Map<String, List<String>> goldStandard;
	Map<String, Map<String, String[]>> annotations; //entity -> {term -> [score1,score2,score3]}

	public SpinaCorpus() {
		this.index = new HashMap<String, String>();
		this.goldStandard = new HashMap<String, List<String>>();
		this.annotations = new HashMap<String, Map<String, String[]>>();
	}
	
	public SpinaCorpus(Map<String, String> index, Map<String, List<String>> goldStandard, Map<String, Map<String, String[]>> annotations) {
		this.index = index;
		this.goldStandard = goldStandard;
		this.annotations = annotations;
	}
	
	public void setIndex(Map<String, String> index) {
		this.index = index;
	}
	
	public Map<String, String> getIndex() {
		return this.index;
	}
	
	public void setGoldStandard(Map<String, List<String>> goldStandard) {
		this.goldStandard = goldStandard;
	}
	
	public void setAnnotations(Map<String, Map<String, String[]>> annotations) {
		this.annotations = annotations;
	}
}
