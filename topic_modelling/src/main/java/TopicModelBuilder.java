import cc.mallet.pipe.*;
import cc.mallet.util.CommandOption;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by kleiner Klotz on 25.07.2014.
 */
public class TopicModelBuilder {

    private void piping() {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords(new File("stop_lists/stop_words_mysql.txt"), "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());
    }
}
