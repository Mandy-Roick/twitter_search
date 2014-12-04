package org.twittersearch.app.search_engine;

/**
 * Created by Mandy Roick on 02.12.2014.
 */
public class MassoudiExpansionTerm implements Comparable {
    private String term;
    private int cooccurrenceCount = 0;
    private long overallFrequency = 0L;
    private double score = -1.0;

    public MassoudiExpansionTerm(String term, int cooccurrenceCount, int overallFrequency) {
        this.term = term;
        this.cooccurrenceCount = cooccurrenceCount;
        this.overallFrequency = overallFrequency;
    }

    public MassoudiExpansionTerm(String term) {
        this.term = term;
        this.cooccurrenceCount = 1;
    }

    public String getTerm() {
        return term;
    }

    public int getCooccurrenceCount() {
        return cooccurrenceCount;
    }

    public long getOverallFrequency() {
        return overallFrequency;
    }

    public Double getScore() {
        return score;
    }

    public void setOverallFrequency(long overallFrequency) {
        this.overallFrequency = overallFrequency;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof MassoudiExpansionTerm))return false;
        MassoudiExpansionTerm otherMyClass = (MassoudiExpansionTerm)other;
        return this.equals(otherMyClass);
    }

    public boolean equals(MassoudiExpansionTerm other) {
        return this.term.equals(other.getTerm());
    }

    @Override
    public int compareTo(Object o) {
        MassoudiExpansionTerm other = (MassoudiExpansionTerm) o;
        return other.getScore().compareTo(this.score);
    }

    public void increaseCooccurrenceCount() {
        this.cooccurrenceCount++;
    }

    public void increaseOverallFrequency() {
        this.overallFrequency++;
    }
}
