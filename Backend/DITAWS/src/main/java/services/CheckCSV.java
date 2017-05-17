package services;

import model.POI;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static services.CategoriesDictionary.CSV_TO_CAT;
import static util.Misc.haverDist;

/**
 * Created by marco on 15/05/2017.
 */
public class CheckCSV {

    public static void main(String[] args) throws Exception {
        check("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\Maranello\\pois\\pois.csv");
        System.out.println("Done");
    }

    public static void check(String file) throws Exception {
        List<POI> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;

        // POI Constructor:
        // String place_id, double lat, double lon, String display_name, String category, String type,
        // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo_url, String description, String www

        //mrnl3,attractions,Museo Ferrari,10.861428,44.529739,http://museo.ferrari.com/it/
        while((line = br.readLine())!=null) {
            line = line.trim();
            if (line.startsWith("//") || line.isEmpty()) continue;
            String[] e = line.split(",");
            String id = e[0];
            String type = e[1];
            String category = CSV_TO_CAT.get(type);
            String name = e[2];
            double lon = Double.parseDouble(e[3]);
            double lat = Double.parseDouble(e[4]);

            POI poi = new POI(id, lat, lon, name, category, type, 10, "", 0, "ok", "ok", 0, null, null, null);


            boolean trovato = false;
            for(POI p: list) {
                if(!poi.getPlace_id().equals(p.getPlace_id()))
                if(haverDist(new double[]{p.getGeometry().getCoordinates().getLatitude(),p.getGeometry().getCoordinates().getLongitude()},
                             new double[]{poi.getGeometry().getCoordinates().getLatitude(),poi.getGeometry().getCoordinates().getLongitude()}) < 20) {
                    trovato = true;
                    System.out.println(poi.getPlace_id()+" close to "+p.getPlace_id());
                }
            }
            if(!trovato)
            list.add(poi);



        }
        br.close();
    }


}
