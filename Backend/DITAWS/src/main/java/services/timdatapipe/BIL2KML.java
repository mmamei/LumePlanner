package services.timdatapipe;

import model.City;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import util.Colors;
import util.KMLSquare;

import java.io.FileWriter;
import java.io.PrintWriter;

import static services.timdatapipe.BILReader.NO_DATA;
import static util.GeoJson2KML.printFooterDocument;
import static util.GeoJson2KML.printHeaderDocument;

/**
 * Created by marco on 21/06/2017.
 */
public class BIL2KML {

    public static void main(String[] args) {
        HeaderBil hb = BILReader.read("D:\\LUME-ER\\Nrealtime_Emilia-Romagna_15_20170621_0930.zip");
        City city = City.getInstance("Modena");

        city = new City("Modena","Modena","",new double[]{10.886880,44.617892,10.953896,44.657667});



        String fileKml = "D:\\"+city.getName()+".kml";
        drawKML(city,hb,fileKml);
    }

    public static void drawKML(City city, HeaderBil hb, String fileKml) {

        System.out.println(city.getName()+" *********************************************************************");


        double ulxmap = Double.parseDouble(hb.header.get("ulxmap"));
        double ulymap = Double.parseDouble(hb.header.get("ulymap"));
        double xdim = Double.parseDouble(hb.header.get("xdim"));
        double ydim = Double.parseDouble(hb.header.get("ydim"));
        double[] lonLatBbox = city.getLonLatBBox();

        int minj = (int)Math.floor((lonLatBbox[0] - ulxmap + xdim/2)/xdim);
        int maxi = (int)Math.ceil((ulymap - lonLatBbox[1] + ydim/2)/ydim);

        int maxj = (int)Math.ceil((lonLatBbox[2] - ulxmap + xdim/2)/xdim);
        int mini = (int)Math.floor((ulymap - lonLatBbox[3] + ydim/2)/ydim);

        int nrows = maxi - mini;
        int ncols = maxj - minj;

        double ox = ulxmap + (minj * xdim);
        double oy = ulymap - (mini * ydim);

        System.out.println("["+mini+","+maxi+"] X ["+minj+","+maxj+"]");

        double[][] x = getCellBorder(mini,minj,ox,oy,xdim,ydim);

        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++) {
                int v = hb.bil[mini+i][minj+j];
                if(v < NO_DATA)
                    ds.addValue(v);
            }

        for(int p=10;p<=100;p=p+10)
            System.out.println(p+" ==> "+ds.getPercentile(p));


        try {
            PrintWriter out = new PrintWriter(new FileWriter(fileKml));
            printHeaderDocument(out, city.getName());
            KMLSquare kmlsq = new KMLSquare();
            for (int i = 0; i < nrows; i++)
                for (int j = 0; j < ncols; j++) {
                    String key = (mini + i)+"-"+(minj + j);
                    int v = hb.bil[mini+i][minj+j];
                    String color = v < NO_DATA ? Colors.val01_to_color(1.0 * v / ds.getMax()) : "770000ff";
                    String desc = v < NO_DATA ? 1.0*v/10 + " / " + ds.getMax()/10 : "NO DATA";
                    out.println(kmlsq.draw(getCellBorder(i, j, ox, oy, xdim, ydim), key, color, color, i + "," + j + " = " + desc));
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
