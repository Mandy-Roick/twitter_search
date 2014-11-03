package org.twittersearch.app.topic_modelling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 19.08.2014.
 */
public class TopicContainer implements Comparable {

    private int score = 0; // this is the topic count
    private int numberOfWords = 0; // this is for counting the number of words in the topic
    private int id;
    private String[] topWords;

    public TopicContainer(int id, int score, int numberOfWords) {
        this(id, score, numberOfWords, null);
    }

    public TopicContainer(int id, String[] topWords) {
        this(id, -1, -1, topWords); // assume numberOfWords and score is not needed
    }

    private TopicContainer(int id, int score, int numberOfWords, String[] topWords) {
        this.id = id;
        this.score = score;
        this.numberOfWords = numberOfWords;
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

    public String[] getTopWords(int numberOfTopWords) {
        return Arrays.copyOfRange(this.getTopWords(), 0, numberOfTopWords);
    }

    public String[] getScoreAndTopWordsAsLine() {
        List<String> scoreAndTopWords = new ArrayList<String>();
        scoreAndTopWords.add(Integer.toString(this.id));
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

    public void unstemmTopWords(Map<String, String> stemmingDictionary) {
        String originalWord;
        for (int i = 0; i < this.topWords.length; i++) {
            originalWord = stemmingDictionary.get(this.topWords[i]);
            if (originalWord != null) {
                this.topWords[i] = originalWord;
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        TopicContainer other = (TopicContainer) o;
        return other.getScore().compareTo(this.score);
    }
}
