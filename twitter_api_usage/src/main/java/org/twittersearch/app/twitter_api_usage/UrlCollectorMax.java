package org.twittersearch.app.twitter_api_usage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Mandy Roick on 04.11.2014.
 */
public class UrlCollectorMax {

    private static final String[] ignoreUrls = {"http://t.co/RLTg4qjBJp","http://t.co/mWSt9ZJFgF","http://t.co/E3qajsmMkp","http://t.co/4WyMTyLiWR","http://t.co/6vkvlngtt7","http://t.co/CznpZqpM0L",
                                                "http://t.co/Imq8Bmqlpx","http://t.co/BOOjP2uQNH","http://t.co/facI0BKtqH","http://t.co/MU6l3569qF","http://t.co/pZJE2VPV61","http://t.co/pZJE2VPV61",
                                                "http://t.co/X9qW97XEUg","http://t.co/FuMGW2gUoz","http://t.co/pxcqhZoqbg","http://t.co/Ak1gdyHoCB","http://t.co/xCMIUtFWxO"};

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

        //System.out.println("Reloading URL list. Crawled URLs: " + crawlCounter);
        List<CrawlUrl> urls = dbManager.selectUrlsWithoutTextForDate(date);

        for (String ignoreUrl : ignoreUrls) {
            CrawlUrl ignoreCrawlUrl = null;
            for (CrawlUrl url : urls) {
                if (url.urlEquals(ignoreUrl)) {
                    ignoreCrawlUrl = url;
                    break;
                }
            }
            if (ignoreCrawlUrl != null)
                urls.remove(ignoreCrawlUrl);
        }

        Collections.shuffle(urls);
        List<CrawlUrl> failedURLs = new ArrayList<CrawlUrl>();
        HashMap<String, Long> domainDelays = new HashMap<String, Long>();
        int crawlDelayMS = 5000;
        int counterOfTrulyFailed = 0;

        while (true) {
            System.out.println("Retrying URLs. Crawled URLs: " + crawlCounter);
            for (CrawlUrl url : urls) {
                try {
                    String currentDomain = url.getDomain();
                    Long currentDomainDelay = domainDelays.get(currentDomain);
                    if ((currentDomainDelay != null) &&
                            (currentDomainDelay < System.currentTimeMillis())) {
                        //have to wait to crawl
                        //failedURLs.add(url);
                        System.out.println("Wait for domain to be available again.");
                        Thread.sleep(System.currentTimeMillis() - currentDomainDelay);
                    } else {
                        String content = crawlURL(url.getURL());
                        crawlCounter++;
                        if (crawlCounter % 20 == 0) {
                            java.util.Date currentDate= new java.util.Date();
                            System.out.println(new Timestamp(currentDate.getTime()) + ": Crawled " + crawlCounter +
                                    " URLs so far. Failed because of errors: " + counterOfTrulyFailed + ". Domains: " + domainDelays.size());

                        }

                        if (content != null) {
                            url.setContent(content);
                            dbManager.writeUrlContentToDB(url.getTweetId(), url.getUrlString(), true, url.getContent());
                        } else {
                            counterOfTrulyFailed++;
                            //failedURLs.add(url);
                        }
                        domainDelays.put(currentDomain, System.currentTimeMillis() + crawlDelayMS);
                    }
                } catch (MalformedURLException e) {
                    System.out.println("Malformed URL.");
                    System.out.println(e.toString());
                    //e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("Interrupt while waiting for domain.");
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
        System.out.println(url);
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
            if (urlconn.getHeaderField("Content-Type").contains("audio/mpeg")) {
                System.out.println("null");
                return null;
            }
            //System.out.println("Content-Type: " + urlconn.getHeaderField("Content-Type"));
            //System.out.println("length: " + urlconn.getHeaderField("Content-Length"));
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
