package org.twittersearch.app.topic_modelling;

import au.com.bytecode.opencsv.CSVWriter;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import cc.mallet.util.CommandOption;

import java.io.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by kleiner Klotz on 25.07.2014.
 */
public class TopicModelBuilder {

    public static void main(String[] args) {
        piping();
    }

    private static void piping() {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}][\\p{L}\\p{Pd}\\p{M}]+\\p{L}")));
        //This pattern filters all sequences of at least 3 literals (also in urls, in users, ...)
        //TODO: ignore users, urls, ...
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stop_lists/stop_words_mysql.txt"), "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        int numTopics = 10;
        String dateSuffix = "_2014-07-30";
        String filePrefix = "tm-" + numTopics + dateSuffix;

        Reader fileReader = null;
        try {
            fileReader = new InputStreamReader(new FileInputStream(new File("mallet_input_file" + dateSuffix + ".csv")), "UTF-8");
            instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)"),
                    3, 2, 1)); // data, label, name fields

            ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
            model.addInstances(instances);
            model.setNumThreads(2);
            model.setNumIterations(500);
            model.estimate();

            //model.printTopicWordWeights(new File(filePrefix + "words.results"));
            //model.printTypeTopicCounts(new File(filePrefix + "type_topic_counts.results"));
            //model.displayTopWords(10,true);

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

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
