package org.twittersearch.app.search_engine;

import java.util.Map;

/**
 * Created by Mandy Roick on 07.12.2014.
 */
public class TopicBasedSearchAnswer extends SearchAnswer {

    private int topicRank;
    private int numberOfTopics;
    private int finalRank = -1;

    public TopicBasedSearchAnswer(String id, String source, Map<String, Object> fields, int rank, int topicRank, int numberOfTopics) {
        super(id, source, fields, rank);
        this.topicRank = topicRank;
        this.numberOfTopics = numberOfTopics;

    }

    public int getTopicRank() {
        return topicRank;
    }


    public void setTopicRank(int topicRank) {
        this.topicRank = topicRank;
    }

    @Override
    public void setFinalRank(int finalRank) {
        this.finalRank = finalRank;
    }

    @Override
    public Integer getPosition() {
        if (finalRank == -1) {
            return this.topicRank + this.numberOfTopics * this.rank;
        }
        return finalRank;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SearchAnswer))return false;
        TopicBasedSearchAnswer otherMyClass = (TopicBasedSearchAnswer)other;

        // We have to pass on the best rank.
        if (this.getPosition() > otherMyClass.getPosition()) {
            this.setRank(otherMyClass.getRank());
            this.setTopicRank(otherMyClass.getTopicRank());
        } else {
            otherMyClass.setRank(this.getRank());
            otherMyClass.setTopicRank(this.getTopicRank());
        }

        return this.getId().equals(otherMyClass.getId());
    }
}
