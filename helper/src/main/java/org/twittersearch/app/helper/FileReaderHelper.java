package org.twittersearch.app.helper;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mandy Roick on 11.10.2014.
 */
public class FileReaderHelper {


    public static Map<String, String[]> readTopicModel(String fileName) {
        Map<String,String[]> typeTopicCounts = new HashMap<String, String[]>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topicCounts = Arrays.copyOfRange(nextLine, 2, nextLine.length);
                typeTopicCounts.put(nextLine[1], topicCounts);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return typeTopicCounts;
    }

    public static Map<Integer, String[]> readTopWords(String fileName) {
        Map<Integer, String[]> topWords = new HashMap<Integer, String[]>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ',', ' ');
            int topicIndex = 0;
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topWordsForTopic = Arrays.copyOfRange(nextLine, 3, nextLine.length);
                topWords.put(Integer.parseInt(nextLine[0]), topWordsForTopic);
                topicIndex++;
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return topWords;
    }
}
