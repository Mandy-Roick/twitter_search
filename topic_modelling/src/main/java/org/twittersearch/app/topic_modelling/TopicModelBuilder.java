package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.twittersearch.app.helper.BetaAndTypesContainer;
import org.twittersearch.app.helper.FileReaderHelper;
import org.twittersearch.app.helper.TopicContainer;
import org.twittersearch.app.helper.TypeContainer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 25.07.2014.
 */
public class TopicModelBuilder {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.set(2014, 9, 20); //Months start with 0 :(
        //c.add(Calendar.DATE, 1); //08 is for him a too large integer number
        learnTopicModel(c);
    }

    public static String learnTopicModel(Calendar c) {
        boolean withSeeding = true;
        String date = sdf.format(c.getTime());
        String inputFileName = "mallet_input_file_" + date + ".csv";

        int numTopics = 200;
        double topicsCutOffPercentage = 0.5/200.0;
        //String filePrefix = "trimmed_tm-" + numTopics + "_" + date + "_wo_seeding";
        String filePrefix = "trimmed_tm-" + numTopics + "_" + date;

        try {

            InstanceList instances = createInstances(date, inputFileName, filePrefix);

            ParallelTopicModelExtension model;

            c.add(Calendar.DATE, -1);
            String yesterdayTypesFileName = "trimmed_tm-" + numTopics + "_" + sdf.format(c.getTime())+ FileReaderHelper.FS_TYPE_TOPIC_COUNTS;
            String yesterdayTopicsFileName = "trimmed_tm-" + numTopics + "_" + sdf.format(c.getTime())+ FileReaderHelper.FS_TOP_WORDS;
            File yesterdayTopicsFile = new File(yesterdayTopicsFileName);
            if (withSeeding && yesterdayTopicsFile.exists()) {
                BetaAndTypesContainer betaAndTypes = FileReaderHelper.readTypesAndBeta(yesterdayTypesFileName);
                Map<Integer, Integer> topicCounts = FileReaderHelper.readTopicCounts(yesterdayTopicsFileName);
                List<Integer> ignoreTopics = calculateIgnoreTopics(topicsCutOffPercentage, topicCounts);

                System.out.println("Number of deleted topics: " + ignoreTopics.size());
                System.out.print("Ignore Topics: ");
                for(Integer ignoreTopic : ignoreTopics) {
                    System.out.print(ignoreTopic + " ");
                }
                System.out.println("\n");

                Map<String, double[]> typeTopicProbabilites = calculateTypesSmoothedTopicCounts(betaAndTypes, ignoreTopics, numTopics);
                model = new ParallelTopicModelExtension(typeTopicProbabilites, numTopics, 0.01*numTopics, 0.05);
            } else {
                model = new ParallelTopicModelExtension(numTopics, 0.01*numTopics, 0.05);
            }
            c.add(Calendar.DATE, 1);

            model.addInstances(instances);
            model.setNumThreads(2);
            model.setNumIterations(500);
            model.setSymmetricAlpha(false);
            model.estimate();

            //model.printTopicWordWeights(new File(filePrefix + "_words.results"));
            model.printTypeTopicCounts(new File(filePrefix + "_type_topic_counts.results"));
            //model.printDocumentTopics(new File(filePrefix + "_document_topics.results"));
            TopicContainer[] topics = extractTopicScores(numTopics, model);
            writeTopWordsToCsv(filePrefix, model, topics);
            writeTopicInferencer(filePrefix, model.getInferencer());
            writeTypeTopicProbabilities(filePrefix, model, topics);

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

        return filePrefix;
    }

    private static void writeTypeTopicProbabilities(String filePrefix, ParallelTopicModelExtension model, TopicContainer[] topics) {
        try {
            model.printTopicWordProbabilities(new PrintWriter(new File(filePrefix + "_wordprobabilities.results")));
        } catch (IOException e) {
            System.out.println("Could not write word probabilities.");
            e.printStackTrace();
        }
    }

    private static List<Integer> calculateIgnoreTopics(double cutOffPercentage, Map<Integer, Integer> topicCounts) {
        List<Integer> ignoreTopics = new LinkedList<Integer>();
        int overallTopicCountSum = 0;
        for (Integer count : topicCounts.values()) {
            overallTopicCountSum += count;
        }

        double cutOffTopicCount = overallTopicCountSum*cutOffPercentage;
        for (Map.Entry<Integer, Integer> topicCount : topicCounts.entrySet()) {
            if (topicCount.getValue() < (cutOffTopicCount)) {
                ignoreTopics.add(topicCount.getKey());
            }
        }
        return ignoreTopics;
    }

    private static Map<String, double[]> calculateTypesSmoothedTopicCounts(BetaAndTypesContainer betaAndTypes, List<Integer> ignoreTopics, int numTopics) {
        Map<String, double[]> result = new HashMap<String, double[]>();
        for (Map.Entry<String, TypeContainer> type : betaAndTypes.getTypes().entrySet()) {
            double[] smoothedCounts = new double[numTopics];
            Arrays.fill(smoothedCounts, betaAndTypes.getBeta());
            for (int topicIndex = 0; topicIndex < numTopics; topicIndex++) {
                smoothedCounts[topicIndex] += type.getValue().getTopicCountForTopic(topicIndex);
            }
            //TODO: throw out topics which have topic count < 5% of all topic counts

            result.put(type.getKey(), smoothedCounts);
        }
        return result;
    }

    public static String learnTopicModel3Days(Calendar endDateCalendar) {
        int numberOfDays = 4;

        String date = sdf.format(endDateCalendar.getTime());
        String inputFileName = "mallet_input_file_" + date + ".csv";

        int numTopics = 200;
        String filePrefix = "five-day-tm-" + numTopics + "_" + date;

        try {
            List<String> inputFileNames = new LinkedList<String>();
            inputFileNames.add(inputFileName);
            for (int i = 0; i < numberOfDays-1; i++) {
                endDateCalendar.add(Calendar.DATE, -1);
                date = sdf.format(endDateCalendar.getTime());
                inputFileName = "mallet_input_file_" + date + ".csv";
                inputFileNames.add(inputFileName);
            }

            ParallelTopicModelExtension model = new ParallelTopicModelExtension(numTopics, 0.01*numTopics, 0.05);
            InstanceList instances = createInstances(date, inputFileNames, filePrefix);
            model.addInstances(instances);

            model.setNumThreads(2);
            model.setNumIterations(500);
            model.setSymmetricAlpha(false);
            model.estimate();

            //model.printTopicWordWeights(new File(filePrefix + "_words.results"));
            model.printTypeTopicCounts(new File(filePrefix + "_type_topic_counts.results"));
            //model.printDocumentTopics(new File(filePrefix + "_document_topics.results"));

            TopicContainer[] topics = extractTopicScores(numTopics, model);

            writeTopWordsToCsv(filePrefix, model, topics);
            writeTopicInferencer(filePrefix, model.getInferencer());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePrefix;

    }

    //---------------------------- read input data and put it in an instance list --------------------------------------

    private static InstanceList createInstances(String date, String inputFileName, String filePrefix) throws IOException {
        List<String> inputFileNames = new LinkedList<String>();
        inputFileNames.add(inputFileName);

        return createInstances(date, inputFileNames, filePrefix);
    }

    private static InstanceList createInstances(String date, List<String> inputFileNames, String filePrefix) throws IOException {
        // We need two instanceIterator, because of the word frequencies
        List<Iterator<Instance>> inputIterators1 = new LinkedList<Iterator<Instance>>();
        List<Iterator<Instance>> inputIterators2 = new LinkedList<Iterator<Instance>>();
        List<Reader> fileReaders = new LinkedList<Reader>();
        for (String inputFileName : inputFileNames) {
            File inputFile = new File(inputFileName);
            if (!inputFile.exists()) {
                MalletInputFileCreator.writeDBContentToInputFile(inputFileName, date);
            }

            // We need two instanceIterator, because of the word frequencies
            Reader fileReader1 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            fileReaders.add(fileReader1);
            Reader fileReader2 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            fileReaders.add(fileReader2);
            Iterator<Instance> inputIterator1 = new CsvIterator(fileReader1, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            inputIterators1.add(inputIterator1);
            Iterator<Instance> inputIterator2 = new CsvIterator(fileReader2, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            inputIterators2.add(inputIterator2);
        }
        InstanceList instances = createInstanceList(inputIterators1, inputIterators2, filePrefix);

        for (Reader fileReader : fileReaders) {
            fileReader.close();
        }

        return instances;
    }

    public static InstanceList createInstanceList(Iterator<Instance> inputIterator1, Iterator<Instance> inputIterator2, String filePrefix) throws IOException {
        List<Iterator<Instance>> inputIterators1 = new LinkedList<Iterator<Instance>>();
        inputIterators1.add(inputIterator1);
        List<Iterator<Instance>> inputIterators2 = new LinkedList<Iterator<Instance>>();
        inputIterators2.add(inputIterator2);
        return createInstanceList(inputIterators1, inputIterators2, filePrefix, true);
    }

    private static InstanceList createInstanceList(List<Iterator<Instance>> inputIterators1, List<Iterator<Instance>> inputIterators2, String filePrefix) throws IOException {
        return createInstanceList(inputIterators1, inputIterators2, filePrefix, true);
    }

    // create instance list with multiple input files
    private static InstanceList createInstanceList(List<Iterator<Instance>> inputIterators1, List<Iterator<Instance>> inputIterators2, String filePrefix, boolean writeFrequencies) throws IOException {
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

        for (Iterator<Instance> inputIterator1 : inputIterators1) {
            initialInstances.addThruPipe(inputIterator1);
        }

        // Create the final instanceList which contains no words which are in less than 10 tweets.
        ArrayList<Pipe> prunedPipeList = new ArrayList<Pipe>();
        prunedPipeList.add(new CharSequenceLowercase());
        prunedPipeList.add(new CharSequence2TokenSequence(Pattern.compile("[#\\p{L}][\\p{L}\\p{Pd}\\p{M}']+\\p{L}")));
        prunedPipeList.add(mySqlStopWordsPipe);
        StemmerPipe stemmer = new StemmerPipe();
        prunedPipeList.add(stemmer);

        Map<String, Integer> documentFrequencies = getDocumentFrequencies(initialInstances);
        Map<String, Integer> wordFrequencies = documentFrequencies;//getWordFrequencies(initialInstances);
        List<String> cutOffWords = getCutOffWords(wordFrequencies, 10);
        TokenSequenceRemoveStopwords cutOffStopWordsPipe = new TokenSequenceRemoveStopwords();
        cutOffStopWordsPipe.addStopWords(cutOffWords.toArray(new String[cutOffWords.size()]));
        prunedPipeList.add(cutOffStopWordsPipe);

        prunedPipeList.add(new TokenSequence2FeatureSequence());

        InstanceList prunedInstances = new InstanceList (new SerialPipes(prunedPipeList));

        for (Iterator<Instance> inputIterator2 : inputIterators2) {
            prunedInstances.addThruPipe(inputIterator2);
        }

        //removeSmallDocuments(prunedInstances, 2);

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

    private static void removeSmallDocuments(InstanceList prunedInstances, int minSize) {
        List<Integer> indicesOfSmallDocuments = new LinkedList<Integer>();
        for (int i = 0; i < prunedInstances.size(); i++) {
            if (((FeatureSequence) prunedInstances.get(i).getData()).size() < minSize) {
                indicesOfSmallDocuments.add(i);
            }
        }
        for (Integer index : indicesOfSmallDocuments) {
            prunedInstances.remove(index);
        }
    }

    //--------------------------------- retrieve data from topic model and write to files ------------------------------

    private static TopicContainer[] extractTopicScores(int numTopics, ParallelTopicModelExtension model) {
        TopicContainer[] topics = new TopicContainer[numTopics];

        for (int i = 0; i < topics.length; i++) {
            topics[i] = new TopicContainer(i, 0, 0);
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

    private static void writeTopWordsToCsv(String filePrefix, ParallelTopicModelExtension model) throws IOException {
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

    private static void writeTopWordsToCsv(String filePrefix, ParallelTopicModelExtension model, TopicContainer[] topicScores) throws IOException {
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

    private static void writeTopicInferencer(String filePrefix, TopicInferencer topicInferencer) {
        File file = new File(filePrefix + FileReaderHelper.FS_TOPIC_INFERENCER);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(topicInferencer);
            oos.close();
        } catch (IOException e) {
            System.out.println("Could not write topic inferencer to file.");
            e.printStackTrace();
        }

    }

    private static void writeStemmingDictionaryFile(String filePrefix, Map<String, String> stemmingDictionary) {
        File file = new File(filePrefix + FileReaderHelper.FS_STEMMING_DICT);
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

    private static Map<String, Integer> getDocumentFrequencies(InstanceList instances) {
        // Collect frequencies of each word.
        Map<String, Integer> documentFrequencies = new HashMap<String, Integer>();
        FeatureSequence data;
        String word;
        Integer count;
        Set<String> currentWords;

        for(Instance instance : instances) {
            data = (FeatureSequence) instance.getData();
            currentWords = new HashSet<String>();
            for (int i = 0; i < data.size(); i++) {
                word = data.get(i).toString();
                currentWords.add(word);
            }
            for (String uniqueWord : currentWords) {
                count = documentFrequencies.get(uniqueWord);
                if (count == null) {
                    documentFrequencies.put(uniqueWord, 1);
                }
                else {
                    documentFrequencies.put(uniqueWord, count + 1);
                }
            }
        }
        return documentFrequencies;
    }
}
