package services.pathfinder;

import io.Mongo;
import model.Activity;
import model.POI;
import model.Visit;
import model.VisitPlan;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static util.Misc.haverDist;

/**
 * Created by marco on 09/10/2017.
 */
public abstract class FindPathAbstract {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void main(String[] args) throws IOException {

        Mongo dao = new Mongo();
        POI departure = new POI("0", 44.6290051, 10.8701162, "Current Location"); // cognento
        POI arrival = new POI("00", 44.6290051, 10.8701162, "Current Location");
        String start_time = "2017/05/18 18:46:58";
        List<String> POIsList = new ArrayList<>();
        POIsList.add("00");
        POIsList.add("44,64555122643570"); // centro
        POIsList.add("44,64160291752600"); // villaggio giardino

        //System.out.println(new FindPathAsIs().newPlan("Modena",dao,"marco",departure,arrival,start_time,POIsList));
        System.out.println(new FindShortestPath().newPlan("Modena",dao,"marco",departure,arrival,start_time,POIsList));
    }


    Logger logger = Logger.getLogger(FindPathAsIs.class);

    List<String> to_visit;

    public VisitPlan newPlan(String city, Mongo dao, String user, POI departure, POI arrival, String start_time, List<String> POIsList) {


        to_visit = new ArrayList<>();
        for (String poi : POIsList) {
            to_visit.add(poi);
        }

        System.out.println(to_visit);

        to_visit.remove(departure.getPlace_id());
        to_visit.remove(arrival.getPlace_id());

        logger.info("user:" + user);
        logger.info("start:" + departure.getPlace_id());
        logger.info("end:" + arrival.getPlace_id());
        logger.info("start_time:" + start_time);

        String[] poi_sequence = compute_poi_sequence(city, dao, user, departure, arrival, start_time, POIsList);



        // compute tot distance
        double tot_distance = computeTotDistance(departure,arrival, poi_sequence,city,dao);



        Calendar cal = null;
        try {
            cal = new GregorianCalendar();
            cal.setTime(SDF.parse(start_time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<Activity> activities = new ArrayList<Activity>();
        POI prev = null, p = null;
        for(int i=1; i<poi_sequence.length-1;i++) {

            Activity current = new Activity();

            prev =  poi_sequence[i-1].equals("0") ? departure :
                        poi_sequence[i-1].equals("00") ? arrival :
                        dao.retrieveActivity(city,poi_sequence[i-1]);

            p = poi_sequence[i].equals("0") ? departure :
                    poi_sequence[i].equals("00") ? arrival :
                    dao.retrieveActivity(city,poi_sequence[i]);

            double d = haverDist(
                    new double[] {prev.getGeometry().getCoordinates().getLatitude(), prev.getGeometry().getCoordinates().getLongitude()},
                    new double[] {p.getGeometry().getCoordinates().getLatitude(), p.getGeometry().getCoordinates().getLongitude()});

            int walking_t = (int)(d * 0.78); // gmaps says 1 m = 0.78 s
            cal.add(Calendar.SECOND,walking_t);
            current.setArrival_time(SDF.format(cal.getTime()));
            int visit_t = 5;
            cal.add(Calendar.SECOND,visit_t);
            current.setDeparture_time(SDF.format(cal.getTime()));
            current.setVisit(p);
            activities.add(current);
            System.out.println(current.getVisit().getPlace_id()+" arrive at: "+current.getArrival_time()+" depart at:"+current.getDeparture_time());
        }


        double d = haverDist(
                new double[] {p.getGeometry().getCoordinates().getLatitude(), p.getGeometry().getCoordinates().getLongitude()},
                new double[] {arrival.getGeometry().getCoordinates().getLatitude(), arrival.getGeometry().getCoordinates().getLongitude()});
        int walking_t = (int)(d * 0.78); // gmaps says 1 m = 0.78 s
        cal.add(Calendar.SECOND,walking_t);
        String end_time = SDF.format(cal.getTime());

        try {
            long dt = cal.getTimeInMillis() - SDF.parse(start_time).getTime();
            System.out.println("Itinerary lasts "+dt/(60*1000)+" minutes");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        logger.info("poi_sequence: "+getPoiSeqString(poi_sequence)+". tot_distance: "+tot_distance);
        return new VisitPlan(user, departure, arrival, start_time, end_time, activities, new ArrayList<Activity>(), 1);
    }

    public VisitPlan updatePlan(String city, Mongo dao, Visit last_visit, VisitPlan plan, List<String> POIsList) {
        return plan;
    }

    public double computeTotDistance(POI departure, POI arrival, String[] poi_sequence,String city, Mongo dao) {
        double tot_distance = 0d;
        POI from = departure;
        for(int i=1; i<poi_sequence.length-1;i++) {
            POI current = dao.retrieveActivity(city, poi_sequence[i]);
            tot_distance += haverDist(
                    new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()},
                    new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
            from = current;
        }
        tot_distance += haverDist(
                new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()},
                new double[] {arrival.getGeometry().getCoordinates().getLatitude(), arrival.getGeometry().getCoordinates().getLongitude()});
        return tot_distance;
    }

    public String getPoiSeqString(String[] poi_sequence) {
        String poi_sequence_string = "";
        for(String p: poi_sequence)
            poi_sequence_string += " --> "+p;
        poi_sequence_string = poi_sequence_string.substring(5);
       return poi_sequence_string;
    }

    public abstract String[] compute_poi_sequence(String city, Mongo dao, String user, POI departure, POI arrival, String start_time, List<String> POIsList);
}
