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

        String file = "D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170413_1700.zip";
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

        List<CityProperties> cities = CityProperties.getInstance("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv");
        for(CityProperties city: cities)
            drawKML(city,header,bil);
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

    public static void drawKML(CityProperties city, Map<String,String> header, int[][] bil) {

        System.out.println(city.getName()+" *********************************************************************");




        double ulxmap = Double.parseDouble(header.get("ulxmap"));
        double ulymap = Double.parseDouble(header.get("ulymap"));
        double xdim = Double.parseDouble(header.get("xdim"));
        double ydim = Double.parseDouble(header.get("ydim"));
        double[][] lonLatBbox = city.getLonLatBbox();

        int minj = (int)Math.floor((lonLatBbox[0][0] - ulxmap + xdim/2)/xdim);
        int maxi = (int)Math.ceil((ulymap - lonLatBbox[0][1] + ydim/2)/ydim);

        int maxj = (int)Math.ceil((lonLatBbox[1][0] - ulxmap + xdim/2)/xdim);
        int mini = (int)Math.floor((ulymap - lonLatBbox[1][1] + ydim/2)/ydim);

        int nrows = maxi - mini;
        int ncols = maxj - minj;

        double ox = ulxmap + (minj * xdim);
        double oy = ulymap - (mini * ydim);

        System.out.println("["+mini+","+maxi+"] X ["+minj+","+maxj+"]");

        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++) {
                int v = bil[mini+i][minj+j];
                if(v < NO_DATA)
                    ds.addValue(v);
            }

        for(int p=10;p<=100;p=p+10)
            System.out.println(p+" ==> "+ds.getPercentile(p));


        try {
            PrintWriter out = new PrintWriter(new FileWriter("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\" + city.getName() + ".kml"));
            printHeaderDocument(out, city.getName());
            KMLSquare kmlsq = new KMLSquare();
            for (int i = 0; i < nrows; i++)
                for (int j = 0; j < ncols; j++) {
                    int v = bil[mini+i][minj+j];
                    String color = v < NO_DATA ? Colors.val01_to_color(1.0 * v / ds.getMax()) : "770000ff";
                    String desc = v < NO_DATA ? v/10 + " / " + ds.getMax()/10 : "NO DATA";
                    out.println(kmlsq.draw(getCellBorder(i, j, ox, oy, xdim, ydim), i + "," + j, color, color, i + "," + j + " = " + desc));
                }
            printFooterDocument(out);
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static double[][] getCellBorder(int i, int j, double ox, double oy, double xdim, double ydim) {
        double[][] ll = new double[5][2];

        // bottom left corner
        double x = ox + (j * xdim) - xdim/2;
        double y = oy - (i * ydim) - ydim/2;

        ll[0] = new double[]{x,y};
        ll[1] = new double[]{x+xdim,y};
        ll[2] = new double[]{x+xdim,y+ydim};
        ll[3] = new double[]{x,y+ydim};
        ll[4] = new double[]{x,y};
        return ll;
    }


}
