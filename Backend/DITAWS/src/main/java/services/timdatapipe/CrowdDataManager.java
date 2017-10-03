package services.timdatapipe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CityProperties;
import model.CrowdData;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static services.timdatapipe.BILReader.NO_DATA;


/**
 * Created by marco on 02/10/2017.
 */
public class CrowdDataManager {

    private Map<String,CityProperties> cities;
    private String last_time = "";

    public static void main(String[] args) {
        long starttime = System.currentTimeMillis();
        CrowdDataManager cdm = new CrowdDataManager();
        cdm.processCrowdInfo();
        cdm.processCrowdInfo();
        long endtime = System.currentTimeMillis();
        System.out.println("Completed in: "+(endtime - starttime) / 1000);
    }

    public CrowdDataManager() {
        cities = CityProperties.getInstanceHash("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv");
    }

    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm"); //20170413_1700

    public void processCrowdInfo() {

        File file = lastFileModified("D:/LUME-ER");
        int day = -1;
        int hour = -1;
        try {
            Calendar cal = new GregorianCalendar();
            String time = file.getName().substring("Nrealtime_Emilia-Romagna_15_".length(),file.getName().length()-4);
            if(time.equals(last_time)) {
                System.out.println("CrowdDataManager "+time+" already processed");
                return;
            }

            last_time = time;
            cal.setTime(sdf.parse(time));
            day = cal.get(Calendar.DAY_OF_WEEK) - 1;
            hour = cal.get(Calendar.HOUR_OF_DAY);
        }catch(Exception e) {
            e.printStackTrace();
        }

        HeaderBil hb = BILReader.read(file.getAbsolutePath());

        double ulxmap = Double.parseDouble(hb.header.get("ulxmap"));
        double ulymap = Double.parseDouble(hb.header.get("ulymap"));
        double xdim = Double.parseDouble(hb.header.get("xdim"));
        double ydim = Double.parseDouble(hb.header.get("ydim"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        float[][][][] means = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("D:\\crowd_mean_20170414_20170502.ser"));
            means = (float[][][][]) in.readObject();
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        for(String city: cities.keySet()) {

            double[][] lonLatBbox = cities.get(city).getLonLatBbox();

            int minj = (int)Math.floor((lonLatBbox[0][0] - ulxmap + xdim/2)/xdim);
            int maxi = (int)Math.ceil((ulymap - lonLatBbox[0][1] + ydim/2)/ydim);

            int maxj = (int)Math.ceil((lonLatBbox[1][0] - ulxmap + xdim/2)/xdim);
            int mini = (int)Math.floor((ulymap - lonLatBbox[1][1] + ydim/2)/ydim);

            int nrows = maxi - mini;
            int ncols = maxj - minj;

            double ox = ulxmap + (minj * xdim);
            double oy = ulymap - (mini * ydim);

            System.out.println(city+" ["+mini+","+maxi+"] X ["+minj+","+maxj+"]");
            int[][] avalues = new int[nrows][ncols];
            float[][] mvalues = null;
            if(means!=null && day >= 0 && hour >= 0)
                mvalues = new float[nrows][ncols];
            for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++) {
                int v = hb.bil[mini+i][minj+j];
                v = (v == NO_DATA) ? -1 : v / 1000;
                avalues[i][j] = v;
                if(mvalues != null)
                    mvalues[i][j] = 1.0f * v / means[i][j][day][hour];
            }


            CrowdData dc = new CrowdData(last_time,ulxmap,ulymap,xdim,ydim,minj,maxi,maxj,
                    mini,nrows,ncols,ox,oy,avalues,mvalues);

            File dir = new File("C:\\Tomcat7\\webapps\\DITA\\files\\data\\" + city);
            dir.mkdirs();
            File f = new File(dir+"\\crowd.json");
            try {
                mapper.writeValue(f,dc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private File lastFileModified(String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }
}