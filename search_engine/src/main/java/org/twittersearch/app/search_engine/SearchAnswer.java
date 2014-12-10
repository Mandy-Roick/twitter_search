package org.twittersearch.app.search_engine;

import java.util.Map;

/**
 * Created by Mandy Roick on 03.12.2014.
 */
public class SearchAnswer implements Comparable {

    protected String source;
    protected String id;
    protected Map<String, Object> fields;
    protected int rank;

    private String evaluationFlag = "";

    protected SearchAnswer(String id, String source, Map<String, Object> fields) {
        this.id = id;
        this.source = source;
        this.fields = fields;
    }

    public SearchAnswer(String id, String source, Map<String, Object> fields, int rank) {
        this.id = id;
        this.source = source;
        this.fields = fields;
        this.rank = rank;
    }

    public String getEvaluationFlag() {
        if (this.evaluationFlag != null && this.evaluationFlag.equals("")) {
            this.evaluationFlag = (String) fields.get("evaluation_flag");
        }
        return this.evaluationFlag;
    }

    public String getId() {
        return id;
    }

    public Integer getRank() {
        return rank;
    }

    public void setFinalRank(int finalRank) {
        this.rank = finalRank;
    }

    @Override
    public String toString() {
        String result = " ";
        result += "id: " + fields.get("id") + " ";
        result += "evaluation_flag: " + this.getEvaluationFlag() + " ";
        result += "content: " + fields.get("content");

        return result;
    }

    @Override
    public int compareTo(Object o) {
        SearchAnswer other = (SearchAnswer) o;
        return this.getRank().compareTo(other.getRank());
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SearchAnswer))return false;
        SearchAnswer otherMyClass = (SearchAnswer)other;
        System.out.println("Oo");

        return this.getId().equals(otherMyClass.getId());
    }
}
