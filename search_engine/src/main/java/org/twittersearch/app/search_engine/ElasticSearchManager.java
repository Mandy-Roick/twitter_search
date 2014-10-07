package org.twittersearch.app.search_engine;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * Created by Mandy Roick on 02.10.2014.
 */
public class ElasticSearchManager {

    public static void main(String[] args) {
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        ElasticSearchIndexer.indexing("2014-10-04", client);

        GetResponse getResponse = client.prepareGet("twitter", "tweet", "518433665978605570")
                .execute()
                .actionGet();
        System.out.println(getResponse.getField("content"));

        SearchResponse response = client.prepareSearch().setQuery(QueryBuilders.queryString("politics")).execute().actionGet();
        SearchHits searchHits = response.getHits();
        for (SearchHit searchHit : searchHits) {
            System.out.println(searchHit.getId());
            //System.out.println(searchHit.field("content").getValue());
        }
        node.close();
    }
}
