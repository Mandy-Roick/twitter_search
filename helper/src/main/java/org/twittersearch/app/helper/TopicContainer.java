package org.twittersearch.app.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 19.08.2014.
 */
public class TopicContainer implements Comparable {

    private double score = 0; // this is the topic count
    private int topicCount = 0;
    private int numberOfWords = 0; // this is for counting the number of words in the topic
    private int id;
    private String[] topWords;
    private String queryTerm = null;

    public TopicContainer(int id, int topicCount, int numberOfWords) {
        this(id, topicCount, numberOfWords, null);
    }

    public TopicContainer(int id, String[] topWords, int topicCount) {
        this(id, topicCount, -1, topWords); // assume numberOfWords is not needed
    }

    private TopicContainer(int id, int topicCount, int numberOfWords, String[] topWords) {
        this.id = id;
        this.topicCount = topicCount;
        this.numberOfWords = numberOfWords;
        this.topWords = topWords;
    }

    public int getId() {
        return id;
    }

    public Integer getTopicCount() {
        return this.topicCount;
    }

    public double getScore() {
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
        List<String> topWordsAndQueryTerm = new ArrayList<String>();
        topWordsAndQueryTerm.add(queryTerm);
        for (int i = 0; i < numberOfTopWords; i++) {
            topWordsAndQueryTerm.add(this.topWords[i]);
        }
        return topWordsAndQueryTerm.toArray(new String[topWordsAndQueryTerm.size()]);
        // return Arrays.copyOfRange(this.getTopWords(), 0, numberOfTopWords);
    }

    public String[] getTopicCountAndTopWordsAsLine() {
        List<String> topicCountAndTopWords = new ArrayList<String>();
        topicCountAndTopWords.add(Integer.toString(this.id));
        topicCountAndTopWords.add(Integer.toString(this.getTopicCount()));
        topicCountAndTopWords.add(Integer.toString(this.getNumberOfWords()));
        topicCountAndTopWords.addAll(Arrays.asList(this.getTopWords()));
        return topicCountAndTopWords.toArray(new String[topicCountAndTopWords.size()]);
    }

    public void calculateAndSetScore(int overallTopicCount, int numberOfTopics) {
        this.score = this.topicCount / (double) overallTopicCount * numberOfTopics;
    }

    public void updateScore(double typesTopicCount) {
        this.score += typesTopicCount;
    }

    public void setQueryTerm(String queryTerm) {
        this.queryTerm = queryTerm;
    }

    public void addTopicCount(int topicCount) {
        this.topicCount += topicCount;
    }

    public void addWord(int numberOfWords) {
        this.numberOfWords += numberOfWords;
    }

    public void setTopWords(String[] topWords) {
        this.topWords = topWords;
    }

    public void unstemmTopWords(Map<String, String> stemmingDictionary) {
        String originalQueryTerm = stemmingDictionary.get(this.queryTerm);
        if (originalQueryTerm != null) {
            this.queryTerm = originalQueryTerm;
        }

        String originalWord;
        for (int i = 0; i < this.topWords.length; i++) {
            originalWord = stemmingDictionary.get(this.topWords[i]);
            if (originalWord != null) {
                this.topWords[i] = originalWord;
            }
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "Id: " + this.getId() + ", ";
        result += "Score: " + this.getTopicCount() + ", ";

        result += "TopWords: ";
        String[] topWords = this.getTopWords(3);
        for (String topWord : topWords) {
            result += topWord + " ";
        }
        return result;
    }

    @Override
    public int compareTo(Object o) {
        TopicContainer other = (TopicContainer) o;
        return other.getTopicCount().compareTo(this.topicCount);
    }
}
