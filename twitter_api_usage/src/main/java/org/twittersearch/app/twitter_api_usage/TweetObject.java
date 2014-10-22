package org.twittersearch.app.twitter_api_usage;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by Mandy Roick on 05.10.2014.
 */
public class TweetObject {

    private Long id;
    private String content;

    //public enum EvaluationFlag {none, politics, sports, economy, ebola, football, soccer}
    //private EvaluationFlag flag;
    private String flag;
    //private Map<String, String> urls;

    public TweetObject(Long id, String content, String evaluationFlag) {
        this.id = id;
        this.content = content;
        this.flag = evaluationFlag;

        //if (evaluationFlag.equals("politics")) this.flag = EvaluationFlag.politics;
        //else if (evaluationFlag.equals("sports")) this.flag = EvaluationFlag.sports;
        //else if (evaluationFlag.equals("economy")) this.flag = EvaluationFlag.economy;
        //else if (evaluationFlag.equals("ebola")) this.flag = EvaluationFlag.ebola;
        //else if (evaluationFlag.equals("football")) this.flag = EvaluationFlag.football;
        //else if (evaluationFlag.equals("soccer")) this.flag = EvaluationFlag.soccer;
        //else this.flag = EvaluationFlag.none;
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

    public String toJson() {
        JsonObject tweetObject = new JsonObject();
        tweetObject.addProperty("id", this.id);
        tweetObject.addProperty("content", this.content);
        tweetObject.addProperty("evaluation_flag", this.flag);

        return tweetObject.toString();
    }
}
