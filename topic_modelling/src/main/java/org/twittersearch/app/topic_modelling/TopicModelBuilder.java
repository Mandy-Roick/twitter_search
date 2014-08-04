package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import cc.mallet.util.CommandOption;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by kleiner Klotz on 25.07.2014.
 */
public class TopicModelBuilder {

    public static void main(String[] args) {
        piping();
    }

    private static void piping() {
        String dateSuffix = "_2014-07-23";
        String inputFileName = "mallet_input_file" + dateSuffix + ".csv";

        int numTopics = 50;
        String filePrefix = "trimmed_tm-" + numTopics + dateSuffix;

        try {
            InstanceList instances = createInstanceList(inputFileName);
            ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
            model.addInstances(instances);
            model.setNumThreads(2);
            model.setNumIterations(500);
            model.estimate();

            model.printTopicWordWeights(new File(filePrefix + "words.results"));
            model.printTypeTopicCounts(new File(filePrefix + "type_topic_counts.results"));
            writeTopWordsToCsv(filePrefix, model);

            // The data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();
            //System.out.println("dataAlphabet:");
            //System.out.println(dataAlphabet.toString());

            FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
            LabelSequence topics = model.getData().get(0).topicSequence;

            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            for (int position = 0; position < tokens.getLength(); position++) {
                out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
            }
            System.out.println(out);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeTopWordsToCsv(String filePrefix, ParallelTopicModel model) throws IOException {
        CSVWriter topWordsCsvWriter = new CSVWriter(new FileWriter(filePrefix + "_top_words.results"), ',', ' ');
        Object[][] topWords = model.getTopWords(20);
        for (Object[] topic : topWords) {
            String[] line = new String[topic.length];
            for (int i = 0; i < topic.length; i++) {
                line[i] = (String) topic[i];
            }
            topWordsCsvWriter.writeNext(line);
        }
        topWordsCsvWriter.close();
    }

    private static InstanceList createInstanceList(String inputFileName) throws IOException {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        //This pattern filters all sequences of at least 3 literals (also in urls, in users, ...)
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}")));

        TokenSequenceRemoveStopwords stopWordsPipe = new TokenSequenceRemoveStopwords(new File("stop_lists/stop_words_mysql.txt"), "UTF-8", false, false, false);
        pipeList.add(stopWordsPipe);
        pipeList.add(new StemmerPipe());
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList initialInstances = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
        initialInstances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1)); // data, label, name fields
        fileReader.close();

        Map<String, Integer> wordFrequencies = getWordFrequencies(initialInstances);
        List<String> cutOffWords = getCutOffWords(wordFrequencies, 10);
        stopWordsPipe.addStopWords(cutOffWords.toArray(new String[cutOffWords.size()]));

        InstanceList prunedInstances = new InstanceList (new SerialPipes(pipeList));
        fileReader = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
        prunedInstances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1)); // data, label, name fields
        fileReader.close();
        return prunedInstances;
    }

    private static List<String> getCutOffWords(Map<String, Integer> wordFrequencies, int cutOff) {
        List<String> cutOffWords = new ArrayList<String>();
        for (Map.Entry<String, Integer> wordFrequency : wordFrequencies.entrySet()) {
            if (wordFrequency.getValue() < cutOff) {
                cutOffWords.add(wordFrequency.getKey());
            }
        }
        return cutOffWords;
    }

    private static Map<String, Integer> getWordFrequencies(InstanceList instances) {
        // Collect frequencies of all each word.
        Map<String, Integer> wordFrequencies = new HashMap<String, Integer>();
        FeatureSequence data;
        String word;
        Integer count;

        for(Instance instance : instances) {
            data = (FeatureSequence) instance.getData();
            for (int i = 0; i < data.size(); i++) {
                word = data.get(i).toString();
                count = wordFrequencies.get(word);
                if (count == null) {
                    wordFrequencies.put((String) word, 1);
                }
                else {
                    wordFrequencies.put((String) word, count + 1);
                }
            }
        }
        return wordFrequencies;
    }
}
