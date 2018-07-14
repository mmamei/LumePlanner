package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.City;
import model.POI;
import org.springframework.security.access.method.P;

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

        convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\Reggio_Emilia_Mercoledi_Rosa\\pois\\itiner_re_18l_tappe.txt",",from:LUME",false,3);
        convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\Reggio_Emilia_Mercoledi_Rosa\\pois\\itiner_re_25l_tappe.txt",",from:LUME",false,3);

        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\BolognaMappePlurali\\pois\\ITINER_BO_1_TAPPE.txt",",from:LUME",false,2);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\BolognaMappePlurali\\pois\\ITINER_BO_2_TAPPE.txt",",from:LUME",false,2);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\BolognaMappePlurali\\pois\\ITINER_BO_3_TAPPE.txt",",from:LUME",false,2);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\BolognaMappePlurali\\pois\\ITINER_BO_4_TAPPE.txt",",from:LUME",false,2);

        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\Maranello\\pois\\pois.csv",",from:Maranello",false,1);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\Reggio_nell'Emilia\\pois\\pois19092017.csv",",from:Reggio-Emilia",false,1);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\Modena\\pois\\pois20092017.csv",",from:Modena",false,1);
        //convert("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\extra-pois\\extra-pois.csv",",from:Lume",true,1);
        System.out.println("Done");
    }


    public static List<POI> getPOIList1(String file, String source) throws Exception {
        List<POI> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;

        // POI Constructor:
        // String place_id, double lat, double lon, String display_name, String category, String type,
        // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo_url, String description, String www

        //mrnl3,attractions,Museo Ferrari,10.861428,44.529739,http://museo.ferrari.com/it/
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("//") || line.isEmpty()) continue;
            System.out.println(line);
            String[] e = line.split(",");
            String id = e[0];
            String type = e[1];
            String category = CSV_TO_CAT.get(type);

            if (category == null) {
                System.out.println(line);
                category = "attractions";
            }
            float importance = Float.parseFloat(e[2]);
            String name = e[3];
            double lon = Double.parseDouble(e[4]);
            double lat = Double.parseDouble(e[5]);
            String img = null;
            String www = null;
            String desc = null;
            if (e.length > 6) {
                if (e[6].startsWith("http")) {
                    www = e[6];
                    if (e.length > 7) {
                        desc = e[7];
                        for (int i = 8; i < e.length; i++)
                            desc += "," + e[i];
                    }
                } else {
                    desc = e[6];
                    for (int i = 7; i < e.length; i++)
                        desc += "," + e[i];

                }
            }
            list.add(new POI(id, lat, lon, name + source, category, type, importance, "", 0, "ok", "ok", 0, img, desc, www));
        }
        br.close();
        return list;
    }

    public static List<POI> getPOIList2(String file, String source) throws Exception {
        List<POI> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] e = line.trim().split(",");
            String img = br.readLine().split(":")[1].trim();
            img = "http://5.144.174.222:8080/DITA/img/pois/bologna_plurali/"+img+".jpg";
            String id = e[0].trim();

            if(id.equals("id36")) {
                System.out.println(id);
            }

            String type = e[1].trim();
            String category = CSV_TO_CAT.get(type);
            if (category == null) {
                System.out.println(line);
                category = "attractions";
            }
            String name = e[2].trim();
            double lat = Double.parseDouble(e[3].trim());
            double lon = Double.parseDouble(e[4].trim());
            String desc = "";
            String www = null;
            if(e.length > 5) {
                System.out.println(line);
                for(int i=5; i<e.length-1;i++)
                    desc += ", "+e[i];

                if(desc.startsWith(", ")) desc = desc.substring(2);
                String last = e[e.length-1].trim();
                if(last.startsWith("http")) www = last;
                else desc += ", "+last;
            }
            list.add(new POI(id, lat, lon, name + source, category, type, 1, "", 0, "ok", "ok", 0, img, desc, www));

        }
        br.close();
        return list;
    }

    public static List<POI> getPOIList3(String file, String source) throws Exception {
        List<POI> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] e = line.trim().split(",");

            String id = e[0].trim();

            String type = e[1].trim();
            String category = CSV_TO_CAT.get(type);
            if (category == null) {
                System.out.println(line);
                category = "attractions";
            }
            String name = e[2].trim();

            double lat = Double.parseDouble(e[3].trim());
            double lon = Double.parseDouble(e[4].trim());
            String desc = "";
            for(int i=5; i<e.length-1;i++)
                desc += ", "+e[i];
            if(desc.startsWith(", ")) desc = desc.substring(2);


            list.add(new POI(id, lat, lon, name + source, category, type, 1, "", 0, "ok", "ok", 0, "", desc, ""));

        }
        br.close();
        return list;
    }



    public static void convert(String file, String source, boolean divide_by_city, int ctype) throws Exception {


        List<POI> list = null;
        if(ctype == 1) list = getPOIList1(file,source);
        if(ctype == 2) list = getPOIList2(file,source);
        if(ctype == 3) list = getPOIList3(file,source);

        System.out.println("TOTAL POIS IN FILE = " + list.size());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (divide_by_city) {
            // map to cities
            List<City> cities = City.getInstance();
            Map<String, List<POI>> hm = new HashMap<>();
            for (City city : cities)
                hm.put(city.getName(), new ArrayList<POI>());

            for (POI p : list) {
                for (City cp : cities) {
                    if (cp.contains(p.getGeometry().getCoordinates().getLatitude(), p.getGeometry().getCoordinates().getLongitude()))
                        hm.get(cp.getName()).add(p);
                }
            }

            String fileName = new File(file.substring(0, file.length() - 3) + "json").getName();
            for (String city : hm.keySet()) {
                System.out.println(city + " ==> " + hm.get(city).size());
                for(POI p : hm.get(city)) {
                    System.out.print(p.getPlace_id()+",");
                }
                System.out.println();


                if (hm.get(city).size() > 0) {
                    File f = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\" + city + "\\pois\\" + fileName);
                    System.out.println(f.getAbsolutePath());
                    mapper.writeValue(f, hm.get(city));
                }
            }
        }
        else {
            File f = new File(file.substring(0,file.length()-3)+"json");
            mapper.writeValue(f,list);
        }
    }
}
