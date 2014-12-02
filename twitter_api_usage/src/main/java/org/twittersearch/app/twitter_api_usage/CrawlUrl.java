package org.twittersearch.app.twitter_api_usage;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mandy Roick on 04.11.2014.
 */
public class CrawlUrl {

    private Long tweetId;
    private String urlString;
    private URL url = null;
    private String content = null;

    public CrawlUrl(String urlString, Long tweetId) {
        this.urlString = urlString;
        this.tweetId = tweetId;
    }

    public URL getURL() throws MalformedURLException {
        if (this.url == null) {
            this.url = new URL(this.urlString);
        }
        return this.url;
    }

    public String getDomain() throws MalformedURLException {
        return this.getURL().getHost();
    }

    public Long getTweetId() {
        return tweetId;
    }

    public String getUrlString() {
        return urlString;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean urlEquals(String url) {
        return (this.urlString.equals(url));
    }

}
