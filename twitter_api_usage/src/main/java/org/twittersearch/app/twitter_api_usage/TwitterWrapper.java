package org.twittersearch.app.twitter_api_usage;

/**
 * Created by Mandy Roick on 29.09.2014. According to Example by Maximilian Jenders
 */

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.HashMap;
import java.util.Map;

/***
 * This class wraps a Twitter object by also storing information about the used
 * calls and the rate limits the Twitter object has to easily determine whether
 * API calls can be made.
 * @author Max
 *
 */
public class TwitterWrapper {

    Twitter _twitter;
    private Map<String, RateLimitStatus> _rateLimit;
    private Map<String, Integer> _usedCalls;

    public TwitterWrapper(Twitter twitter) {
        _twitter = twitter;

        updateRateLimit();
    }

    /***
     * @return Returns the Twitter object being wrapped
     */
    public Twitter getTwitter() {
        return _twitter;
    }

    /***
     * Updates the rate limit status from Twitter
     */
    public void updateRateLimit() {
        try {
            _rateLimit = _twitter.getRateLimitStatus();
            _usedCalls = new HashMap<String, Integer>();
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    /***
     * Given an API call, returns the number of seconds until the rate limit
     * window resets for the specific API call
     * @param apiCall the API call to be checked
     * @return the number of seconds until the rate limit window is being reset
     */
    public int getSecondsUntilReset(String apiCall) {
        return _rateLimit.get(apiCall).getSecondsUntilReset();
    }

    /***
     * Returns the number of remaining open API calls for a specific call
     * @param apiCall
     * @return the number of calls remaining
     */
    public int getRemainingAPICalls(String apiCall) {
        int remaining = _rateLimit.get(apiCall).getRemaining();
        //if only <= 3 APIcalls remaining in the window, update rate limit to make sure
        //they are still available (if something didnt get logged)
        if (remaining <= 3) updateRateLimit();
        remaining = _rateLimit.get(apiCall).getRemaining();

        int called = _usedCalls.containsKey(apiCall) ? _usedCalls.get(apiCall) : 0;
        if (remaining - called > 0) {
            return remaining - called;
        } else if (_rateLimit.get(apiCall).getSecondsUntilReset() < 0) {
            updateRateLimit();
            return getRemainingAPICalls(apiCall);
        } else {
            return 0;
        }
    }

    private void ensureRateLimitCapacity(String apiCall) {
        int remaining = _rateLimit.get(apiCall).getRemaining();
        int called = _usedCalls.containsKey(apiCall) ? _usedCalls.get(apiCall) : 0;
        if (remaining - called > 0) {
            return;
        } else if (_rateLimit.get(apiCall).getSecondsUntilReset() < 0) {
            updateRateLimit();
        } else {
            try {
                System.out.println("Rate limit exceeded, sleeeping for " + _rateLimit.get(apiCall).getSecondsUntilReset() + " seconds.");
                Thread.sleep((_rateLimit.get(apiCall).getSecondsUntilReset() + 1 ) * 1000);
                updateRateLimit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ensureRateLimitCapacity(apiCall);
    }

    /***
     * Logs the use of an API call. Thereby, the number of used API calls is stored offline and
     * the remaining ones do not have to be queried before making a call
     * @param apiCall the API call that was used
     */
    public void logApiCall(String apiCall) {
        if (_usedCalls.containsKey(apiCall)) {
            _usedCalls.put(apiCall, _usedCalls.get(apiCall) + 1);
        } else {
            _usedCalls.put(apiCall, 1);
        }
    }

    private void handleTwitterException(TwitterException e) {
        System.out.println("Twitter Exception: " + e.getErrorCode() + " " + e.getMessage() + " (" + e.getStatusCode() + ").");
    }
}
