package org.twittersearch.app.helper;

import au.com.bytecode.opencsv.CSVReader;
import cc.mallet.topics.TopicInferencer;

import java.io.*;
import java.util.*;

/**
 * Created by Mandy Roick on 11.10.2014.
 */
public class FileReaderHelper {

    // FS = file suffix
    public static final String FS_TOP_WORDS = "_top_words.results";
    public static final String FS_TYPE_TOPIC_COUNTS = "_type_topic_counts.results";
    public static final String FS_STEMMING_DICT = "_stemming_dictionary.results";
    public static final String FS_TOPIC_INFERENCER = "_topic_inferencer.results";

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

    public static Map<String, TypeContainer> readTypes(String filePrefix) {
        Map<String, TypeContainer> types = new HashMap<String, TypeContainer>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(filePrefix + FS_TYPE_TOPIC_COUNTS), ' ');
            String[] nextLine;
            TypeContainer type;

            nextLine = csvReader.readNext();
            if (nextLine != null) {
                if (nextLine[0].equals("Beta:")) {
                    // ignore this
                } else {
                    type = readLineToTypeContainer(nextLine);
                    types.put(type.getName(), type);
                }
            }

            while ((nextLine = csvReader.readNext()) != null) {
                type = readLineToTypeContainer(nextLine);
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

    public static BetaAndTypesContainer readTypesAndBeta(String fileName) {
        Map<String, TypeContainer> types = new HashMap<String, TypeContainer>();
        double beta = 0;
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ' ');
            String[] nextLine;
            TypeContainer type;

            nextLine = csvReader.readNext();
            if (nextLine != null) {
                if (nextLine[0].equals("Beta:")) {
                    beta = Double.parseDouble(nextLine[1]);
                } else {
                    type = readLineToTypeContainer(nextLine);
                    types.put(type.getName(), type);
                }
            }

            while ((nextLine = csvReader.readNext()) != null) {
                type = readLineToTypeContainer(nextLine);
                types.put(type.getName(), type);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open Topic File.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not read next line in Topic File.");
            e.printStackTrace();
        }
        return new BetaAndTypesContainer(beta, types);
    }

    private static TypeContainer readLineToTypeContainer(String[] nextLine) {
        TypeContainer type;
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
        return type;
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

    public static Map<Integer, TopicContainer> readTopWords(String filePrefix) {
        Map<Integer, TopicContainer> topWords = new HashMap<Integer, TopicContainer>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(filePrefix + FS_TOP_WORDS), ',', ' ');
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                int topicId = Integer.parseInt(nextLine[0]);
                String[] topWordsForTopic = Arrays.copyOfRange(nextLine, 3, nextLine.length);
                topWords.put(topicId, new TopicContainer(topicId, topWordsForTopic));
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

    public static TopicInferencer readTopicInferencer(String filePrefix) {
        TopicInferencer result = null;

        File file = new File(filePrefix + FS_TOPIC_INFERENCER);
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (TopicInferencer) ois.readObject();
            ois.close();
        } catch (IOException e) {
            System.out.println("Could not read topic inferencer from file " + file.getName());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String, String> readStemmingDictionary(String filePrefix) {
        Map<String, String> stemmingDictionary = null;
        try {
            FileInputStream fis = new FileInputStream(filePrefix + "_stemming_dictionary.results");
            ObjectInputStream ois = new ObjectInputStream(fis);
            stemmingDictionary = (Map<String, String>) ois.readObject();
        } catch (java.io.IOException e) {
            System.out.println("Could not read stemming dictionary from file.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Could not read stemming dictionary because of class incompatibilities.");
            e.printStackTrace();
        }
        return stemmingDictionary;
    }
}
