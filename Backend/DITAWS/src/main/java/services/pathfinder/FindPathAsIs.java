package services.pathfinder;

import io.Mongo;
import model.Activity;
import model.POI;
import model.Visit;
import model.VisitPlan;
import org.apache.http.annotation.Obsolete;
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
public class FindPathAsIs extends FindPathAbstract {

    @Override
    public String[] compute_poi_sequence(String city, Mongo dao, String user, POI departure, POI arrival, String start_time, List<String> POIsList) {

        //boolean closed_circuit = (departure.getPlace_id().equals(arrival.getPlace_id()) || (departure.getPlace_id().equals("0") && arrival.getPlace_id().equals("00")));
        String[] poi_sequence = new String [to_visit.size()+2];
        logger.info("sequence:"+poi_sequence.length);
        int cont = 0;
        poi_sequence[cont++] = departure.getPlace_id();
        for(String poi: to_visit)
            poi_sequence[cont++] = poi;
        poi_sequence[cont++] = arrival.getPlace_id();
        return poi_sequence;
    }
}
