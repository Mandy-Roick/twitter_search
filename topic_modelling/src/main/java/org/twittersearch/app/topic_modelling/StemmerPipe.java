package org.twittersearch.app.topic_modelling;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Created by Mandy Roick on 04.08.2014.
 */
public class StemmerPipe extends Pipe {

    @Override public Instance pipe(Instance carrier) {
        PorterStemmer stemmer = new PorterStemmer();
        TokenSequence in = (TokenSequence) carrier.getData();

        for (Token token : in) {
            stemmer.setCurrent(token.getText());
            stemmer.stem();
            token.setText(stemmer.getCurrent());
        }

        return carrier;
    }
}
