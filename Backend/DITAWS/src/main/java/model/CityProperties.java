package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 19/10/2016.
 */
public class CityProperties {
    private String name;
    private double[] lonLatBL;
    private double[] lonLatTR;


    public static List<CityProperties> getInstance(String file) {
        List<CityProperties> l = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine())!=null) {
                if(line.startsWith("//")) continue;
                String[] e = line.split(",");
                l.add(new CityProperties(e[0],
                        new double[]{Double.parseDouble(e[1]),Double.parseDouble(e[2])},
                        new double[]{Double.parseDouble(e[3]),Double.parseDouble(e[4])}));
            }

            br.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return l;
    }

    private CityProperties(String name, double[] lonLatBL, double[] lonLatTR) {
        this.name = name;
        this.lonLatBL = lonLatBL;
        this.lonLatTR = lonLatTR;
    }

    public String getName() {
        return name;
    }

    public String getDataDir() {
        return name+"/";
    }

    public String getDB() {
        return "lume-"+name;
    }

    public String getBbox() {
        return getBL()+","+getTR();
    }

    public String getBL() {
        return lonLatBL[0]+","+lonLatBL[1];
    }
    public String getTR() {
        return lonLatTR[0]+","+lonLatTR[1];
    }
    public String getBR() {
        return lonLatTR[0]+","+lonLatBL[1];
    }
    public String getTL() {
        return lonLatBL[0]+","+lonLatTR[1];
    }

    public double[][] getLonLatBbox() {
        return new double[][]{lonLatBL,lonLatTR};
    }


    public double[] getCenterLonLat() {
        return new double[]{(lonLatBL[0]+lonLatTR[0])/2, (lonLatBL[1]+lonLatTR[1])/2};
    }

    public boolean contains(double lat, double lon) {
        return (lonLatBL[0] < lon && lonLatBL[1] < lat && lonLatTR[0] > lon && lonLatTR[1] > lat);
    }

}
