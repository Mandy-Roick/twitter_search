package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 25.07.2014.
 */
public class TopicModelBuilder {

    public static void main(String[] args) {
        String date = "2014-10-10";
        learnTopicModel(date);
    }

    public static void learnTopicModel(String date) {
        String inputFileName = "mallet_input_file_" + date + ".csv";

        int numTopics = 200;
        String filePrefix = "trimmed_tm-" + numTopics + "_" + date;

        try {

            // We need two instanceIterator, because of the word frequencies
            Reader fileReader1 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            Reader fileReader2 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            Iterator<Instance> inputIterator1 =  new CsvIterator(fileReader1, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            Iterator<Instance> inputIterator2 =  new CsvIterator(fileReader2, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            InstanceList instances = createInstanceList(inputIterator1, inputIterator2, filePrefix);
            fileReader1.close();
            fileReader2.close();

            ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.01*numTopics, 0.05);
            model.addInstances(instances);
            model.setNumThreads(2);
            model.setNumIterations(500);
            model.setSymmetricAlpha(false);
            model.estimate();

            //model.printTopicWordWeights(new File(filePrefix + "_words.results"));
            model.printTypeTopicCounts(new File(filePrefix + "_type_topic_counts.results"));
            //model.printDocumentTopics(new File(filePrefix + "_document_topics.results"));
            //TopicModelDiagnostics tmd = new TopicModelDiagnostics(model,20);
            //TopicModelDiagnostics.TopicScores topicScores = tmd.getTokensPerTopic(model.tokensPerTopic);
            //double[] scores = topicScores.scores;
            //int counter = 0;
            //for(double score : scores) {
            //    System.out.println(counter + ": " + score);
            //    counter++;
            //}

            TopicContainer[] topics = extractTopicScores(numTopics, model);

            writeTopWordsToCsv(filePrefix, model, topics);



            //model.getInferencer().writeInferredDistributions(instances, new File(filePrefix + "_distributions.results"),
            //                                                 500, 50, 50, 0.01, 50);

            // The data alphabet maps word IDs to strings
//            Alphabet dataAlphabet = instances.getDataAlphabet();
            //System.out.println("dataAlphabet:");
            //System.out.println(dataAlphabet.toString());
// TODO: use this for topicScores -> iterate over instances or use typeTopicCounts
//            FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
//            LabelSequence topics = model.getData().get(0).topicSequence;
            //topics.getFeatures(); -> int[] -> for each word, which topic was assigned

//            Formatter out = new Formatter(new StringBuilder(), Locale.US);
//            for (int position = 0; position < tokens.getLength(); position++) {
//                out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
//            }
//            System.out.println(out);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static TopicContainer[] extractTopicScores(int numTopics, ParallelTopicModel model) {
        TopicContainer[] topics = new TopicContainer[numTopics];

        for (int i = 0; i < topics.length; i++) {
            topics[i] = new TopicContainer(i);
        }

        for (int typeIndex = 0; typeIndex < model.numTypes; typeIndex++) {
            int[] topicCounts = model.typeTopicCounts[typeIndex];
            int weight;
            int topicIndex = 0;
            while (topicIndex < topicCounts.length && topicCounts[topicIndex] > 0) {
                // TopicCounts encodes the topic as well as the count, therefore, we need bitwise operations.
                int currentTopic = topicCounts[topicIndex] & model.topicMask; // isolate the topic
                weight = topicCounts[topicIndex] >> model.topicBits; // isolate the counts
                topics[currentTopic].addScore(weight);
                topics[currentTopic].addWord(1);
                topicIndex++;
            }
        }

        // Statistics
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (TopicContainer topic : topics) {
            stats.addValue(topic.getNumberOfWords());
        }
        System.out.println("Standard deviation in word counts: " + stats.getStandardDeviation());
        System.out.println("Coefficient of variation in word counts: " + (stats.getStandardDeviation()/(stats.getMean())));

        return topics;
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

    private static void writeTopWordsToCsv(String filePrefix, ParallelTopicModel model, TopicContainer[] topicScores) throws IOException {
        List<TopicContainer> topics = Arrays.asList(topicScores);
        CSVWriter topWordsCsvWriter = new CSVWriter(new FileWriter(filePrefix + "_top_words.results"), ',', ' ');
        Object[][] topWords = model.getTopWords(20);

        assert(topicScores.length == topWords.length);
        for (int topicIndex = 0; topicIndex < topicScores.length; topicIndex++) {
            Object[] topic = topWords[topicIndex];
            String[] line = new String[topic.length];
            for (int i = 0; i < topic.length; i++) {
                line[i] = (String) topic[i];
            }
            topics.get(topicIndex).setTopWords(line);
        }

        Collections.sort(topics);

        for (TopicContainer topic : topics) {
            topWordsCsvWriter.writeNext(topic.getScoreAndTopWordsAsLine());
        }
        //topWordsCsvWriter.writeNext(line);
        topWordsCsvWriter.close();
    }

    private static InstanceList createInstanceList(Iterator<Instance> inputIterator1, Iterator<Instance> inputIterator2, String filePrefix, boolean writeFrequencies) throws IOException {
        //TODO: write less duplicated code!
        // ideas:   - use prune method from FeatureSequence (but needs the creation of a new Alphabet)
        //          - write my own pipe which is able to do this (probably use two pipes -> one to extract frequencies, one to delete less frequent words)
        // Create an initial instanceList which can be used to extract the frequencies of words.
        ArrayList<Pipe> standardPipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        standardPipeList.add(new CharSequenceLowercase());
        //This pattern filters all sequences of at least 3 literals
        standardPipeList.add(new CharSequence2TokenSequence(Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}")));

        TokenSequenceRemoveStopwords mySqlStopWordsPipe = new TokenSequenceRemoveStopwords(new File("stop_lists/stop_words_mysql.txt"), "UTF-8", false, false, false);
        standardPipeList.add(mySqlStopWordsPipe);
        standardPipeList.add(new StemmerPipe());
        standardPipeList.add(new TokenSequence2FeatureSequence());

        InstanceList initialInstances = new InstanceList (new SerialPipes(standardPipeList));

        initialInstances.addThruPipe(inputIterator1);


        // Create the final instanceList which contains no words which are in less than 10 tweets.
        ArrayList<Pipe> prunedPipeList = new ArrayList<Pipe>();
        prunedPipeList.add(new CharSequenceLowercase());
        prunedPipeList.add(new CharSequence2TokenSequence(Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}")));
        prunedPipeList.add(mySqlStopWordsPipe);
        StemmerPipe stemmer = new StemmerPipe();
        prunedPipeList.add(stemmer);

        Map<String, Integer> wordFrequencies = getWordFrequencies(initialInstances);
        List<String> cutOffWords = getCutOffWords(wordFrequencies, 10);
        TokenSequenceRemoveStopwords cutOffStopWordsPipe = new TokenSequenceRemoveStopwords();
        cutOffStopWordsPipe.addStopWords(cutOffWords.toArray(new String[cutOffWords.size()]));
        prunedPipeList.add(cutOffStopWordsPipe);

        prunedPipeList.add(new TokenSequence2FeatureSequence());

        InstanceList prunedInstances = new InstanceList (new SerialPipes(prunedPipeList));
        prunedInstances.addThruPipe(inputIterator2);
        Map<String, String> stemmingDictionary = stemmer.getFinalStemmingDictionary();
        writeStemmingDictionaryFile(filePrefix, stemmingDictionary);

        // sort words after frequencies and write to file if wanted
        List<Map.Entry<String, Integer>> wordFrequenciesList = new LinkedList<Map.Entry<String, Integer>>(wordFrequencies.entrySet());
        Collections.sort(wordFrequenciesList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        if(writeFrequencies) writeWordFrequenciesToCsv(filePrefix, wordFrequenciesList);
        return prunedInstances;
    }

    private static void writeStemmingDictionaryFile(String filePrefix, Map<String, String> stemmingDictionary) {
        File file = new File(filePrefix + "_stemming_dictionary.results");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(stemmingDictionary);
            oos.close();
        } catch (IOException e) {
            System.out.println("Could not write stemming dictionary to file.");
            e.printStackTrace();
        }

    }

    private static InstanceList createInstanceList(Iterator<Instance> inputIterator1, Iterator<Instance> inputIterator2, String filePrefix) throws IOException {
        return createInstanceList(inputIterator1, inputIterator2, filePrefix, true);
    }

    public static InstanceList createInstanceList(Iterator<Instance> inputIterator1, Iterator<Instance> inputIterator2) throws IOException {
        return createInstanceList(inputIterator1, inputIterator2, "", false);
    }

    private static void writeWordFrequenciesToCsv(String filePrefix, List<Map.Entry<String,Integer>> wordFrequenciesList) throws IOException {
        CSVWriter wordFrequencyCsvWriter = new CSVWriter(new FileWriter(filePrefix + "_word_frequencies.results"), ',', ' ');
        String[] line = new String[2];
        for (Map.Entry<String, Integer> wordFrequency : wordFrequenciesList) {
            line[0] = wordFrequency.getKey();
            line[1] = wordFrequency.getValue().toString();
            wordFrequencyCsvWriter.writeNext(line);
        }
        wordFrequencyCsvWriter.close();
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
        // Collect frequencies of each word.
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
