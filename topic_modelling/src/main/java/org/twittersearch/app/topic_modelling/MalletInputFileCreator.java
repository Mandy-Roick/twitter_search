package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import org.twittersearch.app.twitter_api_usage.DBManager;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.Map;

/**
 * Created by kleiner Klotz on 28.07.2014.
 */
public class MalletInputFileCreator {

    public static void main(String[] args) {
        writeDBContentToInputFile("mallet_input_file_2014-07-30.csv");
    }

    private static void writeDBContentToInputFile(String filePath) {
        DBManager dbManager = new DBManager();
        String date = "2014-07-23";
        Map<Long, String> tweetIdsToContent = dbManager.selectTweetsCreatedAt(date);

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath), '\t', '\"');
            String[] line = new String[3];
            for (Map.Entry<Long, String> entry : tweetIdsToContent.entrySet()) {
                line[0] = entry.getKey().toString();
                line[1] = "X";
                line[2] = normalizeTweetContent(entry.getValue());
                csvWriter.writeNext(line);
            }
            csvWriter.close();
        } catch (IOException e) {
            System.out.print("InputFile for Topic Modelling could not be created.");
            e.printStackTrace();
        }
    }

    private static String normalizeTweetContent(String tweetContent) {
        String normalizedTweet = tweetContent.replace('\n',' ');
        normalizedTweet = normalizedTweet.replace('\r',' ');
        normalizedTweet = normalizedTweet.replace('\"','\'');

        normalizedTweet = normalizedTweet.replaceAll("@(\\w+)[\\s:\\p{Po}]"," ");
        normalizedTweet = normalizedTweet.replaceAll("@(\\w+)$","");
        normalizedTweet = normalizedTweet.replaceAll("(\\w+)://(\\S+)\\s"," ");
        normalizedTweet = normalizedTweet.replaceAll("(\\w+)://(\\S+)$"," ");
        return normalizedTweet;
    }
}
