package org.twittersearch.app.search_engine;

import java.util.Map;

/**
 * Created by Mandy Roick on 03.12.2014.
 */
public class SearchAnswer {

    private String source;
    private Map<String, Object> fields;
    private int rank;
    private int topicRank = 0;
    private int numberOfTopics = 1;

    private String evaluationFlag = "";

    public SearchAnswer(String source, Map<String, Object> fields, int rank) {
        this.source = source;
        this.fields = fields;
        this.rank = rank;
    }

    public SearchAnswer(String source, Map<String, Object> fields, int rank, int topicRank, int numberOfTopics) {
        this(source,fields,rank);
        this.topicRank = topicRank;
        this.numberOfTopics = numberOfTopics;
    }

    public String getEvaluationFlag() {
        if (this.evaluationFlag != null && this.evaluationFlag.equals("")) {
            this.evaluationFlag = (String) fields.get("evaluation_flag");
        }
        return this.evaluationFlag;
    }

    public int getPosition() {
        return this.topicRank + this.numberOfTopics * rank;
    }

    @Override
    public String toString() {
        String result = " ";
        result += "id: " + fields.get("id") + " ";
        result += "evaluation_flag: " + this.getEvaluationFlag() + " ";
        result += "content: " + fields.get("content");

        return result;
    }
}
