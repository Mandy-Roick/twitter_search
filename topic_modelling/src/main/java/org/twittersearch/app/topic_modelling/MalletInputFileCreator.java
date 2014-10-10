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

/**
 * Created by Mandy Roick on 28.07.2014.
 */
public class MalletInputFileCreator {

    DBManager dbManager;
    String date;

    public static void main(String[] args) {
        String date = "2014-10-09";
        MalletInputFileCreator.writeDBContentToInputFile("mallet_input_file_" + date + ".csv", date);
    }

    public static void writeDBContentToInputFile(String filePath, String date) {
        DBManager dbManager = new DBManager();
        Map<Long, String> tweetIdsToContent = dbManager.selectTweetsCreatedAt(date);
        Map<Long, List<String>> tweetsHashtags = dbManager.selectTweetsAndHashtagsCreatedAt(date);

        MalletInputFileCreator.writeTweetsToInputFile(filePath, tweetIdsToContent, tweetsHashtags);
    }

    public static void writeTweetsToInputFile(String fileName, Map<Long, String> tweets, Map<Long, List<String>> tweetsHashtags) {
        TweetPreprocessor tweetPreprocessor = new TweetPreprocessor();
        Map<Long, String> preprocessedTweets = tweetPreprocessor.preprocessTweets(tweets, tweetsHashtags);

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(fileName), '\t', '\"');
            String[] line = new String[3];
            int counter = 1;
            for (Map.Entry<Long, String> entry : preprocessedTweets.entrySet()) {
                if (counter % 1000 == 0) {
                    System.out.println(counter);
                }
                counter++;
                line[0] = entry.getKey().toString();
                line[1] = "X";
                line[2] = entry.getValue();
                csvWriter.writeNext(line);
            }
            csvWriter.close();
        } catch (IOException e) {
            System.out.print("InputFile for Topic Modelling could not be created.");
            e.printStackTrace();
        }
    }

}
