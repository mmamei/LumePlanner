package services;

import com.graphhopper.util.shapes.GHPlace;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by marco on 27/09/2017.
 */
public class ExtraPoisCSVPreprocess {


    static Set<String> all = new HashSet<>();

    static int count = 0;

    public static void main(String[] args) throws Exception {
        File extraDir = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\extra-pois");
        PrintWriter out = new PrintWriter(new FileWriter(extraDir+"/extra-pois.csv"));
        for(File dir: extraDir.listFiles()) {
            if(dir.isDirectory()) {
                // dentro ad ognuna di queste directory ci deve essere un file csv.
                // eventualmente un file xlsx e dei file di immagini
                for(File f: dir.listFiles()) {
                    if(f.getName().endsWith(".csv")) {
                        process(f,out);
                    }
                }
            }
        }
        out.close();
    }


    static Map<String,String> dict = new HashMap<String,String>();
    static {
        dict.put("nome azienda e ragione sociale","nome azienda");
        dict.put("descrizione/attività","descrizione");
        dict.put("referente/email/tel","contact");
    }



    public static void process(File f, PrintWriter out) throws Exception {

        // check for extra address
        String extra_address = "";
        File extra_address_file = new File(f.getParentFile()+"/extra-address.txt");
        if(extra_address_file.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(extra_address_file), "UTF8"));
            extra_address = br.readLine();
            br.close();
        }



        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

        String header = br.readLine().toLowerCase();
        //System.out.println(header);

        String sep = ",";
        if(header.contains(";")) sep = ";";





        Map<String,Integer> field2pos = new HashMap<>();

        String[] fields = header.split(sep);

        for(int i=0; i<fields.length;i++) {
            String field = fields[i].trim();
            if(dict.get(field) != null) field = dict.get(field);
            if(!field.isEmpty())
                field2pos.put(field,i);


        }

        System.out.println(field2pos);



        String line;
        while((line = br.readLine())!=null) {
            try {
                String[] e = line.split(sep);

                String nome = e[field2pos.get("nome azienda")];
                String descrizione = e[field2pos.get("descrizione")].replaceAll("\"","");

                double lat = 0;
                double lng = 0;

                if(field2pos.containsKey("latitude") && field2pos.containsKey("longitude")) {
                    lat = Double.parseDouble(e[field2pos.get("latitude")]);
                    lng = Double.parseDouble(e[field2pos.get("longitude")]);
                }
                else {
                    String indirizzo = e[field2pos.get("indirizzo")].replaceAll("\"","") + ", " + extra_address;
                    double[] lnglat = GeocoderGoogleMaps.geocodeLngLat(indirizzo);
                    lat = lnglat[1];
                    lng = lnglat[0];
                }
                //mrnl1_6,attractions,0.5,Galleria del vento,10.871535,44.530991,Opera
                count ++;
                out.println("extra"+count+",lifestyle,0.3,"+nome+","+lng+","+lat+","+descrizione);
            } catch(Exception e) {
                e.printStackTrace();
                System.err.println(line);
                System.exit(0);
            }

        }
        br.close();

    }

}
