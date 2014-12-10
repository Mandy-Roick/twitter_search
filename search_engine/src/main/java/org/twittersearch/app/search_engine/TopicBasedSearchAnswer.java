package org.twittersearch.app.search_engine;

import java.util.Map;

/**
 * Created by Mandy Roick on 07.12.2014.
 */
public class TopicBasedSearchAnswer extends SearchAnswer {

    private double score;
    private int rank = -1;

    public TopicBasedSearchAnswer(String id, String source, Map<String, Object> fields, double score) {
        super(id, source, fields);
        this.score = score;
    }

    public Double getScore() {
        return score;
    }

    public void updateScore(double additionalScore) {
        this.score += additionalScore;
    }

    @Override
    public void setFinalRank(int finalRank) {
        this.rank = finalRank;
    }

    @Override
    public int compareTo(Object o) {
        TopicBasedSearchAnswer other = (TopicBasedSearchAnswer) o;
        return other.getScore().compareTo(this.getScore());
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SearchAnswer))return false;
        TopicBasedSearchAnswer otherMyClass = (TopicBasedSearchAnswer)other;

        return this.getId().equals(otherMyClass.getId());
    }
}
