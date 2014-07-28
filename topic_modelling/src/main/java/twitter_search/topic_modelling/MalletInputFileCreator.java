package twitter_search.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import twitter_search.twitter_api_usage.DBManager;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kleiner Klotz on 28.07.2014.
 */
public class MalletInputFileCreator {

    public static void main(String[] args) {
        writeDBContentToInputFile("mallet_input_file.csv");
    }

    private static void writeDBContentToInputFile(String filePath) {
        DBManager dbManager = new DBManager();
        Date sqlDate = new Date(1406174400000L); // 07/24/2014
        Map<Long, String> tweetIdsToContent = dbManager.selectTweetsCreatedAt(sqlDate);

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath), '\t', '\"');
            String[] line = new String[3];
            for (Map.Entry<Long, String> entry : tweetIdsToContent.entrySet()) {
                line[0] = entry.getKey().toString();
                line[1] = "X";
                line[2] = normalizeTweetContent(entry.getValue());
                csvWriter.writeNext(line);
            }
        } catch (IOException e) {
            System.out.print("InputFile for Topic Modelling could not be created.");
            e.printStackTrace();
        }
    }

    private static String normalizeTweetContent(String tweetContent) {
        String normalizedTweet = tweetContent.replace('\n',' ');
        normalizedTweet = normalizedTweet.replace('\r',' ');
        normalizedTweet = normalizedTweet.replace('\"','\'');
        return normalizedTweet;
    }
}
