package org.twittersearch.app.search_engine;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Collection;

/**
 * Created by Mandy Roick on 02.10.2014.
 */
public class ElasticSearchManager {

    Client client;

    public static void main(String[] args) {
        //Node node = NodeBuilder.nodeBuilder().local(true).node();
        //Client client = node.client();
        Settings settings = ImmutableSettings.settingsBuilder().build();
        TransportClient client = new TransportClient(settings);
        client = client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        ElasticSearchIndexer.indexFromDB("2014-10-21", client);

        //GetResponse getResponse = client.prepareGet("twitter", "tweet", "518695575102164993")
        //        .execute()
        //        .actionGet();
        //System.out.println(getResponse.getSource());

        //ElasticSearchManager esManager = new ElasticSearchManager();
        //esManager.searchFor("politics");
        client.close();
    }

    public ElasticSearchManager() {
        Settings settings = ImmutableSettings.settingsBuilder().build();
        TransportClient transportClient = new TransportClient(settings);
        transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        this.client = transportClient;
    }

    public void addToIndex(String date) {
        ElasticSearchIndexer.indexFromDBWithUrls(date, this.client);
    }

    public SearchHits searchFor(String query) {
        SearchResponse response = this.client.prepareSearch().setQuery(QueryBuilders.matchQuery("content", query))
                                                             .setFrom(0)
                                                             .setSize(60).execute().actionGet();
        return response.getHits();
    }

    public SearchHits searchFor(String query, String date) {
        // TODO: use the date and search only in tweets from that date => need to google how elastic search handles dates
        FilterBuilder filterBuilder = FilterBuilders.termFilter("created_at", date);
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(query, "content", "url_content");
        FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);

        SearchResponse response = this.client.prepareSearch().setQuery(filteredQueryBuilder)
                .setFrom(0)
                .setSize(60).execute().actionGet();
        return response.getHits();
    }

    public SearchHits searchForInSample(String query, Collection<String> sampledIDs) {
        FilterBuilder filterBuilder = FilterBuilders.idsFilter("tweet").addIds(sampledIDs.toArray(new String[sampledIDs.size()]));
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(query, "content", "url_content"); // first query, than fields I query on
        FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);

        SearchResponse response = this.client.prepareSearch().setQuery(filteredQueryBuilder)
                .setFrom(0)
                .setSize(60).execute().actionGet();

        return response.getHits();
//        SearchHits searchHits = response.getHits();
//        System.out.println(searchHits.totalHits());
//        for (SearchHit searchHit : searchHits) {
//            //System.out.println(searchHit.getId());
//            System.out.println(searchHit.getId() + ": " + searchHit.getScore() + " : " + searchHit.getSource());
//        }
    }
}
