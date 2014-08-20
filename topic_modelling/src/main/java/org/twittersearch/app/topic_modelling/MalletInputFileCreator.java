package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang.StringUtils;
import org.twittersearch.app.twitter_api_usage.DBManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 28.07.2014.
 */
public class MalletInputFileCreator {

    DBManager dbManager;
    Object2ObjectMap<String, String[]> splitHashtags;
    Map<Long, List<String>> tweetsHashtags;
    String date;

    public static void main(String[] args) {
        String date = "2014-08-02";
        MalletInputFileCreator malletInputFileCreator = new MalletInputFileCreator(date);
        malletInputFileCreator.writeDBContentToInputFile("mallet_input_file_" + date + ".csv");
    }

    public MalletInputFileCreator(String date) {
        this.date = date;
        this.dbManager = new DBManager();
        this.splitHashtags = new Object2ObjectOpenHashMap<String, String[]>();
    }

    private void writeDBContentToInputFile(String filePath) {
        Map<Long, String> tweetIdsToContent = this.dbManager.selectTweetsCreatedAt(date);
        this.tweetsHashtags = this.dbManager.selectTweetsAndHashtags();

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath), '\t', '\"');
            String[] line = new String[3];
            int counter = 1;
            for (Map.Entry<Long, String> entry : tweetIdsToContent.entrySet()) {
                if (counter % 1000 == 0) {
                    System.out.println(counter);
                }
                counter++;
                line[0] = entry.getKey().toString();
                line[1] = "X";
                line[2] = preprocessTweetContent(entry.getKey(), entry.getValue());
                csvWriter.writeNext(line);
            }
            csvWriter.close();
        } catch (IOException e) {
            System.out.print("InputFile for Topic Modelling could not be created.");
            e.printStackTrace();
        }
    }

    private String preprocessTweetContent(Long tweetId, String tweetContent) {
        String preprocessedTweetContent = normalizeTweetContent(tweetContent);
        //System.out.println("Tweet: " + preprocessedTweetContent);
        preprocessedTweetContent += splitHashtags(tweetId);
        //System.out.println("Hashtag Tweet: " + preprocessedTweetContent);
        return preprocessedTweetContent;
    }

    private String splitHashtags(Long tweetId) {
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

            for (String camelCaseWord : currentSplitHashtag) {
                concatenatedCamelCaseHashtags += " " + camelCaseWord;
            }
        }
        return concatenatedCamelCaseHashtags;
    }

    private String normalizeTweetContent(String tweetContent) {
        String normalizedTweet = tweetContent.replace('\n',' ');
        normalizedTweet = normalizedTweet.replace('\r',' ');
        normalizedTweet = normalizedTweet.replace('\"','\'');

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

        return normalizedTweet;
    }

}
