package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import org.twittersearch.app.helper.FileReaderHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.pow;

/**
 * Created by Mandy Roick on 11.10.2014.
 */
public class TopicMatcher {

    public static void main(String[] args) {
        String date1 = "2014-10-20";
        String date2 = "2014-10-21";
        //Map<Integer, String[]> topWords1 = FileReaderHelper.readTopWords("three-day-tm-200_" + date1 + "_top_words.results");
        //Map<Integer, String[]> topWords2 = FileReaderHelper.readTopWords("three-day-tm-200_" + date2 + "_top_words.results");
        Map<Integer, String[]> topWords1 = FileReaderHelper.readTopWords("trimmed_tm-200_" + date1);
        Map<Integer, String[]> topWords2 = FileReaderHelper.readTopWords("trimmed_tm-200_" + date2);
        Map<Integer, Integer> matching = matchTopics(topWords1, topWords2);

        //writeMatchingResultToCsv(date1 + "_and_" + date2 + "_three_days.csv",matching);
        writeMatchingResultToCsv(date1 + "_and_" + date2 + ".csv",matching);
        for (Map.Entry<Integer, Integer> entry : matching.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
    }

    private static void writeMatchingResultToCsv(String fileName, Map<Integer, Integer> matching) {
        try {
            CSVWriter matchingCsvWriter = new CSVWriter(new FileWriter(fileName), ',');
            String[] line = new String[2];
            for (Map.Entry<Integer, Integer> entry : matching.entrySet()) {
                line[0] = entry.getKey().toString();
                line[1] = entry.getValue().toString();
                matchingCsvWriter.writeNext(line);
            }
            matchingCsvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<Integer, Integer> matchTopics(Map<Integer, String[]> topics1, Map<Integer, String[]> topics2) {
        Map<Integer,Integer> mapping = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, String[]> topic1 : topics1.entrySet()) {
            int bestMatch = -1;
            double bestMatchEvaluation = 0;
            double currentMatchEvaluation;
            for (Map.Entry<Integer, String[]> topic2 : topics2.entrySet()) {
                //currentMatchEvaluation = compareTopics(topic1, topic2);
                currentMatchEvaluation = rankBiasedOverlap(topic1.getValue(), topic2.getValue(), 0.98);
                if ((bestMatch == -1) || (currentMatchEvaluation > bestMatchEvaluation)) {
                    bestMatch = topic2.getKey();
                    bestMatchEvaluation = currentMatchEvaluation;
                }
            }

            mapping.put(topic1.getKey(), bestMatch);
        }

        return mapping;
    }

    private static double rankBiasedOverlap(String[] topic1, String[] topic2, double p) {
        assert(topic1.length == topic2.length);

        String word1;
        String word2;
        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();


        List<Double> x_d = new ArrayList<Double>();
        x_d.add(0.0);
        double sum = 0.0;

        for (int i = 0; i < topic1.length; i++) {
            word1 = topic1[i];
            word2 = topic2[i];

            if (word1.equals(word2)) {
                x_d.add(x_d.get(i) + 1.0);
            } else {
                set1.add(word1);
                set2.add(word2);

                x_d.add(x_d.get(i) + (set1.contains(word2) ? 1.0 : 0.0)
                                   + (set2.contains(word1) ? 1.0 : 0.0));

            }
            sum += (x_d.get(i+1))/(double)(i+1) * pow(p, (i+1));
        }

        return (1-p)/p*sum;
    }

    private static double compareTopics(Map.Entry<Integer, String[]> topic1, Map.Entry<Integer, String[]> topic2) {
        int matchingWords = 0;
        for ( String word1 : topic1.getValue()) {
            for (String word2 : topic2.getValue()) {
                if (word1.equals(word2)) matchingWords++;
            }
        }
        return matchingWords;
    }


}
