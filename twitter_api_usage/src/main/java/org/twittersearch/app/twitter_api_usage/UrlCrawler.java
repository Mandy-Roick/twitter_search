package org.twittersearch.app.twitter_api_usage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 * Created by Mandy Roick on 20.07.2014.
 */
public class UrlCrawler implements Callable<String> {
    private String urlString;

    UrlCrawler(String url) {
        this.urlString = url;
    }

    @Override
    public String call() throws IOException {
        URL url = new URL(this.urlString);
        URLConnection urlConnection = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String urlText = "";
        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            urlText += inputLine;
        }
        br.close();

        return urlText;
    }
}
