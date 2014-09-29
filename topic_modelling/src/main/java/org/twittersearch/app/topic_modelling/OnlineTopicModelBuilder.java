package org.twittersearch.app.topic_modelling;

import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.twittersearch.app.twitter_api_usage.DBManager;
import vagueobjects.ir.lda.online.OnlineLDA;
import vagueobjects.ir.lda.online.Result;
import vagueobjects.ir.lda.tokens.Documents;
import vagueobjects.ir.lda.tokens.PlainVocabulary;
import vagueobjects.ir.lda.tokens.Vocabulary;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Mandy Roick on 11.09.2014.
 */
public class OnlineTopicModelBuilder {

    private static final int numOfTopics = 200;
    private static final double tau = 1d;
    private static final double kappa = 0.d;//0.8d;
    private static final double alpha = 1.d/ numOfTopics;
    private static final double eta = 1.d/ numOfTopics;
    private OnlineLDA ldaModel;
    private String startingDate;

    public static void main(String[] args) {
        String startDate = "2014-09-23";
        OnlineTopicModelBuilder oTMB = new OnlineTopicModelBuilder(startDate);
        oTMB.updateTopicModel();

        //learnTopicModel();
    }


    public OnlineTopicModelBuilder(String startingDate) {
        this.ldaModel = OnlineTopicModelBuilder.initializeTopicModel(startingDate);
        this.startingDate = startingDate;
    }

    private static void learnTopicModel() {
        String dateSuffix = "2014-09-07";

        int numOfTopics = 50;
        int batchSize = 1000;
        double tau = 1d;
        double kappa = 0.8d;
        double alpha = 1.d/ numOfTopics;
        double eta = 1.d/ numOfTopics;

        try {
            InstanceList instances = createInstanceList(dateSuffix);
            List<String> documents = createDocumentsList(instances);

            String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
            Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));
            OnlineLDA lda = new OnlineLDA(vocabulary.size(), numOfTopics, instances.size(), alpha, eta, tau, kappa);

            for (int i = 0; i*batchSize < documents.size(); ++i) {
                int max = Math.min((i+1)*batchSize,documents.size());
                Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
                Result result = lda.workOn(onlineLDADocuments);
                System.out.println(result);
            }

        } catch (IOException e) {
            System.out.println("Could not read instances.");
            e.printStackTrace();
        }

    }

    public static OnlineLDA initializeTopicModel(String date) {
        int batchSize = 100000;//262144;

        try {

            System.out.println("Create Instance List.");
            InstanceList instances = createInstanceList(date);
                    //TopicModelBuilder.createInstanceList(inputFileName, filePrefix); //fileprefix is needed for writing the wordfrequencies to file

            System.out.println("Create Documents List.");
            List<String> documents = createDocumentsList(instances);

            String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
            Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));
            OnlineLDA lda = new OnlineLDA(vocabulary.size(), numOfTopics, instances.size(), alpha, eta, tau, kappa);

            for (int i = 0; i*batchSize < documents.size(); ++i) {
                System.out.println("Work on Batch number " + i + ".");
                int max = Math.min((i+1)*batchSize,documents.size());
                Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
                Result result = lda.workOn(onlineLDADocuments);
                System.out.println(result);
            }

            return lda;

        } catch (IOException e) {
            System.out.println("Could not read instances.");
            e.printStackTrace();
        }

        return null;
    }

    private static InstanceList createInstanceList(String date) throws IOException{
        //DBManager dbManager = new DBManager();

        //Map<Long, String> tweetIdsToContent = dbManager.selectTweetsCreatedAt(date);
        //Map<Long, List<String>> tweetsHashtags = dbManager.selectTweetsAndHashtagsCreatedAt(date);

        //TweetPreprocessor tweetPreprocessor = new TweetPreprocessor();
        //Map<Long, String> preprocessedTweets = tweetPreprocessor.preprocessTweets(tweetIdsToContent, tweetsHashtags);

        //List preprocessedTweetContents = new ArrayList(preprocessedTweets.values());

        String inputFileName = "mallet_input_file_" + date + ".csv";
        MalletInputFileCreator malletInputFileCreator = new MalletInputFileCreator(date);
        malletInputFileCreator.writeDBContentToInputFile(inputFileName);

        Reader fileReader1 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
        Reader fileReader2 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
        Iterator<Instance> inputIterator1 =  new CsvIterator(fileReader1, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
        Iterator<Instance> inputIterator2 =  new CsvIterator(fileReader2, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
        InstanceList instances = TopicModelBuilder.createInstanceList(inputIterator1, inputIterator2);
        fileReader1.close();
        fileReader2.close();

        return instances;
    }

    private static List<String> createDocumentsList(InstanceList instances) {
        List<String> documents = new ArrayList<String>();
        for (Instance instance : instances) {
            FeatureSequence documentFS = (FeatureSequence) instance.getData();
            String document = "";
            for (int i = 0; i < documentFS.size(); i++) {
                document += documentFS.get(i) + " ";
            }
            documents.add(document);
        }
        return documents;
    }

    private void updateTopicModel() {
        DBManager dbManager = new DBManager();
        Long highestIdFromStartingDate = dbManager.selectHighestIdFromDate(this.startingDate);
        Map<Long, String> newTweets = dbManager.selectTweetsAboveID(highestIdFromStartingDate);

    }
}
