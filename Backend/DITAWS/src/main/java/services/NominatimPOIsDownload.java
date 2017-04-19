package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.CityProp;
import io.RESTController;
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
    private static String [] POI_CATEGORIES = {"attractions", "monuments", "museums", "eating", "parks", "resting", "historical_sites", "religious_sites"}; //"lifestyle"
    private static final String DATA_PATH = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\nominatim\\";

    public static void main(String[] args) {
        String city = "ReggioEmilia";
        String bbox = CityProp.getInstance().get(city).getBbox();
        String out_file = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\nominatim.json";
        download(city,bbox,out_file);
    }

    public static void download(String city, String bbox, String out_file) {
        try {
            Logger logger = Logger.getLogger(RESTController.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);


            Map<String, Integer> ratings = new HashMap<>();//new SocialPulse().loadStars();
            List<POI> modified = new ArrayList<>();
            for (int i=0; i<POI_CATEGORIES.length;i++) {
                File file = new File(DATA_PATH + POI_CATEGORIES[i]);
                logger.info("=======> " + file.getAbsolutePath());

                BufferedReader file_br = new BufferedReader(new FileReader(file));
                String rowCategory = "";

                while ((rowCategory = file_br.readLine()) != null) {

                    logger.info("=======================> " + rowCategory);

                    String typeCategory = rowCategory.split(",")[0]; //[0] contains the type of category

                    double visiting_time = 0d;

                    if (!POI_CATEGORIES[i].equals("resting"))
                        visiting_time = Double.parseDouble(rowCategory.split(",")[1]); //[1] contains the visiting time (not for hotels)

                    //osm API call to get POIs of a certain category type within the bbox
                    URL url = new URL(NOMINATIM_URL + "search?q=" + typeCategory + "&format=json&viewbox=" + bbox + "&bounded=1&limit=1000");
                    logger.info(url.toString());
                    BufferedReader url_br = new BufferedReader(new InputStreamReader(url.openStream()));

                    //parsing the POIs in the json returned by the API call
                    JSONArray parsed = new JSONArray(url_br.readLine());

                    //loop over the POIs retrieved
                    for (int j=0;j<parsed.length();j++) {
                        JSONObject currentJPOI = (JSONObject) parsed.get(j);
                        POI currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
                        currentPOI.setCategory(POI_CATEGORIES[i]);
                        if (!POI_CATEGORIES[i].equals("resting")) {
                            currentPOI.setVisiting_time(visiting_time);
                            if (ratings.containsKey(currentPOI.getPlace_id())) {
                                currentPOI.setRating(ratings.get(currentPOI.getPlace_id()));
                            }
                            //to add: opening times and days
                        }

                        modified.add(currentPOI);
                    }
                    url_br.close();
                }
                file_br.close();
            }
            mapper.writeValue(new File(out_file), modified);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
