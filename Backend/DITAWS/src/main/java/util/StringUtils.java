package util;

import java.text.Normalizer;

/**
 * Created by marco on 11/10/2017.
 */
public class StringUtils {

    public static String removeAccent(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static String array2string(double[] x) {
        StringBuffer sb  = new StringBuffer();
        for(int i=0; i<x.length;i++)
            sb.append(","+x[i]);
        return sb.substring(1);
    }

}
