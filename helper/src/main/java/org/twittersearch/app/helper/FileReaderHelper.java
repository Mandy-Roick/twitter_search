package org.twittersearch.app.helper;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Mandy Roick on 11.10.2014.
 */
public class FileReaderHelper {


    public static Map<String, String[]> readTopicModelForExpansion(String fileName) {
        Map<String,String[]> typeTopicCounts = new HashMap<String, String[]>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                // Ignores that all the topic counts are still in the format 10:193
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

    public static Map<String, TypeContainer> readTypes(String fileName) {
        Map<String, TypeContainer> types = new HashMap<String, TypeContainer>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            TypeContainer type;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topicCounts = Arrays.copyOfRange(nextLine, 2, nextLine.length);
                Map<Integer, Integer> finalTopicCounts = new HashMap<Integer, Integer>();
                int overallTopicCount = 0;

                String[] topicIndexString;
                for (String topicCount : topicCounts) {
                    topicIndexString = topicCount.split(":");
                    Integer topicIndex = Integer.parseInt(topicIndexString[0]);
                    Integer count = Integer.parseInt(topicIndexString[1]);
                    overallTopicCount += count;
                    finalTopicCounts.put(topicIndex, count);
                }
                type = new TypeContainer(nextLine[1], overallTopicCount, finalTopicCounts);
                types.put(type.getName(), type);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return types;
    }

    public static Map<String, Integer> readTypesTopics(String fileName) {
        Map<String, Integer> typesTopics = new HashMap<String, Integer>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            String currentTopicString;
            while ((nextLine = csvReader.readNext()) != null) {
                currentTopicString = (nextLine[2].split(":"))[0];
                typesTopics.put(nextLine[1], Integer.parseInt(currentTopicString));
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return typesTopics;
    }

    public static Map<Integer, String[]> readTopWords(String fileName) {
        Map<Integer, String[]> topWords = new HashMap<Integer, String[]>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ',', ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] topWordsForTopic = Arrays.copyOfRange(nextLine, 3, nextLine.length);
                topWords.put(Integer.parseInt(nextLine[0]), topWordsForTopic);
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

    public static Map<Integer, Integer> readTopicCounts(String fileName) {
        Map<Integer, Integer> topicCounts = new HashMap<Integer, Integer>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ',', ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                Integer topicIndex = Integer.parseInt(nextLine[0]);
                Integer topicCount = Integer.parseInt(nextLine[1]);
                topicCounts.put(topicIndex, topicCount);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return topicCounts;
    }
}
