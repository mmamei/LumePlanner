package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.RESTController;
import model.CityProperties;
import model.POI;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 14/04/2017.
 */
public class NominatimPOIsDownload {

    private static final String NOMINATIM_URL = "http://nominatim.openstreetmap.org/";
    private static final String NOMINATIM_KEYS = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\nominatimKeys.csv";


    public static void main(String[] args) {
        for(CityProperties cp: CityProperties.getInstance("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv"))
            go(cp);
    }

    public static void go(CityProperties cp) {

        String bbox = cp.getBbox();
        String out_file = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+cp.getName()+"\\pois\\nominatim.json";
        if(new File(out_file).exists())
            System.out.println(out_file+" Already Exists");
        else
            download(cp.getName(),bbox,out_file);
        System.out.println("Completed "+cp.getName());
    }

    public static void download(String city, String bbox, String out_file) {
        try {
            Logger logger = Logger.getLogger(RESTController.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

            List<POI> modified = new ArrayList<>();

            BufferedReader file_br = new BufferedReader(new FileReader(NOMINATIM_KEYS));
            String line;
            while((line = file_br.readLine())!=null) {
                if(line.startsWith("//")) continue;
                String[] e = line.split(":");
                String poi_category = e[0];
                String[] typesAndvtime = e[1].split(",");
                for(int i=0; i<typesAndvtime.length;i=i+2) {
                    String typeCategory = typesAndvtime[i];
                    double visiting_time = Double.parseDouble(typesAndvtime[i+1]);
                    URL url = new URL(NOMINATIM_URL + "search?q=" + typeCategory + "&format=json&viewbox=" + bbox + "&bounded=1&limit=1000");
                    logger.info(url.toString());
                    BufferedReader url_br = new BufferedReader(new InputStreamReader(url.openStream()));
                    JSONArray parsed = new JSONArray(url_br.readLine());
                    for (int j=0;j<parsed.length();j++) {
                        JSONObject currentJPOI = (JSONObject) parsed.get(j);
                        POI currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
                        currentPOI.setCategory(poi_category);
                        if (!poi_category.equals("resting")) {
                            currentPOI.setVisiting_time(visiting_time);
                            //if (ratings.containsKey(currentPOI.getPlace_id())) {
                            //    currentPOI.setRating(ratings.get(currentPOI.getPlace_id()));
                            //}
                            //to add: opening times and days
                        }
                        modified.add(currentPOI);
                    }
                    url_br.close();
                }
            }
            file_br.close();
            mapper.writeValue(new File(out_file), modified);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
