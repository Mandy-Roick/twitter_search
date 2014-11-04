package org.twittersearch.app.twitter_api_usage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Mandy Roick on 04.11.2014.
 */
public class UrlCollectorMax {

    public static void main(String[] args) {
        String date = "2014-10-21";
        if(args.length >= 1) {
            date = args[0];
        }

        crawlURLs(date);
    }

    public static void crawlURLs(String date) {
        DBManager dbManager = new DBManager();
        int crawlCounter = 0;

        System.out.println("Reloading URL list. Crawled URLs: " + crawlCounter);
        List<CrawlUrl> urls = dbManager.selectUrlsWithoutTextForDate(date);
        Collections.shuffle(urls);
        List<CrawlUrl> failedURLs = new ArrayList<CrawlUrl>();
        HashMap<String, Long> domainDelays = new HashMap<String, Long>();
        int crawlDelayMS = 5000;

        while (true) {
            System.out.println("Retrying URLs. Crawled URLs: " + crawlCounter);
            for (CrawlUrl url : urls) {
                try {
                    String currentDomain = url.getDomain();
                    if (domainDelays.containsKey(currentDomain) &&
                            (domainDelays.get(currentDomain) < System.currentTimeMillis())) {
                        //have to wait to crawl
                        failedURLs.add(url);
                    } else {
                        String content = crawlURL(url.getURL());
                        crawlCounter++;
                        if (crawlCounter % 100 == 0) System.out.println("Crawled " + crawlCounter + " URLs so far. Failed: " + failedURLs.size());

                        if (content != null) {
                            url.setContent(content);
                            dbManager.writeUrlContentToDB(url.getTweetId(), url.getUrlString(), true, url.getContent());
                        } else {
                            failedURLs.add(url);
                        }
                        domainDelays.put(currentDomain, System.currentTimeMillis() + crawlDelayMS);
                    }
                } catch (MalformedURLException e) {
                    System.out.println("Malformed URL.");
                    System.out.println(e.toString());
                    //e.printStackTrace();
                }
            }
            //try failed URLs again
            System.out.println("1 Crawl-Session complete. Crawled " + crawlCounter +" urls with " + failedURLs.size() + " errors.");
            urls = failedURLs;
            failedURLs = new ArrayList<CrawlUrl>();
        }
    }

    private static String crawlURL(URL url) {
        //crawl site
        StringBuilder text = new StringBuilder();
        String line = "";
        String content = "";
        BufferedReader in = null;
        HttpURLConnection urlconn = null;
        try {
            urlconn = (HttpURLConnection) url.openConnection();
            urlconn.setConnectTimeout(100000);
            urlconn.setReadTimeout(10000);
            urlconn.setInstanceFollowRedirects(true);
            urlconn.setRequestMethod("GET");
            urlconn.connect();
            Charset charset = Charset.forName("UTF-8");
            in = new BufferedReader(new InputStreamReader(urlconn.getInputStream(), charset));
            while ((line = in.readLine()) != null) text.append(line);

            content = text.toString();
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout for URL " + url.toString());
        } catch (Exception e) {
            content = null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                    urlconn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content;
    }
}
