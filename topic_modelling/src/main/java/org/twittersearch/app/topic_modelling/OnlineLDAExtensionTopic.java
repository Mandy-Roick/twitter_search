package org.twittersearch.app.topic_modelling;

import java.util.Map;

/**
 * Created by Mandy Roick on 08.10.2014.
 */
public class OnlineLDAExtensionTopic {

    protected double topicScore;
    protected Map<String, Double> wordCounts;

    public OnlineLDAExtensionTopic(double topicScore, Map<String, Double> wordCounts) {
        this.topicScore = topicScore;
        this.wordCounts = wordCounts;
    }

    public String toCsvString() {
        String csvString = String.valueOf(topicScore);
        for(String word : wordCounts.keySet()) {
            csvString += "\t" + word;
        }
        return csvString;
    }
}
