package org.twittersearch.app.topic_modelling;

/*
Copyright (c) 2013 miberk

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Adapted by Mandy Roick on 07.10.2014.
*/

import vagueobjects.ir.lda.online.matrix.Matrix;
import vagueobjects.ir.lda.online.matrix.Vector;
import vagueobjects.ir.lda.tokens.Documents;

import java.util.*;

/**
 * Displays topics discovered by Online LDA. Topics are sorted by
 * their statistical importance.
 */
public class OnlineLDAExtensionResult {

    /**Number of terms per each tokens to show*/
    static int NUMBER_OF_TOKENS = 15;
    private final Matrix lambda;
    private final double perplexity;
    private final Documents documents;
    private final int totalTokenCount;

    /**
     *
     * @param docs  - documents in the batch
     * @param D   - total number of documents in corpus
     * @param bound  - variational bound
     * @param lambda   - variational distribution q(beta|lambda)
     */
    public OnlineLDAExtensionResult(Documents docs, int D, double bound, Matrix lambda) {
        this.lambda = lambda;
        this.documents = docs;
        this.totalTokenCount = docs.getTokenCount();
        double perWordBound = (bound * docs.size())  / D / totalTokenCount;
        this.perplexity = Math.exp(-perWordBound);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Perplexity estimate: ").append(perplexity).append("\n");
        int numTopics = lambda.getNumberOfRows();
        int numTerms = Math.min(NUMBER_OF_TOKENS, lambda.getNumberOfColumns());
        for (int k = 0; k < numTopics; ++k) {
            Vector termScores = lambda.getRow(k);

            // ToDo: sortTopicTerms should
            for(OnlineLDAExtensionTuple tuple:  sortTopicTerms(termScores, numTerms)){
                tuple.addToString(sb, documents);
            }

            sb.append('\n');
        }
        sb.append("\n");
        return sb.toString();
    }

    private Collection<OnlineLDAExtensionTuple> sortTopicTerms(Vector termScores, int numTerms) {
        return ((ArrayList<OnlineLDAExtensionTuple>) sortTopicTerms(termScores)).subList(0, numTerms);
    }

    private Collection<OnlineLDAExtensionTuple> sortTopicTerms(Vector termScores) {
        Set<OnlineLDAExtensionTuple> tuples = new TreeSet<OnlineLDAExtensionTuple>();
        double sum=0d;
        for(int i=0; i< termScores.getLength();++i){
            sum += termScores.elementAt(i);
        }

        double [] p = new double[termScores.getLength()];
        for(int i=0; i< termScores.getLength();++i){
            p[i] = termScores.elementAt(i)/sum;
        }


        for(int i=0; i< termScores.getLength();++i){
            OnlineLDAExtensionTuple tuple = new OnlineLDAExtensionTuple(i, p[i]);
            tuples.add(tuple);
        }
        return new ArrayList<OnlineLDAExtensionTuple>(tuples);
    }

    public List<OnlineLDAExtensionTopic> topicScoresAndTopWords(int numberOfTopWords) {
        List<OnlineLDAExtensionTopic> result = new LinkedList<OnlineLDAExtensionTopic>();

        Vector topicScores = lambda.sumByRows();
        double topicScore;
        Collection<OnlineLDAExtensionTuple> topWordTuples;
        Map<String, Double> topWords;
        for (int i = 0; i < topicScores.getLength(); i++) {
            topicScore = topicScores.elementAt(i);

            topWordTuples = this.sortTopicTerms(lambda.getRow(i));
            topWords = new HashMap<String, Double>();
            for (OnlineLDAExtensionTuple topWordTuple : topWordTuples) {
                topWords.put(topWordTuple.getToken(documents), topWordTuple.getValue());
            }

            result.add(new OnlineLDAExtensionTopic(topicScore, topWords));
        }
        return result;
    }
}