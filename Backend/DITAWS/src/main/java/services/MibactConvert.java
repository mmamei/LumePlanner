package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CityProperties;
import model.POI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class MibactConvert {

    public static  boolean GUIDA_ROSSA_ONLY = true;

    public static void main(String[] args) throws Exception {

        Map<String,String> mibact2nominatim = new HashMap<>();


        // POI Categories
        // attractions
        // monuments
        // museums
        // parks
        // historical
        // religious

        mibact2nominatim.put("Battistero","religious");
        mibact2nominatim.put("Portico","historical");
        mibact2nominatim.put("Monumento","monuments");
        mibact2nominatim.put("Stazione","attractions");
        mibact2nominatim.put("Borgo","historical");
        mibact2nominatim.put("Convento","religious");
        mibact2nominatim.put(" Mercato","attractions");
        mibact2nominatim.put("Oratorio","religious");
        mibact2nominatim.put("Ospitale","historical");
        mibact2nominatim.put("Colombaio","historical");
        mibact2nominatim.put("Canonica","religious");
        mibact2nominatim.put("Pubblica","attractions");
        mibact2nominatim.put("Cisterna","attractions");
        mibact2nominatim.put("Annesso","attractions");
        mibact2nominatim.put("Ecclesiastica","religious");
        mibact2nominatim.put("Palazzo","attractions");
        mibact2nominatim.put("Rocca","historical");
        mibact2nominatim.put("Scuola","historical");
        mibact2nominatim.put("Fienile","parks");
        mibact2nominatim.put("Parco","parks");
        mibact2nominatim.put("Sacrestia","religious");
        mibact2nominatim.put("Grotta","parks");
        mibact2nominatim.put("Campanile","religious");
        mibact2nominatim.put("Piazza","attractions");
        mibact2nominatim.put("Barchessa","attractions");
        mibact2nominatim.put("Villa","attractions");
        mibact2nominatim.put("Edificio","attractions");
        mibact2nominatim.put("Sinagoga","religious");
        mibact2nominatim.put("Elemento","attractions");
        mibact2nominatim.put("Cimitero","religious");
        mibact2nominatim.put("Mulino","historical");
        mibact2nominatim.put("Chiesa","religious");
        mibact2nominatim.put("Casa","attractions");
        mibact2nominatim.put("Mura","historical");
        mibact2nominatim.put("Stalla","attractions");
        mibact2nominatim.put("Teatro","attractions");
        mibact2nominatim.put("Strada","attractions");
        mibact2nominatim.put("Santuario","religious");
        mibact2nominatim.put("Cinema","attractions");
        mibact2nominatim.put("Ospedale","attractions");
        mibact2nominatim.put("Bottega","attractions");
        mibact2nominatim.put("Macello","attractions");
        mibact2nominatim.put("Impianto","attractions");
        mibact2nominatim.put("Carcere","attractions");
        mibact2nominatim.put("Torre","attractions");
        mibact2nominatim.put("Cappella","religious");
        mibact2nominatim.put("Ponte","monuments");
        mibact2nominatim.put("Porta","monuments");
        mibact2nominatim.put("Fabbricato","attractions");
        mibact2nominatim.put("Caserma","attractions");


        List<CityProperties> cities = CityProperties.getInstance("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv");

        Map<String,List<POI>> hm = new HashMap<>();
        for(CityProperties city : cities)
            hm.put(city.getName(),new ArrayList<POI>());


        BufferedReader br = new BufferedReader(new FileReader("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\patrimonioculturale-er.csv"));
        String line;

        String[] header = br.readLine().split("\t"); // skip header
        //for(int i=0; i<header.length;i++)
        //System.out.println(i+" "+header[i]);


        // POI Constructor:
        // String place_id, double lat, double lon, String display_name, String category, String type,
        // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating



        Map<String,Integer> allCategories = new HashMap<>();

        int tot = 0;
        while((line = br.readLine())!=null) {
            String[] e = line.split("\t");

            String cat = e[13].split(" |/")[0];

            Integer c = allCategories.get(cat);
            if(c == null) allCategories.put(cat,1);
            else allCategories.put(cat,c+1);


            double[] lonlat = convert(e[0]);
            for(CityProperties cp: cities) {
                if(cp.contains(lonlat[1],lonlat[0])) {
                    if(!GUIDA_ROSSA_ONLY || (GUIDA_ROSSA_ONLY && e.length > 26 && !e[26].isEmpty())) {
                        hm.get(cp.getName()).add(new POI(e[1], lonlat[1], lonlat[0], "mibact: " + e[3], mibact2nominatim.get(cat), cat, 10, "", 0, "ok", "ok", 0));
                        tot ++;
                    }
                }
            }
        }

        for(String k: allCategories.keySet())
            System.out.println(k+" ==> "+allCategories.get(k));

        br.close();
        System.out.println("tot = "+tot);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for(String city: hm.keySet()) {
            System.out.println(city+" ==> "+hm.get(city).size());
            File f = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\mibact.json");
            mapper.writeValue(f, hm.get(city));
        }
    }
    //"POINT (11.586493673133132 44.806869144609884)"
    private static double[] convert(String wtk) {
        String[] e = wtk.substring("\"POINT (".length(),wtk.length()-2).split(" ");
        return new double[]{Double.parseDouble(e[0]),Double.parseDouble(e[1])};
    }
}
