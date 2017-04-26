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

        logger.info("USER: "+plan_request.getUser()+"   "+"TIME: "+plan_request.getStart_time());
        logger.info("CITY: "+city);
        logger.info("PLAN REQUEST: "+POIsList.toString());
        logger.info("DEP: "+departure.toString());
        logger.info("ARR: "+arrival.toString());


        VisitPlan shortest = new FindShortestPath().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList);
        VisitPlan asis = new FindPathAsIs().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList);
        VisitPlan crowd = shortest;
        return new VisitPlanAlternatives(city, asis, shortest, crowd, plan_request.getCrowd_preference());
    }

    public VisitPlanAlternatives addVisitedAndReplanWithType(Mongo dao, Visit new_visited) {
        VisitPlanAlternatives plans = dao.updatePlan(new_visited);
        if (null == plans) return null;

        String selectedPlan = plans.getSelected();

        //logger.info(plans.toString());
        VisitPlan currentP = null;


        String city = new_visited.getCity();


        if(selectedPlan.equals("asis")) currentP = plans.getAsis();
        if(selectedPlan.equals("shortest")) currentP = plans.getShortest();
        if(selectedPlan.equals("crowd")) currentP = plans.getCrowd();

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



            return new VisitPlanAlternatives(
                    city,
                    new FindShortestPath().updatePlan(city,dao, new_visited, plans.getAsis(), pois),
                    new FindShortestPath().updatePlan(city,dao, new_visited, plans.getShortest(), pois),
                    new FindShortestPath().updatePlan(city,dao, new_visited, plans.getCrowd(), pois),
                    plans.getCrowd_preference());
        }

        return plans;
    }

}
