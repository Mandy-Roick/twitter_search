package org.twittersearch.app.search_engine;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * Created by Mandy Roick on 02.10.2014.
 */
public class ElasticSearchManager {

    public static void main(String[] args) {
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        node.close();
    }
}
