package org.twittersearch.app.topic_modelling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kleiner Klotz on 01.08.2014.
 */
public class RegexManager {
    public static void main(String[] args) {

        String tweet = "{\"id\":524147578901102592,\"content\":\"\"@Emmanuel_makina @austinmusamali @JacksonKizito @Aswo_Moli politics of perception!\"\",\"evaluation_flag\":null,\"created_at\":\"2014-10-20\"}";

        String url = "http://t.co/1hYdDxCoSR";
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            System.out.println(domain);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Pattern pattern = Pattern.compile("[\\p{L}][\\p{L}\\p{Pd}\\p{M}]+\\p{L}");
        System.out.println(pattern.matches("[\\p{L}][\\p{L}\\p{Pd}\\p{M}]+\\p{L}", "don`t"));

        Pattern nonSpecialCharacterPattern = Pattern.compile("(.*)");
        Matcher nonSpecialCharacterMatcher = nonSpecialCharacterPattern.matcher("#American #sweetheart #Debut - 9/1 #Event\u0085 #countrymusic from #Kentucky #Atlanta #newmusicsoon >>  ");
        String nonSpecialCharacterString = "";
        if(nonSpecialCharacterMatcher.matches()) {
            System.out.println(nonSpecialCharacterMatcher.groupCount());
            for (int i = 0; i < nonSpecialCharacterMatcher.groupCount(); i++) {
                nonSpecialCharacterString += nonSpecialCharacterMatcher.group(i);
            }
            System.out.println(nonSpecialCharacterString);
        }

        String test = "#American #sweetheart #Debut - 9/1 #Event\u0085 #countrymusic from #Kentucky #Atlanta #newmusicsoon >>  ";
        System.out.println(test);

        System.out.println(test);

        nonSpecialCharacterPattern = Pattern.compile("(.*)");
        nonSpecialCharacterMatcher = nonSpecialCharacterPattern.matcher(test);
        nonSpecialCharacterString = "";
        if(nonSpecialCharacterMatcher.matches()) {
            System.out.println(nonSpecialCharacterMatcher.groupCount());
            for (int i = 0; i < nonSpecialCharacterMatcher.groupCount(); i++) {
                nonSpecialCharacterString += nonSpecialCharacterMatcher.group(i);
            }
            System.out.println(nonSpecialCharacterString);
        }
    }
}
