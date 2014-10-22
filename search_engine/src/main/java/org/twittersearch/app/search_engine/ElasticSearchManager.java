package org.twittersearch.app.search_engine;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

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

        ElasticSearchIndexer.indexingFromDB("2014-10-09", client);

        //GetResponse getResponse = client.prepareGet("twitter", "tweet", "518695575102164993")
        //        .execute()
        //        .actionGet();
        //System.out.println(getResponse.getSource());

        ElasticSearchManager esManager = new ElasticSearchManager();
        esManager.searchFor("politics");
        client.close();
    }

    public ElasticSearchManager() {
        Settings settings = ImmutableSettings.settingsBuilder().build();
        TransportClient transportClient = new TransportClient(settings);
        transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        this.client = transportClient;
    }

    public void searchFor(String query) {
        SearchResponse response = this.client.prepareSearch().setQuery(QueryBuilders.matchQuery("content", query))
                                                             .setFrom(0)
                                                             .setSize(60).execute().actionGet();
        SearchHits searchHits = response.getHits();
        System.out.println(searchHits.totalHits());
        for (SearchHit searchHit : searchHits) {
            //System.out.println(searchHit.getId());
            System.out.println(searchHit.getId() + ": " + searchHit.getScore() + " : " + searchHit.getSource());
        }
    }
}
