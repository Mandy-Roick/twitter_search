package org.twittersearch.app.topic_modelling;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 23.09.2014.
 */
public class TweetPreprocessor {

    Object2ObjectMap<String, String[]> splitHashtags;

    public TweetPreprocessor() {
        this.splitHashtags = new Object2ObjectOpenHashMap<String, String[]>();
    }

    public Map<Long, String> preprocessTweets(Map<Long, String> tweets, Map<Long, List<String>> tweetsHashtags) {

        Map<Long, String> preprocessedTweets = new HashMap<Long, String>();
        int counter = 1; //only for status updates on the console
        for (Map.Entry<Long, String> entry : tweets.entrySet()) {
            if (counter % 1000 == 0) {
                System.out.println(counter);
            }
            counter++;

            preprocessedTweets.put(entry.getKey(), preprocessTweetContent(entry.getKey(), entry.getValue(), tweetsHashtags));
        }

        return preprocessedTweets;

    }

    private String preprocessTweetContent(Long tweetId, String tweetContent, Map<Long, List<String>> tweetsHashtags) {
        String preprocessedTweetContent = TweetPreprocessor.normalizeTweetContent(tweetContent);
        //System.out.println("Tweet: " + preprocessedTweetContent);
        preprocessedTweetContent += splitHashtags(tweetId, tweetsHashtags);
        //System.out.println("Hashtag Tweet: " + preprocessedTweetContent);
        return preprocessedTweetContent;
    }

    public static String normalizeTweetContent(String tweetContent) {
        String normalizedTweet = tweetContent.replace('\n',' ');
        normalizedTweet = normalizedTweet.replace('\r',' ');
        normalizedTweet = normalizedTweet.replace('\"','\'');
        normalizedTweet = normalizedTweet.replace('#',' ');

        // Remove all special characters
        Pattern unicode = Pattern.compile("[^\\x00-\\x7F]",
                Pattern.UNICODE_CASE | Pattern.CANON_EQ
                        | Pattern.CASE_INSENSITIVE);
        Matcher matcher = unicode.matcher(normalizedTweet);
        normalizedTweet = matcher.replaceAll(" ");

        // Remove all invisible characters (not sure, that this is helping)
        normalizedTweet = normalizedTweet.replaceAll("\\p{C}", "");
        // Remove all users
        normalizedTweet = normalizedTweet.replaceAll("@(\\w+)[\\s:\\p{Po}]"," ");
        normalizedTweet = normalizedTweet.replaceAll("@(\\w+)$","");
        // Remove all urls
        normalizedTweet = normalizedTweet.replaceAll("(\\w+)://(\\S+)\\s"," ");
        normalizedTweet = normalizedTweet.replaceAll("(\\w+)://(\\S+)$"," ");
        normalizedTweet = normalizedTweet.replaceAll("https?(\\p{Punct}+)"," ");
        normalizedTweet = normalizedTweet.replaceAll("https?(\\p{Punct}*)(\\s*)$"," ");

        return normalizedTweet;
    }

    private String splitHashtags(Long tweetId, Map<Long, List<String>> tweetsHashtags) {
        String concatenatedCamelCaseHashtags = "";
        List<String> hashtagsForTweet = tweetsHashtags.get(tweetId);
        if(hashtagsForTweet == null) {
            return concatenatedCamelCaseHashtags;
        }

        String[] currentSplitHashtag;
        String[] splitHashtagFromMap;
        String hashtagLowerCase;
        for (String hashtag : hashtagsForTweet) {
            currentSplitHashtag = StringUtils.splitByCharacterTypeCamelCase(hashtag);
            hashtagLowerCase = hashtag.toLowerCase();
            splitHashtagFromMap = this.splitHashtags.get(hashtagLowerCase);

            if ((splitHashtagFromMap != null) && (splitHashtagFromMap.length > currentSplitHashtag.length)) {
                currentSplitHashtag = splitHashtagFromMap;
            } else {
                this.splitHashtags.put(hashtagLowerCase, currentSplitHashtag);
            }
            if (currentSplitHashtag.length > 1) { //add only split hashtags, if they have more information than the hashtag itself
                for (String camelCaseWord : currentSplitHashtag) {
                    concatenatedCamelCaseHashtags += " " + camelCaseWord;
                }
            }
        }
        return concatenatedCamelCaseHashtags;
    }
}
