package org.twittersearch.app.topic_modelling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kleiner Klotz on 19.08.2014.
 */
public class TopicContainer implements Comparable {

    private int score = 0;
    private int numberOfWords = 0;
    private String[] topWords;

    public TopicContainer() {
        this.score = 0;
        this.numberOfWords = 0;
    }

    public TopicContainer(int score, String[] topWords) {
        this.score = score;
        this.topWords = topWords;
    }

    public Integer getScore() {
        return this.score;
    }

    public Integer getNumberOfWords() {
        return this.numberOfWords;
    }

    public String[] getTopWords() {
        assert(this.topWords != null);
        return this.topWords;
    }

    public String[] getScoreAndTopWordsAsLine() {
        List<String> scoreAndTopWords = new ArrayList<String>();
        scoreAndTopWords.add(Integer.toString(this.getScore()));
        scoreAndTopWords.add(Integer.toString(this.getNumberOfWords()));
        scoreAndTopWords.addAll(Arrays.asList(this.getTopWords()));
        return scoreAndTopWords.toArray(new String[scoreAndTopWords.size()]);
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void addWord(int numberOfWords) {
        this.numberOfWords += numberOfWords;
    }

    public void setTopWords(String[] topWords) {
        this.topWords = topWords;
    }

    @Override
    public int compareTo(Object o) {
        TopicContainer other = (TopicContainer) o;
        return other.getScore().compareTo(this.score);
    }
}
