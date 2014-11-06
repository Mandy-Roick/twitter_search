package org.twittersearch.app.twitter_api_usage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mandy Roick on 05.10.2014.
 */
public class TweetObject extends Object {

    private Long id;
    private String content;
    private String createdAt;
    private List<String> urlContents;

    //public enum EvaluationFlag {none, politics, sports, economy, ebola, football, soccer}
    //private EvaluationFlag flag;
    private String flag;
    //private Map<String, String> urls;

    public TweetObject(Long id, String content, String evaluationFlag, String createdAt) {
        this.id = id;
        this.content = content;
        this.flag = evaluationFlag;
        this.createdAt = createdAt;
        this.urlContents = new LinkedList<String>();
    }

    public Long getId() {
        return id;
    }

    public String getFlag() {
        return flag;
    }

    public String getContent() {
        return content;
    }

    public void addUrlContent(String urlContent) {
        this.urlContents.add(urlContent);
    }

    public void addUrlContents(List<String> urlContents) {
        this.urlContents.addAll(urlContents);
    }

    public String toJson() {
        JsonObject tweetObject = new JsonObject();
        tweetObject.addProperty("id", this.id);
        tweetObject.addProperty("content", "\"" + this.content + "\"");
        tweetObject.addProperty("evaluation_flag", this.flag);
        tweetObject.addProperty("created_at", this.createdAt);

        if (!urlContents.isEmpty()) {
            JsonArray urlsObject = new JsonArray();
            for (String urlContent : this.urlContents) {
                urlsObject.add(new JsonPrimitive("\"" + urlContent + "\""));
            }
            tweetObject.add("url_content", urlsObject);
        }

        return tweetObject.toString();
    }

    @Override
    public String toString() {
        String result = "";
        result += this.getId() + ": ";
        result += this.getContent() + " ";
        result += "Evaluation Flag: " + this.getFlag();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        TweetObject tweetObject = (TweetObject) o;
        return this.id.equals(tweetObject.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
