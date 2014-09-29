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
    Object2ObjectMap<String, String[]> splitHashtags;
    //Map<Long, List<String>> tweetsHashtags;
    String date;

    public static void main(String[] args) {
        String date = "2014-09-16";
        MalletInputFileCreator malletInputFileCreator = new MalletInputFileCreator(date);
        malletInputFileCreator.writeDBContentToInputFile("mallet_input_file_" + date + ".csv");
    }

    public MalletInputFileCreator(String date) {
        this.date = date;
        this.dbManager = new DBManager();
        this.splitHashtags = new Object2ObjectOpenHashMap<String, String[]>();
    }

    public void writeDBContentToInputFile(String filePath) {
        Map<Long, String> tweetIdsToContent = this.dbManager.selectTweetsCreatedAt(this.date);
        Map<Long, List<String>> tweetsHashtags = this.dbManager.selectTweetsAndHashtagsCreatedAt(this.date);

        TweetPreprocessor tweetPreprocessor = new TweetPreprocessor();
        Map<Long, String> preprocessedTweets = tweetPreprocessor.preprocessTweets(tweetIdsToContent, tweetsHashtags);

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath), '\t', '\"');
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
