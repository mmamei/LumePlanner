package services;

import io.CityData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import util.Graph;
import util.TimeUtils;
import util.TravelPath;
import model.Activity;
import model.Node;
import model.POI;
import model.Path;
import model.Visit;
import model.VisitPlan;

import static util.Misc.haverDist;

public class FindGreedyPath {

	private Logger logger = Logger.getLogger(FindGreedyPath.class);

	private List<String> to_visit;

	public VisitPlan newPlan(CityData cityData, String user, POI departure, POI arrival, String start_time, List<String> POIsList) {
		to_visit = new ArrayList<String>();
		for (String poi : POIsList) {
			to_visit.add(poi);
		}
		logger.info("user:"+user);
		logger.info("start:"+departure.getPlace_id());
		logger.info("end:"+arrival.getPlace_id());
		logger.info("start_time:"+start_time);
		Map<String[], Double> solution = new HashMap<>();
		String[] poi_sequence = new String [(departure.getPlace_id().equals(arrival.getPlace_id()) || (departure.getPlace_id().equals("0") && arrival.getPlace_id().equals("00"))) ? to_visit.size()+1 : to_visit.size()];
		logger.info("sequence:"+poi_sequence.length);
		int cont = 0;
		poi_sequence[cont++] = departure.getPlace_id();

		to_visit.remove(departure.getPlace_id());
		to_visit.remove(arrival.getPlace_id());

		double tot_distance = 0d;
		POI from = departure;
		while (!to_visit.isEmpty()) {
			double min_distance = Double.MAX_VALUE;
			POI closest = null;
			for (String poi : to_visit) {
				POI current = cityData.getPOI(poi);

				double current_distance = haverDist(
						new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
						new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
				if (current_distance < min_distance) {
					min_distance = current_distance;
					closest = current;
				}
			}
			poi_sequence[cont++] = closest.getPlace_id();
			tot_distance += min_distance;
			to_visit.remove(closest.getPlace_id());
			from = closest;
		}
		tot_distance += haverDist(
				new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
				new double[] {arrival.getGeometry().getCoordinates().getLatitude(), arrival.getGeometry().getCoordinates().getLongitude()});
		poi_sequence[cont++] = arrival.getPlace_id();


		String poi_sequence_string = "";
		for(String p: poi_sequence)
			poi_sequence_string += p+" --> ";

		logger.info("poi_sequence: "+poi_sequence_string+" tot_distance: "+tot_distance);


		solution.put(poi_sequence, tot_distance);
		
		Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
				departure.getPlace_id(),
				arrival.getPlace_id(),
				Graph.getSuccessorsList(solution));

		POI to = null;
		List<Node> path = new ArrayList<Node>();
		List<Activity> activities = new ArrayList<Activity>();
		try {
			path = new AstarPathFinding().AstarSearch(cityData, departure, arrival, start_time, graph, 1d);
			logger.info("Greedy path with real cong.levels :"+path.get(0).getName()+"-"+path.get(path.size()-1).getName()+":"+path.get(path.size()-1).getF_scores());
		} catch (Exception e) {
			e.printStackTrace();
			activities.add(new Activity("-1"));
			return new VisitPlan();
		}
		from = null;
		to = null;
		double congestion = 0d;
		Path path_points = new Path();
		for (Node n : path) {
			congestion +=n.getCongestion_score();
			Activity current = new Activity();
			POI p = null;
			if (n.getName().equals("0") || n.getName().equals("00")) {
				p = (n.getName().equals("0")) ? departure : arrival;
			} else {
				p = cityData.getPOI(n.getName());
			}
			current.setDeparture_time(TimeUtils.getStringTime(n.getDepartureTime()));
			current.setArrival_time(TimeUtils.getStringTime(n.getArrivalTime()));
			current.setVisit(p);
			if (from != null) {
				to = p;
			}
			if (to != null) {
				Path subpath = new TravelPath().compute(cityData.hopper, from, to);
				path_points.addPoints(subpath.getPoints());
				path_points.incrementLength(subpath.getLength());
			}
			from = p;
			activities.add(current);
		}

		String departure_time = activities.get(0).getDeparture_time();
		String arrival_time = activities.get(activities.size()-1).getArrival_time();
		activities.remove(0);
		activities.remove(activities.size()-1);

		return new VisitPlan(user, departure, arrival, departure_time, arrival_time, activities, new ArrayList<Activity>(), path_points, congestion/(activities.size()+1d));

	}


	public VisitPlan updatePlan(CityData cityData, Visit last_visit, VisitPlan plan, List<String> POIsList) {
		to_visit = new ArrayList<String>();
		for (String poi : POIsList) {
			to_visit.add(poi);
		}
		logger.info("user:"+plan.getUser());
		logger.info("start:"+last_visit.getVisited().getPlace_id());
		logger.info("end:"+plan.getArrival().getPlace_id());
		logger.info("start_time:"+plan.getDeparture_time());
		Map<String[], Double> solution = new HashMap<>();
		String[] poi_sequence = new String [(last_visit.getVisited().equals(plan.getArrival())) ? to_visit.size()+1 : to_visit.size()];
		int cont = 0;
		poi_sequence[cont++] = last_visit.getVisited().getPlace_id();

		to_visit.remove(last_visit.getVisited().getPlace_id());
		to_visit.remove(plan.getArrival().getPlace_id());

		double tot_distance = 0d;
		POI from = last_visit.getVisited();
		while (!to_visit.isEmpty()) {
			double min_distance = Double.MAX_VALUE;
			POI closest = null;
			for (String poi : to_visit) {
				POI current = cityData.getPOI(poi);

				double current_distance = haverDist(
						new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
						new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
				if (current_distance < min_distance) {
					min_distance = current_distance;
					closest = current;
				}
			}
			poi_sequence[cont++] = closest.getPlace_id();
			tot_distance += min_distance;
			to_visit.remove(closest.getPlace_id());
			from = closest;
		}
		
		tot_distance += haverDist(
				new double[] {from.getGeometry().getCoordinates().getLatitude(), from.getGeometry().getCoordinates().getLongitude()}, 
				new double[] {plan.getArrival().getGeometry().getCoordinates().getLatitude(), plan.getArrival().getGeometry().getCoordinates().getLongitude()});
		logger.info("tot_distance:"+tot_distance);
		poi_sequence[cont++] = plan.getArrival().getPlace_id();

		solution.put(poi_sequence, tot_distance);

		Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
				last_visit.getVisited().getPlace_id(),
				plan.getArrival().getPlace_id(),
				Graph.getSuccessorsList(solution));

		POI to = null;
		List<Node> path = new ArrayList<Node>();
		List<Activity> activities = new ArrayList<Activity>();
		try {
			path = new AstarPathFinding().AstarSearch(cityData, last_visit.getVisited(), plan.getArrival(), TimeUtils.getStringTime(last_visit.getTime()%86400000L), graph, 1d);
			logger.info("Greedy path with real cong.levels :"+path.get(0).getName()+"-"+path.get(path.size()-1).getName()+":"+path.get(path.size()-1).getF_scores());
		} catch (Exception e) {
			e.printStackTrace();
			activities.add(new Activity("-1"));
			return new VisitPlan();
		}
		from = null;
		to = null;
		double congestion = 0d;
		Path path_points = new Path();
		for (Node n : path) {
			congestion +=n.getCongestion_score();
			Activity current = new Activity();
			POI p = null;
			if (n.getName().equals("0") || n.getName().equals("00")) {
				p = (n.getName().equals("0")) ? last_visit.getVisited() : plan.getArrival();
			} else {
				p = cityData.getPOI(n.getName());
			}
			current.setDeparture_time(TimeUtils.getStringTime(n.getDepartureTime()));
			current.setArrival_time(TimeUtils.getStringTime(n.getArrivalTime()));
			current.setVisit(p);
			if (from != null) {
				to = p;
			}
			if (to != null) {
				Path subpath = new TravelPath().compute(cityData.hopper, from, to);
				path_points.addPoints(subpath.getPoints());
				path_points.incrementLength(subpath.getLength());
			}
			from = p;
			activities.add(current);
		}
		activities.remove(0);
		activities.remove(activities.size()-1);
		//logger.info("activities:"+activities.toString());
		congestion = ((congestion/(activities.size()+1d))+(plan.getCrowding()*plan.getVisited().size()))/(plan.getVisited().size()+1d);
		plan.setTo_visit(activities);
		plan.setCrowding(congestion);

		plan.setPath(path_points);
		//logger.info(plan.toJSONString());
		plan.computeHash();
		return plan;

	}
}
