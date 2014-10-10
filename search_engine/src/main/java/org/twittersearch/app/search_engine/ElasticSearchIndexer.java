package org.twittersearch.app.search_engine;

import au.com.bytecode.opencsv.CSVReader;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import java.io.FileReader;

/**
 * Created by Mandy Roick on 06.10.2014.
 */
public class ElasticSearchIndexer {

    public static void indexing(String date, Client client) {
        String inputFileName = "mallet_input_file_" + date + ".csv";

        try {
            CSVReader inputFileReader = new CSVReader(new FileReader(inputFileName), '\t', '\"');

            String[] nextLine;
            String json;
            IndexResponse indexResponse;
            int counter = 0;
            while ((nextLine = inputFileReader.readNext()) != null) {
                json = TweetsToJSONConverter.convertTweetToJSON(Long.parseLong(nextLine[0]), nextLine[2]);
                indexResponse = client.prepareIndex("twitter", "tweet", nextLine[0]).setSource(json).execute().actionGet();
                if(indexResponse.isCreated()) {
                    if((counter % 1000) == 0) {
                        System.out.println(counter + ": " + indexResponse.getId());
                    }
                    counter++;
                }
                //ToDo: evaluate indexResponse (.isCreated() = false means it was updated, is this interesting for us?)
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not read mallet input file.");
            e.printStackTrace();
        }


    }
}