package services.timdatapipe;

import model.CityProperties;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import util.Colors;
import util.KMLSquare;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static util.GeoJson2KML.printFooterDocument;
import static util.GeoJson2KML.printHeaderDocument;

/**
 * Created by marco on 24/04/2017.
 */
public class BILReader {

    public static final int NO_DATA = 65535;

    public static void main(String[] args) throws Exception {


        HeaderBil hb = read("D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170413_1700.zip");

    }

    public static HeaderBil read(String file) {
        try {
            File f = new File(file);
            if(!f.exists()) return null;

            ZipFile zipFile = new ZipFile(file);
            Enumeration<ZipEntry> e = (Enumeration<ZipEntry>)zipFile.entries();
            InputStream hdr_is = null;
            InputStream bil_is = null;
            while((e.hasMoreElements())) {
                ZipEntry ze = e.nextElement();
                System.out.println(ze.getName());
                if(ze.getName().endsWith(".hdr"))
                    hdr_is = zipFile.getInputStream(ze);
                if(ze.getName().endsWith(".bil"))
                    bil_is = zipFile.getInputStream(ze);
            }
            Map<String,String> header = processHdr(hdr_is);
            int[][] bil = processBil(bil_is,header);
            zipFile.close();
            return new HeaderBil(header,bil);

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public static Map<String,String> processHdr(InputStream is) {
        Map<String,String> header = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                String[] e = line.split(" ");
                header.put(e[0],e[1]);
            }
            br.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println(header);
        return header;
    }

    public static int[][] processBil(InputStream is, Map<String,String> header) {
        int nrows = Integer.parseInt(header.get("nrows"));
        int ncols = Integer.parseInt(header.get("ncols"));
        int[][] sarray = new int[nrows][ncols];
        try {
            DataInputStream dis = new DataInputStream(is);

            System.out.println(nrows+" * "+ncols);
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (int i = 0; i < nrows; i++)
                for (int j = 0; j < ncols; j++) {
                    sarray[i][j] = dis.readUnsignedShort();
                    ds.addValue(sarray[i][j]);
                }
            dis.close();

            for(int p=10;p<=100;p=p+10)
                System.out.println(p+" ==> "+ds.getPercentile(p));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sarray;
    }




}
