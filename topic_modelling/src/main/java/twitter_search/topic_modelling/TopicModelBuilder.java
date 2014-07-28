package twitter_search.topic_modelling;

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
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{Pd}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stop_lists/stop_words_mysql.txt"), "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = null;
        try {
            fileReader = new InputStreamReader(new FileInputStream(new File("mallet_input_file.csv")), "UTF-8");
            instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)"),
                    3, 2, 1)); // data, label, name fields

            int numTopics = 1000;
            ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
            model.addInstances(instances);
            model.setNumThreads(2);
            model.setNumIterations(50);
            model.estimate();

            // The data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();

            FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
            LabelSequence topics = model.getData().get(0).topicSequence;

            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            for (int position = 0; position < tokens.getLength(); position++) {
                out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
            }
            //System.out.println(out);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
