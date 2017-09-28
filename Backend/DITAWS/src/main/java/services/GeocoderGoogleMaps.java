package services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static io.LoginInfo.GEOCODER_KEY;

/**
 * Created by marco on 27/09/2017.
 */
public class GeocoderGoogleMaps {

    private static int timeoutInMillis = 10000;
    private static String userAgent = "Lume Web Service";

    public static void main(String[] args) throws Exception {
        double[] lnglat = geocodeLngLat("Via dei Tintori 25, Modena, Italy");
        System.out.println(lnglat[1]+","+lnglat[0]);
    }

    public static double[] geocodeLngLat(String address) throws Exception {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+encodeURL(address)+"&key=AIzaSyCupE5baRdgb9hL2zPKIRlOYPXnPJBsRkM";
        HttpURLConnection hConn = openConnection(url);
        String str = readString(hConn.getInputStream());
        //System.out.println(str);
        JSONArray results = new JSONObject(str).getJSONArray("results");
        if(results.length() == 0)
            System.out.println("cannot find "+address);
        if(results.length() > 1)
            System.out.println(address+" has "+results.length()+" entries");

        JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
        double lng = location.getDouble("lng");
        double lat = location.getDouble("lat");
        return new double[]{lng,lat};

    }

    private static String encodeURL( String str )
    {
        try
        {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception _ignore)
        {
            return str;
        }
    }

    private static HttpURLConnection openConnection( String url ) throws IOException {
        HttpURLConnection hConn = (HttpURLConnection) new URL(url).openConnection();
        hConn.setRequestProperty("User-Agent", userAgent);
        hConn.setRequestProperty("content-charset", "UTF-8");
        hConn.setConnectTimeout(timeoutInMillis);
        hConn.setReadTimeout(timeoutInMillis);
        hConn.connect();
        return hConn;
    }
    private static String readString( InputStream inputStream ) throws IOException {
        String encoding = "UTF-8";
        InputStream in = new BufferedInputStream(inputStream, 4096);
        try
        {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int numRead;
            while ((numRead = in.read(buffer)) != -1)
            {
                output.write(buffer, 0, numRead);
            }
            return output.toString(encoding);
        } finally
        {
            in.close();
        }
    }

}
