package org.twittersearch.app.twitter_api_usage;

import au.com.bytecode.opencsv.CSVReader;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mandy Roick on 29.09.2014. According to example by Maximilian Jenders
 */
public class TwitterApiCustodian {

    private static TwitterApiCustodian instance = null;

    private List<TwitterWrapper> _wrappers;
    private String accessDataFileName;

    public static synchronized TwitterApiCustodian getInstance() {
        if(instance == null) {
            instance = new TwitterApiCustodian("input_data/twitter_keys.csv");
        }
        return instance;
    }

    public TwitterApiCustodian(String authenticationDataFilename) {
        _wrappers = new LinkedList<TwitterWrapper>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(authenticationDataFilename), '\t', '\"', 1);

            String[] nextLine;
            ConfigurationBuilder cb;
            TwitterWrapper tw;

            while((nextLine = csvReader.readNext()) != null) {
                cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(nextLine[0])
                        .setOAuthConsumerSecret(nextLine[1])
                        .setOAuthAccessToken(nextLine[2])
                        .setOAuthAccessTokenSecret(nextLine[3]);
                tw = new TwitterWrapper(new TwitterFactory(cb.build()).getInstance());
                _wrappers.add(tw);
            }

        } catch (java.io.IOException e) {
            System.out.println("Could not read authentication File.");
            e.printStackTrace();
        }

    }

    /***
     * Returns a Twitter account with which the specified API call can be made
     * (i.e., that still has the capacity to do so). If no Twitter account has
     * API calls left, the Thread will sleep until the rate limit has been reset
     * @param apiCall the API call for which a Twitter wrapper is to be found
     * @return the according TwitterWrapper object
     */
    public synchronized TwitterWrapper getFreeTwitterWrapper(String apiCall) {
        TwitterWrapper wrapper = null;
        //by default, most API calls have a reset window of 15 minutes = 900 seconds
        int secondsUntilReset = 900;

        for (TwitterWrapper tw : _wrappers) {
            if (tw.getRemainingAPICalls(apiCall) > 0) {
                wrapper = tw;
                break;
            }
            //Finds the minimum wait time for API call window reset if no free wrapper can be found
            secondsUntilReset = Math.min(secondsUntilReset, tw.getSecondsUntilReset(apiCall));
        }

        if (wrapper != null) {
            //Put the wrapper at the end of the queue
            _wrappers.remove(wrapper);
            _wrappers.add(wrapper);
            return wrapper;
        } else {
            //No free wrapper could be found
            System.out.println("Rate limit exceeded, sleeeping for " + secondsUntilReset + " seconds.");
            try {
                Thread.sleep((secondsUntilReset + 10) * 1000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateWrappers();
            wrapper = getFreeTwitterWrapper(apiCall);
        }
        return wrapper;
    }

    private void updateWrappers() {
        for (TwitterWrapper tw : _wrappers) {
            tw.updateRateLimit();
        }
    }
}
