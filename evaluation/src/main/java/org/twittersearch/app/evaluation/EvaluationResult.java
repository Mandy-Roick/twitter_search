package org.twittersearch.app.evaluation;

import java.util.List;

/**
 * Created by Mandy Roick on 06.11.2014.
 */
public class EvaluationResult {

    private int TP = 0;
    private int TN = 0;
    private int FP = 0;
    private int FN = 0;
    private List<Integer> positions = null;

    public EvaluationResult(int TP, int TN, int FP, int FN) {
        this.TP = TP;
        this.TN = TN;
        this.FP = FP;
        this.FN = FN;
    }

    public EvaluationResult(List<Integer> positions) {
        this.positions = positions;
    }

    public EvaluationResult(int TP, int TN, int FP, int FN, List<Integer> positions) {
        this(TP, TN, FP, FN);
        this.positions = positions;
    }

    public double fMeasure() {
        double precision = precision();
        double recall = recall();
        return 2*(precision*recall)/(precision+recall);
    }

    public double recall() {
        return TP /(double) (TP + FN);
    }

    public double precision() {
        return TP /(double) (TP + FP);
    }

    @Override
    public String toString() {
        String result = "";

        if (this.TP + this.TN + this.FP + this.FN != 0) {
            result += "TP: " + this.TP + ", ";
            result += "TN: " + this.TN + ", ";
            result += "FP: " + this.FP + ", ";
            result += "FN: " + this.FN + ", ";
            result += "F-Measure: " + this.fMeasure();
        }
        if (this.positions != null) {
            for (Integer position : this.positions) {
                result += position + ", ";
            }
        }

        return result;
    }
}
