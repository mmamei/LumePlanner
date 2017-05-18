package services.pathfinder;

import io.Mongo;
import model.Activity;
import model.POI;
import model.Visit;
import model.VisitPlan;
import org.apache.log4j.Logger;
import sun.util.resources.cldr.naq.CalendarData_naq_NA;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static util.Misc.haverDist;

/**
 * Created by marco on 19/04/2017.
 */
public class FindPathAsIs {

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

        System.out.println(new FindPathAsIs().newPlan("Modena",dao,"marco",departure,arrival,start_time,POIsList));

    }



    private Logger logger = Logger.getLogger(FindPathAsIs.class);

    private List<String> to_visit;

    public VisitPlan newPlan(String city, Mongo dao, String user, POI departure, POI arrival, String start_time, List<String> POIsList) {
        Calendar cal = null;
        try {
            cal = new GregorianCalendar();
            cal.setTime(SDF.parse(start_time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        to_visit = new ArrayList<>();
        for (String poi : POIsList) {
            to_visit.add(poi);
        }
        logger.info("user:" + user);
        logger.info("start:" + departure.getPlace_id());
        logger.info("end:" + arrival.getPlace_id());
        logger.info("start_time:" + start_time);

        String[] poi_sequence = new String [(departure.getPlace_id().equals(arrival.getPlace_id()) || (departure.getPlace_id().equals("0") && arrival.getPlace_id().equals("00"))) ? to_visit.size()+1 : to_visit.size()];
        logger.info("sequence:"+poi_sequence.length);
        int cont = 0;
        poi_sequence[cont++] = departure.getPlace_id();

        to_visit.remove(departure.getPlace_id());
        to_visit.remove(arrival.getPlace_id());

        double tot_distance = 0d;
        POI from = departure;
        for(String poi: to_visit){
            poi_sequence[cont++] = poi;
            POI current = dao.retrieveActivity(city, poi_sequence[cont-1]);
            tot_distance += haverDist(
                            new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()},
                            new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
            from = current;
        }
        tot_distance += haverDist(
                new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()},
                new double[] {arrival.getGeometry().getCoordinates().getLatitude(), arrival.getGeometry().getCoordinates().getLongitude()});
        poi_sequence[cont++] = arrival.getPlace_id();


        String poi_sequence_string = "";
        for(String p: poi_sequence)
            poi_sequence_string += p+" --> ";

        System.out.println(poi_sequence_string);

        List<Activity> activities = new ArrayList<Activity>();
        for(int i=1; i<poi_sequence.length-1;i++) {
            String ps = poi_sequence[i];
            Activity current = new Activity();
            POI p = null;
            if (ps.equals("0") || ps.equals("00")) {
                p = (ps.equals("0")) ? departure : arrival;
            } else {
                p = dao.retrieveActivity(city,ps);
            }
            cal.add(Calendar.MINUTE,20);
            current.setDeparture_time(SDF.format(cal.getTime()));
            cal.add(Calendar.MINUTE,20);
            current.setArrival_time(SDF.format(cal.getTime()));
            current.setVisit(p);
            activities.add(current);
        }

        cal.add(Calendar.MINUTE,20);

        logger.info("poi_sequence: "+poi_sequence_string+" tot_distance: "+tot_distance);
        return new VisitPlan(user, departure, arrival, start_time, SDF.format(cal.getTime()), activities, new ArrayList<Activity>(), 1);




    }

    public VisitPlan updatePlan(String city, Mongo dao, Visit last_visit, VisitPlan plan, List<String> POIsList) {
        return plan;
    }
}
