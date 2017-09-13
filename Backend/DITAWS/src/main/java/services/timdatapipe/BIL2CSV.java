package services.timdatapipe;

import model.CityProperties;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by marco on 16/06/2017.
 */
public class BIL2CSV {
    public final static SimpleDateFormat sdf_day = new SimpleDateFormat("yyyyMMdd"); //20170413
    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm"); //20170413_1700
    public static void main(String[] args) throws  Exception {

        CityProperties city = CityProperties.getInstanceHash("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv").get("Modena");
        Calendar start = new GregorianCalendar(2017, Calendar.APRIL, 14, 0, 0, 0);
        Calendar end = new GregorianCalendar(2017, Calendar.JULY, 27,  24, 0, 0);
        run(city,start,end,"D:\\"+city.getName()+"_"+sdf_day.format(start.getTime())+"_"+sdf_day.format(end.getTime()));
    }
    public static void run(CityProperties city,Calendar start,Calendar end,String outfile) throws Exception{

        PrintWriter out = new PrintWriter(new FileWriter(outfile));


        HeaderBil hb = BILReader.read("D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170413_1245.zip");
        double ulxmap = Double.parseDouble(hb.header.get("ulxmap"));
        double ulymap = Double.parseDouble(hb.header.get("ulymap"));
        double xdim = Double.parseDouble(hb.header.get("xdim"));
        double ydim = Double.parseDouble(hb.header.get("ydim"));
        double[][] lonLatBbox = city.getLonLatBbox();

        int minj = (int)Math.floor((lonLatBbox[0][0] - ulxmap + xdim/2)/xdim);
        int maxi = (int)Math.ceil((ulymap - lonLatBbox[0][1] + ydim/2)/ydim);

        int maxj = (int)Math.ceil((lonLatBbox[1][0] - ulxmap + xdim/2)/xdim);
        int mini = (int)Math.floor((ulymap - lonLatBbox[1][1] + ydim/2)/ydim);

        int nrows = maxi - mini;
        int ncols = maxj - minj;



        Calendar cal = (Calendar)start.clone();
        while(cal.before(end)) {
            String time = sdf.format(cal.getTime());
            System.out.println(time);
            String file = "D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_"+time+".zip";
            writeCSV(out,cal,nrows,ncols,mini,minj,BILReader.read(file));
            cal.add(Calendar.MINUTE,15);
        }
        out.close();
    }
    public static  void writeCSV(PrintWriter out, Calendar cal, int nrows, int ncols, int mini, int minj, HeaderBil hb) throws Exception {

        //System.out.println("["+mini+","+maxi+"] X ["+minj+","+maxj+"]");
        for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++) {
                String key = (mini + i)+"-"+(minj + j);
                int v = hb == null ? BILReader.NO_DATA : hb.bil[mini + i][minj + j];
                out.println(sdf.format(cal.getTime())+","+key+","+v);
            }
    }

}
