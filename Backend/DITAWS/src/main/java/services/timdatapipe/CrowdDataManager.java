package services.timdatapipe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.City;
import model.CrowdData;
import util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static services.timdatapipe.BILReader.NO_DATA;


/**
 * Created by marco on 02/10/2017.
 */
public class CrowdDataManager {

    private List<City> cities;
    private String last_time = "";

    public static void main(String[] args) {
        long starttime = System.currentTimeMillis();
        CrowdDataManager cdm = new CrowdDataManager();
        cdm.processCrowdInfo(true);
        cdm.processCrowdInfo(true);
        long endtime = System.currentTimeMillis();
        System.out.println("Completed in: "+(endtime - starttime) / 1000);
    }

    public CrowdDataManager() {
        cities = City.getInstance();
    }

    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm"); //20170413_1700

    public void processCrowdInfo(boolean verbose) {

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
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("D:\\crowd_mean.ser"));
            means = (float[][][][]) in.readObject();
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        /*
        Parma [247,313] X [644,711]
        Ravenna [518,613] X [1779,1877]
        Bologna [503,534] X [1260,1307]
        Maranello [447,502] X [940,1031]
        Modena [364,419] X [997,1081]
        Rimini [830,935] X [2003,2093]
        Ferrara [234,254] X [1438,1468]
        Forli [712,780] X [1667,1742]
        ReggioEmilia [333,381] X [843,901]
        Cesena [784,820] X [1799,1857]
        Piacenza [56,95] X [262,341]

        int mi =  364;
        for(int j=997; j<1081;j++) {
              System.out.println(mi+","+j+" => "+means[mi][j][day][hour]+" ");

        }
        //System.exit(0);
        */


        for(City city: cities) {

            if(verbose) System.out.println(city);

            double[] lonLatBbox = city.getLonLatBBox();

            int minj = (int)Math.floor((lonLatBbox[0] - ulxmap + xdim/2)/xdim);
            int maxi = (int)Math.ceil((ulymap - lonLatBbox[1] + ydim/2)/ydim);

            int maxj = (int)Math.ceil((lonLatBbox[2] - ulxmap + xdim/2)/xdim);
            int mini = (int)Math.floor((ulymap - lonLatBbox[3] + ydim/2)/ydim);

            int nrows = maxi - mini;
            int ncols = maxj - minj;

            double ox = ulxmap + (minj * xdim);
            double oy = ulymap - (mini * ydim);

            System.out.print(city+" ["+mini+","+maxi+"] X ["+minj+","+maxj+"] ");
            int sum = 0;
            int[][] avalues = new int[nrows][ncols];
            float[][] mvalues = null;
            if(means!=null && day >= 0 && hour >= 0)
                mvalues = new float[nrows][ncols];
            for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++) {
                int v = hb.bil[mini+i][minj+j];
                v = (v == NO_DATA) ? -1 : 3 * v / 10; // 3 per considerare TIM, Vodsfone e Wind
                avalues[i][j] = v;
                sum += v;
                if(mvalues != null) {
                    mvalues[i][j] = (v == -1 || means[mini+i][minj+j][day][hour] <= 0) ? -1 : 1.0f * v / means[mini+i][minj+j][day][hour];

                    //if(city.equals("Modena") && mvalues[i][j] > 10)
                    //    System.out.println(v+" / "+ means[mini+i][minj+j][day][hour]);

                }
            }
            System.out.println("tot = "+sum);



            CrowdData dc = new CrowdData(last_time,ulxmap,ulymap,xdim,ydim,minj,maxi,maxj,
                    mini,nrows,ncols,ox,oy,avalues,mvalues);

            File dir = new File("C:\\Tomcat7\\webapps\\DITA\\files\\data\\" + city.getName());
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