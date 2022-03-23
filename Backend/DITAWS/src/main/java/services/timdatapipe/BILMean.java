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
        saveMean();
    }

    public static void saveMean() {
        /*
        Calendar start = new GregorianCalendar();
        start.add(Calendar.WEEK_OF_YEAR,-2);
        start.set(Calendar.HOUR_OF_DAY ,0);
        start.set(Calendar.MINUTE ,0);

        Calendar end = new GregorianCalendar();
        end.set(Calendar.HOUR_OF_DAY ,23);
        end.set(Calendar.MINUTE ,59);
        */

        Calendar start = new GregorianCalendar();
        try {
            start.setTime(sdf_day.parse("20180513"));
        }catch(Exception e) {
            e.printStackTrace();
        }

        Calendar end = new GregorianCalendar();
        try {
            end.setTime(sdf_day.parse("20180523"));
        }catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Compute means from "+sdf.format(start.getTime())+" to "+sdf.format(end.getTime()));

        saveMean(start,end,"D:\\crowd_mean.ser","D:\\crowd_sd.ser");
    }


    public static void saveMean(Calendar start, Calendar end, String mFile, String sdFile) {

        int[][] sample = BILReader.read("D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170413_1245.zip").bil;

        float[][][][] num = new float[sample.length][sample[0].length][7][24];
        float[][][][] numsq = new float[sample.length][sample[0].length][7][24];
        float[][][][] den = new float[sample.length][sample[0].length][7][24];

        System.out.println("init complete");

        Calendar cal = (Calendar)start.clone();
        while(cal.before(end)) {
            String time = sdf.format(cal.getTime());
            //System.out.print(time);
            String file = "D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_"+time+".zip";
            HeaderBil hb = BILReader.read(file);
            int valid_data = 0;
            if(hb != null) {
                int d = cal.get(Calendar.DAY_OF_WEEK) - 1;
                int h = cal.get(Calendar.HOUR_OF_DAY);
                for (int i = 0; i < hb.bil.length; i++)
                    for (int j = 0; j < hb.bil[i].length; j++)
                        if (hb.bil[i][j] < NO_DATA) {
                            num[i][j][d][h] += 1.0f * 3 * hb.bil[i][j] / 10; // 3 per TIM,Vodafone e Wind
                            numsq[i][j][d][h] += Math.pow(num[i][j][d][h],2);
                            den[i][j][d][h]++;
                            valid_data ++;
                        }
            }
            //System.out.println(".... valid data = "+valid_data);
            cal.add(Calendar.MINUTE,15);
        }


        for(int i=0; i<num.length;i++)
        for(int j=0; j<num[i].length;j++)
        for(int d=0; d<7; d++)
        for(int h=0; h<24; h++) {
            num[i][j][d][h] = den[i][j][d][h] > 0 ? Math.round(num[i][j][d][h] / den[i][j][d][h]) : -1;
            numsq[i][j][d][h] = den[i][j][d][h] > 0 ? (float)Math.sqrt((numsq[i][j][d][h] / den[i][j][d][h]) - Math.pow(num[i][j][d][h],2))  : -1;
        }


        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mFile));
            out.writeObject(num);
            out.close();

            out = new ObjectOutputStream(new FileOutputStream(sdFile));
            out.writeObject(numsq);
            out.close();

        }catch(Exception z) {
            z.printStackTrace();
        }
    }
}
