package services.pathfinder;

import io.Mongo;
import model.POI;
import services.timdatapipe.CrowdDataManager;

import java.util.List;

import static util.Misc.haverDist;

/**
 * Created by marco on 19/04/2017.
 */

public class FindCrowdPath extends FindPathAbstract {

    @Override
    public String[] compute_poi_sequence(String city, Mongo dao, String user, POI departure, POI arrival, String start_time, List<String> POIsList) {
        String[] poi_sequence = new String [to_visit.size()+2];
        int cont = 0;
        poi_sequence[cont++] = departure.getPlace_id();

        POI from = departure;
        while (!to_visit.isEmpty()) {
            double min_distance = Double.MAX_VALUE;
            POI closest = null;
            for (String poi : to_visit) {
                POI current = dao.retrieveActivity(city,poi);

                double current_distance = haverDist(
                        new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()},
                        new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
                current_distance *= crowding(city, current, start_time);
                if (current_distance < min_distance) {
                    min_distance = current_distance;
                    closest = current;
                }
            }
            poi_sequence[cont++] = closest.getPlace_id();

            to_visit.remove(closest.getPlace_id());
            from = closest;
        }
        poi_sequence[cont++] = arrival.getPlace_id();
        return poi_sequence;
    }


    public double crowding(String city, POI current, String time) {
        return CrowdDataManager.getCrowdings(current.getGeometry().getCoordinates().getLatitude(),current.getGeometry().getCoordinates().getLongitude());
    }
}