package org.twittersearch.app.topic_modelling;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 08.10.2014.
 */
public class OnlineLDAExtensionTopic {

    protected double topicScore;
    //protected Map<String, Double> wordCounts;
    protected List<String> topWords;

    public OnlineLDAExtensionTopic(double topicScore, List<String> topWords) {
        this.topicScore = topicScore;
        //this.wordCounts = wordCounts;
        this.topWords = topWords;
    }

    public String toCsvString(int numberOfTopWords) {
        String csvString = String.valueOf(topicScore);
        int counter = 0;
        for(String word : topWords.subList(0, numberOfTopWords)) {
            csvString += "\t" + word;
        }
        return csvString;
    }
}
