package services.timdatapipe;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by marco on 16/01/2018.
 */
public class TimeCoverage {

    public static void main(String[] args) throws Exception {

        File dir = new File("D:\\LUME-ER");

        Map<String,Integer> h = new LinkedHashMap<>();

        for(String f: dir.list()) {
            String day = f.split("_")[3]; // Nrealtime_Emilia-Romagna_15_20180116_0845.zip
            System.out.println(day);
            Integer c = h.get(day);
            if(c == null) c = 0;
            h.put(day,c+1);
        }

        PrintWriter out = new PrintWriter(new FileWriter("D:\\LUME-ER-TIME-COVERAGE.csv"));
        for(String day: h.keySet())
            out.println(day+";"+h.get(day));


        out.close();

    }

}
