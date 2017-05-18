package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CityProperties;
import model.POI;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static services.CategoriesDictionary.CSV_TO_CAT;

/**
 * Created by marco on 21/04/2017.
 */


public class CSVConvert {


    public static void main(String[] args) throws Exception {
        convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\Maranello\\pois\\pois.csv");
        System.out.println("Done");
    }

    public static void convert(String file) throws Exception {






        List<POI> list = new ArrayList<>();




        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

        String line;



        // POI Constructor:
        // String place_id, double lat, double lon, String display_name, String category, String type,
        // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo_url, String description, String www

        //mrnl3,attractions,Museo Ferrari,10.861428,44.529739,http://museo.ferrari.com/it/
        while((line = br.readLine())!=null) {
            line = line.trim();
            if(line.startsWith("//") || line.isEmpty()) continue;
            //System.out.println(line);
            String[] e = line.split(",");
            String id = e[0];
            String type = e[1];
            String category = CSV_TO_CAT.get(type);

            if(category == null)
                System.out.println(line);

            String name = e[2];
            double lon = Double.parseDouble(e[3]);
            double lat = Double.parseDouble(e[4]);
            String www = null;
            String desc = null;
            if(e.length > 5) {
                if(e[5].startsWith("http")) {
                    www = e[5];
                    if(e.length > 6) {
                        desc = e[6];
                        for (int i = 7; i < e.length; i++)
                            desc += "," + e[i];
                    }
                }
                else {
                    desc = e[5];
                    for(int i=6; i<e.length;i++)
                        desc += "," + e[i];

                }
            }
            list.add(new POI(id, lat, lon, name, category, type, 10, "", 0, "ok", "ok", 0, null, desc, www));
        }
        br.close();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File f = new File(file.substring(0,file.length()-3)+"json");
        mapper.writeValue(f,list);
    }
}