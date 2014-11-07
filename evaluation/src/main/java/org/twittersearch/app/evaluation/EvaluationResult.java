package org.twittersearch.app.evaluation;

/**
 * Created by Mandy Roick on 06.11.2014.
 */
public class EvaluationResult {

    private int TP;
    private int TN;
    private int FP;
    private int FN;

    public EvaluationResult(int TP, int TN, int FP, int FN) {
        this.TP = TP;
        this.TN = TN;
        this.FP = FP;
        this.FN = FN;
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
        result += "TP: " + this.TP + ", ";
        result += "TN: " + this.TN + ", ";
        result += "FP: " + this.FP + ", ";
        result += "FN: " + this.FN + ", ";
        result += "F-Measure: " + this.fMeasure();
        return result;
    }
}
