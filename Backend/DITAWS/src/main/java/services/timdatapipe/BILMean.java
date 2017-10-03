package services.timdatapipe;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static services.timdatapipe.BILReader.NO_DATA;

/**
 * Created by marco on 02/10/2017.
 */
public class BILMean {
    public final static SimpleDateFormat sdf_day = new SimpleDateFormat("yyyyMMdd"); //20170413
    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm"); //20170413_1700

    public static void main(String[] args) {
        Calendar start = new GregorianCalendar(2017, Calendar.APRIL, 14, 0, 0, 0);
        Calendar end = new GregorianCalendar(2017, Calendar.MAY, 1,  24, 0, 0);
        String file = "D:\\crowd_mean_"+sdf_day.format(start.getTime())+"_"+sdf_day.format(end.getTime())+".ser";
        saveMean(start,end,file);
    }


    public static void saveMean(Calendar start, Calendar end, String outFile) {

        int[][] sample = BILReader.read("D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170413_1245.zip").bil;

        float[][][][] num = new float[sample.length][sample[0].length][7][24];
        float[][][][] den = new float[sample.length][sample[0].length][7][24];

        Calendar cal = (Calendar)start.clone();
        while(cal.before(end)) {
            String time = sdf.format(cal.getTime());
            System.out.println(time);
            String file = "D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_"+time+".zip";
            HeaderBil hb = BILReader.read(file);
            if(hb != null) {
                int d = cal.get(Calendar.DAY_OF_WEEK) - 1;
                int h = cal.get(Calendar.HOUR_OF_DAY);
                for (int i = 0; i < hb.bil.length; i++)
                    for (int j = 0; j < hb.bil[i].length; j++)
                        if (hb.bil[i][j] < NO_DATA) {
                            num[i][j][d][h] = 1.0f *  hb.bil[i][j] / 1000;
                            den[i][j][d][h]++;
                        }
            }
            cal.add(Calendar.MINUTE,15);
        }


        for(int i=0; i<num.length;i++)
        for(int j=0; j<num[i].length;j++)
        for(int d=0; d<7; d++)
        for(int h=0; h<24; h++)
            num[i][j][d][h] = den[i][j][d][h] > 0 ? num[i][j][d][h] / den[i][j][d][h] : -1;


        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(num);
            out.close();
        }catch(Exception z) {
            z.printStackTrace();
        }
    }
}
