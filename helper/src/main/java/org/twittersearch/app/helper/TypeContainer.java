package org.twittersearch.app.helper;

import java.util.*;

/**
 * Created by Mandy Roick on 24.10.2014.
 */
public class TypeContainer {

    private String name;
    private int overallTopicCount;
    private Map<Integer, Integer> topicCounts;

    public TypeContainer(String type, int overallTopicCount, Map<Integer, Integer> topicCounts) {
        this.name = type;
        this.overallTopicCount = overallTopicCount;
        this.topicCounts = topicCounts;
    }

    public String getName() {
        return name;
    }

    public int getOverallTopicCount() {
        return overallTopicCount;
    }

    public Map<Integer, Double> getBestTopics(double thresholdPercentage) {
        List<Map.Entry<Integer, Integer>> sortedTopics = new ArrayList<Map.Entry<Integer, Integer>>(this.topicCounts.entrySet());

        Collections.sort(sortedTopics, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        int overallTopicCount = 0;
        for (Map.Entry<Integer, Integer> topicCount : sortedTopics) {
            overallTopicCount += topicCount.getValue();
        }

        Map<Integer, Double> bestTopics = new HashMap<Integer, Double>();
        for (Map.Entry<Integer, Integer> topicCount : sortedTopics) {
            if( topicCount.getValue() < (this.overallTopicCount * thresholdPercentage)) {
                break;
            }
            bestTopics.put(topicCount.getKey(), topicCount.getValue() / (double) overallTopicCount * 200 / 100);
        }

        return bestTopics;

    }

    public int getTopicCountForTopic(int topicIndex) {
        Integer topicCount = topicCounts.get(topicIndex);
        if (topicCount == null) {
            return 0;
        } else {
            return topicCount;
        }
    }
}
