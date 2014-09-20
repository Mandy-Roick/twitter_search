package org.twittersearch.app.topic_modelling;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import vagueobjects.ir.lda.online.OnlineLDA;
import vagueobjects.ir.lda.online.Result;
import vagueobjects.ir.lda.tokens.Documents;
import vagueobjects.ir.lda.tokens.PlainVocabulary;
import vagueobjects.ir.lda.tokens.Vocabulary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mandy Roick on 11.09.2014.
 */
public class OnlineTopicModelBuilder {

    public static void main(String[] args) {
        learnTopicModel();
    }

    private static void learnTopicModel() {
        String dateSuffix = "_2014-09-07";
        String inputFileName = "mallet_input_file" + dateSuffix + ".csv";

        int numOfTopics = 50;
        int batchSize = 1000;
        double tau = 1d;
        double kappa = 0.8d;
        double alpha = 1.d/ numOfTopics;
        double eta = 1.d/ numOfTopics;


        String filePrefix = "trimmed_tm-" + numOfTopics + dateSuffix;

        try {
            InstanceList instances = TopicModelBuilder.createInstanceList(inputFileName, filePrefix);
            List<String> documents = new ArrayList<String>();
            for (Instance instance : instances) {
                FeatureSequence documentFS = (FeatureSequence) instance.getData();
                String document = "";
                for (int i = 0; i < documentFS.size(); i++) {
                    document += documentFS.get(i) + " ";
                }
                documents.add(document);
            }

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

}
