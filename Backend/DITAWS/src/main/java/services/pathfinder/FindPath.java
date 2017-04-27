package services.pathfinder;

import io.Mongo;
import model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 21/04/2017.
 */
public class FindPath {
    private Logger logger = Logger.getLogger(FindPath.class);
    public VisitPlanAlternatives getNewVisitPlan(Mongo dao, PlanRequest plan_request) {
        String user = plan_request.getUser();
        String city = plan_request.getCity();
        POI start = plan_request.getStart_place();
        POI end = plan_request.getEnd_place();
        String start_time = plan_request.getStart_time();
        List<String> pois = plan_request.getVisits();
        List<String> POIsList = new ArrayList<String>();
        POI departure = null;
        POI arrival = null;

        if (start.getPlace_id().equals("0")) {
            departure = new POI("0",
                    start.getGeometry().getCoordinates().getLatitude(),
                    start.getGeometry().getCoordinates().getLongitude(),
                    "Current Location");
        } else {
            departure = start;
        }
        if (end.getPlace_id().equals("0")) {
            arrival = new POI("00",
                    end.getGeometry().getCoordinates().getLatitude(),
                    end.getGeometry().getCoordinates().getLongitude(),
                    "Current Location");
        } else {
            arrival = end;
        }
        //if start!=end insert both id in the list
        if (!start.getPlace_id().equals(end.getPlace_id()) &&
                !(start.getPlace_id().equals("0") && end.getPlace_id().equals("00"))) {
            POIsList.add(departure.getPlace_id());
        }
        //insert only one, otherwise
        POIsList.add(arrival.getPlace_id());

        for (String poi : pois) {
            POIsList.add(poi);
        }

        logger.info("USER: "+user+"   "+"TIME: "+start_time);
        logger.info("CITY: "+city);
        logger.info("PLAN REQUEST: "+POIsList.toString());
        logger.info("DEP: "+departure.toString());
        logger.info("ARR: "+arrival.toString());

        VisitPlanAlternatives vpa = new VisitPlanAlternatives(city,user);
        vpa.add("shortest", new FindShortestPath().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList));
        vpa.add("asis",  new FindPathAsIs().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList));
        vpa.add("crowd", new FindShortestPath().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList));

        return vpa;
    }

    public VisitPlanAlternatives addVisitedAndReplanWithType(Mongo dao, Visit new_visited) {
        VisitPlanAlternatives plans = dao.updatePlan(new_visited);
        if (null == plans) return null;

        String selectedPlan = plans.getSelected();

        //logger.info(plans.toString());
        VisitPlan currentP = plans.get(selectedPlan);

        String user = plans.getUser();
        String city = plans.getCity();


        //logger.info(v.toString());
        if (!currentP.getTo_visit().isEmpty()) {

            List<String> pois = new ArrayList<String>();

            for (Activity to_visit : currentP.getTo_visit()) {
                pois.add(to_visit.getVisit().getPlace_id());
            }
            if (!currentP.getArrival().equals(new_visited.getVisited())) {
                pois.add(new_visited.getVisited().getPlace_id());
            }
            pois.add(currentP.getArrival().getPlace_id());


            VisitPlanAlternatives newVPA = new VisitPlanAlternatives(city,user);
            for(String key : plans.getPlans().keySet()) {
                VisitPlan p = plans.get(key);
                newVPA.add(key,new FindShortestPath().updatePlan(city,dao, new_visited, p, pois));  // <-- problema!!!
            }
            return newVPA;
        }

        return plans;
    }

}
