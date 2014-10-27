package org.twittersearch.app.helper;

import java.util.Map;

/**
 * Created by Mandy Roick on 27.10.2014.
 */
public class BetaAndTypesContainer {


    private double beta;
    private Map<String, TypeContainer> types;

    public BetaAndTypesContainer(double beta, Map<String, TypeContainer> types) {
        this.beta = beta;
        this.types = types;
    }

    public double getBeta() {
        return beta;
    }

    public Map<String, TypeContainer> getTypes() {
        return types;
    }
}
