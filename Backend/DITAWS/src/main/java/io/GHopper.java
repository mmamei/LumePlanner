package io;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPlace;
import model.POI;
import model.Path;
import org.geojson.LineString;
import org.geojson.LngLatAlt;

import java.util.List;

/**
 * Created by marco on 21/04/2017.
 */
public class GHopper {


    public static void main(String[] args) {
        GHopper gh = new GHopper();
        Path p = gh.route("foot",44.52615814969668,10.866599678993227,44.524776,10.867791);
        System.out.println(p.toJSONString());

    }


    private GraphHopper gh;
    public GHopper() {
        Config config = new Config();
        gh = new GraphHopper().forServer();
        gh.setInMemory();
        //gh.setOSMFile("C:/DATASET/osm/er/emilia-romagna.osm");
        //gh.setGraphHopperLocation("C:/DATASET/osm/er/FOOT/");
        gh.setOSMFile(config.get("OSM_FILE"));
        gh.setGraphHopperLocation(config.get("GRAPHHOPPER_LOC"));
        gh.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
        gh.importOrLoad();
    }

    public Path route(String vehicle, POI o, POI d) {
        double start_lat = o.getGeometry().getCoordinates().getLatitude();
        double start_lon = o.getGeometry().getCoordinates().getLongitude();
        double end_lat = d.getGeometry().getCoordinates().getLatitude();
        double end_lon = d.getGeometry().getCoordinates().getLongitude();
        return route(vehicle, start_lat,start_lon,end_lat,end_lon);
    }

    public Path route(String vehicle, double start_lat, double start_lon, double end_lat, double end_lon) {
        Path result = new Path();
        GHPlace from = new GHPlace(start_lat,start_lon);
        GHPlace  to = new GHPlace(end_lat,end_lon);
        GHRequest request = new GHRequest(from, to).setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);
        GHResponse response = gh.route(request);
        result.setPoints(convertToLineString(response.getPoints().toGeoJson(false)));
        result.setLength(response.getDistance());
        return result;
    }

    private LineString convertToLineString(List<Double[]> points) {
        LineString lineString = new LineString();
        for (Double[] point : points) {
            lineString.add(new LngLatAlt(point[0], point[1]));
        }
        return lineString;
    }
}
