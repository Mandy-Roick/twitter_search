package org.twittersearch.app.search_engine;

import java.util.Map;

/**
 * Created by Mandy Roick on 07.12.2014.
 */
public class TopicBasedSearchAnswer extends SearchAnswer {

    private double score;
    private double topicScore;
    private double searchScore;
    private int doubled = 0;
    private String expandedQuery;
    private int rank = -1;

    public TopicBasedSearchAnswer(String id, String source, Map<String, Object> fields, double score, String expandedQuery, double searchScore, double topicScore) {
        super(id, source, fields);
        this.expandedQuery = expandedQuery;
        this.score = score;
        this.searchScore = searchScore;
        this.topicScore = topicScore;
    }

    public Double getScore() {
        return score;
    }

    public void updateScore(double additionalScore) {
        if (additionalScore > this.score)
            this.score = additionalScore;
        this.doubled++;
    }

    @Override
    public void setFinalRank(int finalRank) {
        this.rank = finalRank;
    }

    @Override
    public String toString() {
        String result = " ";
        result += "score: " + this.score + ", " + this.topicScore + ", " + this.searchScore + ", " + this.doubled + "; ";
        result += "query: " + this.expandedQuery + "\n";
        result += "\t id: " + fields.get("id") + " ";
        result += "evaluation_flag: " + this.getEvaluationFlag() + " ";
        result += "content: " + fields.get("content");

        return result;
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
