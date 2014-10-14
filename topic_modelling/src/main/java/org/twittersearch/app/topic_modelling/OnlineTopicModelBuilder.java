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
    private static final double kappa = 0.8d;
    private static final double alpha = 1.d/ numOfTopics;
    private static final double eta = 1.d/ numOfTopics;
    private static final int batchSize = 100000;//262144;

    private OnlineLDAExtension ldaModel;
    private OnlineLDAExtensionResult currentResult;
    private String startingDate;

    public static void main(String[] args) {
        String startDate = "2014-10-04";
        OnlineTopicModelBuilder oTMB = new OnlineTopicModelBuilder(startDate);
        oTMB.writeCurrentResultToCsv(startDate + "_online_lda_result.csv");
        System.out.println("Done with date: " + startDate);

        updateLDA("2014-10-05", oTMB);
        updateLDA("2014-10-06", oTMB);
        updateLDA("2014-10-07", oTMB);
        updateLDA("2014-10-08", oTMB);
        updateLDA("2014-10-09", oTMB);
        //updateLDA("2014-10-10", oTMB);

        //learnTopicModel();
    }

    private static void updateLDA(String date, OnlineTopicModelBuilder oTMB) {
        oTMB.updateTopicModelByDate(date);
        oTMB.writeCurrentResultToCsv(date + "_online_lda_result.csv");
        System.out.println("--------------------------- Done with date: " + date + " --------------------------------");
    }

    private void writeCurrentResultToCsv(String fileName) {
        this.currentResult.writeToCsv(fileName, 20);
    }

    public OnlineTopicModelBuilder(String startingDate) {
        initializeTopicModel(startingDate);
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

        InstanceList instances = createInstanceList(dateSuffix);
        List<String> documents = createDocumentsList(instances);

        String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
        Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));
        OnlineLDAExtension lda = new OnlineLDAExtension(numOfTopics, instances.size(), alpha, eta, tau, kappa, vocabulary);

        for (int i = 0; i*batchSize < documents.size(); ++i) {
            int max = Math.min((i+1)*batchSize,documents.size());
            Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
            OnlineLDAExtensionResult result = lda.workOn(onlineLDADocuments);
            System.out.println(result);
        }

    }

    public void initializeTopicModel(String date) {
        System.out.println("Create Instance List.");
        String inputFileName = "mallet_input_file_" + date + ".csv";
        MalletInputFileCreator.writeDBContentToInputFile(inputFileName, date);
        InstanceList instances = createInstanceList(inputFileName);
                //TopicModelBuilder.createInstanceList(inputFileName, filePrefix); //fileprefix is needed for writing the wordfrequencies to file

        System.out.println("Create Documents List.");
        List<String> documents = createDocumentsList(instances);

        String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
        Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));
        this.ldaModel = new OnlineLDAExtension(numOfTopics, instances.size(), alpha, eta, tau, kappa, vocabulary);

        for (int i = 0; i*batchSize < documents.size(); ++i) {
            System.out.println("Work on Batch number " + i + ".");
            int max = Math.min((i+1)*batchSize,documents.size());
            Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
            this.currentResult = this.ldaModel.workOn(onlineLDADocuments);
            //System.out.println(this.currentResult);
        }

    }

    private static InstanceList createInstanceList(String inputFileName) {
        //DBManager dbManager = new DBManager();

        //Map<Long, String> tweetIdsToContent = dbManager.selectTweetsCreatedAt(date);
        //Map<Long, List<String>> tweetsHashtags = dbManager.selectTweetsAndHashtagsCreatedAt(date);

        //TweetPreprocessor tweetPreprocessor = new TweetPreprocessor();
        //Map<Long, String> preprocessedTweets = tweetPreprocessor.preprocessTweets(tweetIdsToContent, tweetsHashtags);

        //List preprocessedTweetContents = new ArrayList(preprocessedTweets.values());

        try {
            Reader fileReader1 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            Reader fileReader2 = new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8");
            Iterator<Instance> inputIterator1 =  new CsvIterator(fileReader1, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            Iterator<Instance> inputIterator2 =  new CsvIterator(fileReader2, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1); // data, label, name fields
            InstanceList instances = TopicModelBuilder.createInstanceList(inputIterator1, inputIterator2);
            fileReader1.close();
            fileReader2.close();

            return instances;

        } catch (IOException e) {
            System.out.println("Could not read mallet input file while initializing.");
            e.printStackTrace();
        }

        return null;
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
        Map<Long, List<String>> newTweetsHashtags = dbManager.selectTweetsHashtagsAboveID(highestIdFromStartingDate);

        //create mallet input file or at least normalize tweets
        String inputFileName = "mallet_input_file_starting_id_" + highestIdFromStartingDate + ".csv"; //Instead of writing to file, probably use a StringBuilder
        MalletInputFileCreator.writeTweetsToInputFile(inputFileName, newTweets, newTweetsHashtags);
        InstanceList instances = createInstanceList(inputFileName);
        List<String> documents = createDocumentsList(instances);

        String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
        Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));

        for (int i = 0; i*batchSize < documents.size(); ++i) {
            System.out.println("Work on Batch number " + i + ".");
            int max = Math.min((i+1)*batchSize,documents.size());
            Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
            this.currentResult = ldaModel.workOn(onlineLDADocuments, vocabulary);
            //System.out.println(this.currentResult);
        }

    }


    private void updateTopicModelByDate(String date) {
        //create mallet input file or at least normalize tweets
        String inputFileName = "mallet_input_file_" + date + ".csv";
        MalletInputFileCreator.writeDBContentToInputFile(inputFileName, date);
        InstanceList instances = createInstanceList(inputFileName);
        List<String> documents = createDocumentsList(instances);

        String[] malletDataAlphabet = (String[]) instances.getDataAlphabet().toArray(new String[instances.getDataAlphabet().size()]);
        Vocabulary vocabulary = new PlainVocabulary(Arrays.asList(malletDataAlphabet));

        for (int i = 0; i*batchSize < documents.size(); ++i) {
            System.out.println("Work on Batch number " + i + ".");
            int max = Math.min((i+1)*batchSize,documents.size());
            Documents onlineLDADocuments = new Documents(documents.subList(i*batchSize, max), vocabulary);
            this.currentResult = ldaModel.workOn(onlineLDADocuments, vocabulary);
            //System.out.println(this.currentResult);
        }

    }
}
