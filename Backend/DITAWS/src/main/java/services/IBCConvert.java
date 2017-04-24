package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CityProperties;
import model.POI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by marco on 21/04/2017.
 */

/*
This program takes as input a csv file from:
http://www.patrimonioculturale-er.it/webgis/
And split it according to the cities we have

0 WKT
1 gid
2 codice
3 nome
4 denominazioni
5 proprietario_pubblico
6 provincia
7 comune
8 diocesi
9 frazione
10 indirizzo
11 tipo_tutela
12 tipo_proprieta
13 tipologie_cronologie
14 categoria
15 eta
16 eta_attestazione
17 provvedimenti_date
18 stato+
19 data_cambio_stato
20 link_vir
21 data_upd
22 lon
23 lat
24 x
25 y
26 guidarossa
27 link_fai
*/

public class IBCConvert {

    public static  boolean GUIDA_ROSSA_ONLY = true;

    public static void main(String[] args) throws Exception {

        Map<String,String> ibc2nominatim = new HashMap<>();


        // POI Categories
        // attractions
        // monuments
        // museums
        // parks
        // historical
        // religious

        ibc2nominatim.put("HolderOfArchives","museums");
        ibc2nominatim.put("Library","museums");
        ibc2nominatim.put("Museum","museums");

        List<CityProperties> cities = CityProperties.getInstance("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv");

        Map<String,List<POI>> hm = new HashMap<>();
        for(CityProperties city : cities)
            hm.put(city.getName(),new ArrayList<POI>());



        String ibc_dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\ibc\\";
        String[] files = new String[]{"HolderOfArchives","Library","Museum"};
        for(String f: files) {
            System.out.println("Processing IBC: "+f);
            BufferedReader br = new BufferedReader(new FileReader(ibc_dir+f+".csv"));
            String line;

            String[] header = br.readLine().split("\t"); // skip header
            //for(int i=0; i<header.length;i++)
            //System.out.println(i+" "+header[i]);


            // POI Constructor:
            // String place_id, double lat, double lon, String display_name, String category, String type,
            // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating

            NumberFormat format = NumberFormat.getInstance(Locale.ITALY);
            int tot = 0;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("\",\"","\";\"");
                //System.out.println(line);
                String[] e = line.replaceAll("\"","").split(";");
                double[] lonlat = new double[]{format.parse(e[2]).doubleValue(),format.parse(e[1]).doubleValue()};

                for(CityProperties cp: cities) {
                    if (cp.contains(lonlat[1], lonlat[0])) {
                        hm.get(cp.getName()).add(new POI(e[1], lonlat[1], lonlat[0], "ibc: " + e[0], ibc2nominatim.get(f), f, 10, "", 0, "ok", "ok", 0));
                        tot++;
                    }
                }
            }
            System.out.println("tot = "+tot);

            br.close();
        }

        System.out.println("Processing IBC: Trees");

        // Processing tree file
        int tot = 0;
        Map<String,List<Object>> trees = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(ibc_dir+"Tree.csv"));
        String line;
        br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            String other;
            while (!(other = br.readLine()).contains("\",\""))
                line = line + " " + other;
            line = line + " " + other;
            if(line.endsWith(",")) line = line.substring(0,line.length()-1);
            line = line.replaceAll("\",\"","\";\"").replaceAll("\"","");
            System.out.println(line);
            String[] e = line.split(";");

            String key = e[0]+" "+e[1]+""+e[2];
            List<Object> v = trees.get(key);
            if(v == null) {
                v  = new ArrayList<>();
                v.add(e[0]);
                v.add(Double.parseDouble(e[1]));
                v.add(Double.parseDouble(e[2]));
                trees.put(key,v);
            }
            if(e.length>3) v.add(e[3]);
        }
        br.close();

        for(List<Object> v: trees.values()) {
            double[] lonlat = new double[]{(double)v.get(2),(double)v.get(1)};
            for(CityProperties cp: cities) {
                if (cp.contains(lonlat[1], lonlat[0])) {
                    hm.get(cp.getName()).add(new POI(v.get(0)+" "+tot, lonlat[1], lonlat[0], "ibc: " +v.get(0)+" "+v.get(3), "parks", "parks", 10, "", 0, "ok", "ok", 0));
                    tot++;
                }
            }
        }

        System.out.println("tot = "+tot);






        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for(String city: hm.keySet()) {
            System.out.println(city+" ==> "+hm.get(city).size());
            File f = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\ibc.json");
            mapper.writeValue(f, hm.get(city));
        }
    }
}
