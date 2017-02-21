package services;

import io.CityData;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import util.*;
import model.Edge;
import model.Node;
import model.POI;

public class AstarPathFinding {
	static POI place_start;
	static POI place_end;
	static String time_start;
	private Logger logger = Logger.getLogger(AstarPathFinding.class);
	private static final double MAX_SPEED = 80; // [meter/minute] average walking speed wthout congestions
	//correlation factor between speed and congestion in Leisure/Shopping condition of pedestrians
	private static final double GAMMA = 0.245; //to be multiplied by MAX_CONGESTION times
	//see paper: Bruno, Venuti, "The pedestrian speed-density relation: modeling and application", Footbridge 2008.


		public static void main(String[] args) throws IOException {
			AstarPathFinding p = new AstarPathFinding();
			time_start = "09:00";

			CityData.DESKTOP_RUN = true;
			CityData cityData = new CityData("Modena");
			cityData.init();



			place_start = new POI("0",44.6290051,10.8701162, "Current Location");
			place_end = new POI("00",44.6290051,10.8701162, "Current Location");
			String [] POIsIDarr = new String [] {
					"00",
					"159280732"
			};

			int cont = 0;
			POI departure = place_start;
			POI arrival = place_end;

			List<String> POIsIDlist = new ArrayList<>();
			for (int i=0; i< POIsIDarr.length; i++) {
				POIsIDlist.add(POIsIDarr[i]);
			}


			List<Node> path = new AstarPathFinding().AstarSearch(cityData, departure, arrival, time_start, POIsIDlist, 10, 1);

			String[] sequence = new String[(departure.getPlace_id().equals(arrival.getPlace_id()) || (departure.getPlace_id().equals("0") && arrival.getPlace_id().equals("00"))) ? POIsIDlist.size() + 1 : POIsIDlist.size()];
			for (Node n : path) {
				sequence[cont++] = n.getName();
				//logger.info("sequence["+(cont-1)+"]"+sequence[cont-1]);
			}
			Map<String[], Double> solution = new HashMap<>();
			solution.put(sequence, 0D);

			Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
					departure.getPlace_id(),
					arrival.getPlace_id(),
					Graph.getSuccessorsList(solution));

			/*
			place_start = cityData.retrieveActivity("44791071");
			place_end = cityData.retrieveActivity("44791071");
			String [] POIsIDarr = new String [] {
					"44791071",
					"48034822"
			};
			*/


			POIsIDlist = new ArrayList<>();
			for (int i=0; i< POIsIDarr.length; i++) {
				POIsIDlist.add(POIsIDarr[i]);
			}

			new AstarPathFinding().AstarSearch(cityData,place_start,place_end,time_start,POIsIDlist,5,1);
		}

	private Node findStartNode(Map<String, Map<Integer, Node>> graph, String start) {
		for (String POIID : graph.keySet())
			if (POIID.equals(start))
				for (Integer nodeID : graph.get(POIID).keySet())
					if (graph.get(POIID).get(nodeID).getDepth() == 0)
						return graph.get(POIID).get(nodeID);
		return new Node();
	}

	
	@SuppressWarnings("unused")
	private void printPath(Map<String, Map<Integer, Node>> graph, Node target) {
		String out = "";
		List<Node> path = new ArrayList<>();
		path.add(target);
		for (Node POI = target; POI.getParent() != null; ) {
			POI = graph.get(POI.getParent().getName()).get(POI.getParent().getId());
			path.add(POI);
		}

		Collections.reverse(path);
		for (Node p : path) {
			//System.out.print(p.getName()+"("+p.getF_scores()+")\t");
			out += p.getName() + "(" + TimeUtils.getStringTime(p.getArrivalTime()) + "-" + TimeUtils.getStringTime(p.getDepartureTime()) + ")\t";
		}
		System.out.println(out);
	}

	private List<Node> getPath(Map<String, Map<Integer, Node>> graph, Node target) {
		List<Node> path = new ArrayList<>();
		path.add(target);
		Node POI = target;

		while (POI.getParent() != null) {
			POI = graph.get(POI.getParent().getName()).get(POI.getParent().getId());
			path.add(POI);
		}

		Collections.reverse(path);
		return path;
	}

	public List<Node> AstarSearch(
			CityData cityData,
			POI start_place,
			POI end_place,
			String start_time,
			List<String> POIsIDlist,
			//TreeMap<String, TreeMap<String, Double>> distances, Map<String, HashMap<String, List<UncertainValue>>> times,
			//Map<String, HashMap<String, List<UncertainValue>>> congestions,
			//Map<String, List<Integer>> occupancies,
			int num_TSP_solutions,
			double crowd_preference) throws IOException {
		//logger.info("POIsList:"+POIsIDlist.toString());
		Map<String, Map<Integer, Node>> graph = Graph.buildGraph(
				start_place.getPlace_id(),
				end_place.getPlace_id(),
				Graph.getSuccessorsList(new TabuSearchTSP().run(cityData, start_place, end_place, POIsIDlist, num_TSP_solutions)));

		final double MAX_CONGESTION = cityData.retrieveGridMaxCrowding();
		final double MAX_OCCUPANCY = cityData.findMaxOccupancy();
		/*
        A* Algorithm pseudocode
		1  Create a node containing the goal state node_goal
		2  Create a node containing the start state node_start
		3  Put node_start on the open list
		4  while the OPEN list is not empty
		5  {
		6  Get the node off the open list with the lowest f and call it node_current
		7  if node_current is the same state as node_goal we have found the solution; break from the while loop
		8      Generate each state node_successor that can come after node_current
		9      for each node_successor of node_current
		10      {
		11          Set the cost of node_successor to be the cost of node_current plus the cost to get to node_successor from node_current
		12          find node_successor on the OPEN list
		13          if node_successor is on the OPEN list but the existing one is as good or better then discard this successor and continue
		14          if node_successor is on the CLOSED list but the existing one is as good or better then discard this successor and continue
		15          Remove occurences of node_successor from OPEN and CLOSED
		16          Set the parent of node_successor to node_current
		17          Set h to be the estimated getDistance to node_goal (Using the heuristic function)
		18           Add node_successor to the OPEN list
		19      }
		20      Add node_current to the CLOSED list
		21  }
		 */

		Node source = findStartNode(graph, start_place.getPlace_id());
		String source_name;
		if (source.getName().equals("0")) {
			source_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
		} else {
			source_name = source.getName();
		}
		String goalPOI = end_place.getPlace_id();
		Node goal = null;

		int time_h = Integer.parseInt(start_time.split(":")[0]);
		int time_m = Integer.parseInt(start_time.split(":")[1]);


		Set<Node> explored = new HashSet<Node>();
		PriorityQueue<Node> queue = new PriorityQueue<Node>(100, new NodeComparator());

		//time = start
		source.setDepartureTime(time_h * 60 + time_m);

		queue.add(source);

		//logger.debug("From: "+source.getId());
		//logger.debug("To: "+goalPOI);
		while ((!queue.isEmpty())) {
			Node current = queue.poll(); //the POI in queue having the lowest f_score value

			explored.add(current);

			//goal found
			if (current.getName().equals(goalPOI) && current.getId() != source.getId()) {
				goal = current;
				break;
			}
			//check every child of current POI
			for (Edge e : current.getAdjacencies()) {
				Node child = e.getTarget();
				String current_name;
				String child_name;
				if (current.getName().equals("0")) {
					current_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
				} else if (current.getName().equals("00")) {
					current_name = cityData.retrieveClosestActivity(end_place).getPlace_id();
				} else {
					current_name = current.getName();
				}
				if (child.getName().equals("0")) {
					child_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
				} else if (child.getName().equals("00")) {
					child_name = cityData.retrieveClosestActivity(end_place).getPlace_id();
				} else {
					child_name = child.getName();
				}

				double time = cityData.getTime(current_name,child_name,current.getDepartureTime());
				double distance = cityData.getDistance(current_name,child_name);
				double congestion = cityData.getCrowd(current_name,child_name,current.getDepartureTime());

				double actual_speed = (distance/time) * (1d - Math.pow(Math.E, (-GAMMA*(1d/(congestion/MAX_CONGESTION)-1d))));

				double travel_cost_actual = 1 / actual_speed * distance;
				double travel_cost_pref = crowd_preference * travel_cost_actual;

				//double congestion_cost = round(crowd_preference * absolute_congestion_cost, 5);
			    //double cost = time * (1 + congestion_cost);

				double temp_arr_time_actual = ((current.getDepartureTime() + travel_cost_actual) >= 1440) ? ((current.getDepartureTime() + travel_cost_actual) - 1440) : (current.getDepartureTime() + travel_cost_actual);
				double temp_arr_time_pref = ((current.getDepartureTime() + travel_cost_pref) >= 1440) ? ((current.getDepartureTime() + travel_cost_pref) - 1440) : (current.getDepartureTime() + travel_cost_pref);

				double visit_time = cityData.getTime(child_name,child_name,(int)temp_arr_time_actual);
				double visit_distance = visit_time * MAX_SPEED;
				int occupancy = cityData.getOccupancy(child_name,(int) temp_arr_time_actual);

				double actual_visit_speed = MAX_SPEED * (1d - Math.pow(Math.E, (-GAMMA*(1d/(occupancy/MAX_OCCUPANCY)-1d))));

				double visit_cost_actual = 1 / actual_visit_speed * visit_distance;
				double visit_cost_pref = crowd_preference * visit_cost_actual;

//				double visit_cost = round(
//						RandomValue.get(times.get(child_name).get(child_name).get(TimeUtils.getTimeSlot((int) temp_arr_time_actual)))
//						+ RandomValue.get(times.get(child_name).get(child_name).get(TimeUtils.getTimeSlot((int) temp_arr_time_actual)))
//						* (crowd_preference
//								* (occupancies.get(child_name).get(TimeUtils.getTimeSlot((int) temp_arr_time_actual))
//										/ RandomValue.get(times.get(child_name).get(child_name).get(TimeUtils.getTimeSlot((int) temp_arr_time_actual)))
//										)
//								)
//								, 5);

				double temp_dep_time_actual = temp_arr_time_actual + visit_cost_actual;
				double temp_dep_time_pref = temp_arr_time_pref + visit_cost_pref;

				double time_h_cost = cityData.getTime(source_name,child_name,source.getDepartureTime());

				double temp_f_scores = temp_dep_time_pref + time_h_cost;

				/*if child POI has been evaluated and
                        the newer f_score is higher, skip*/
                        if ((explored.contains(child)) &&
                        		(temp_f_scores >= child.getF_scores())) {
                        	continue;
                        }

				/*else if child POI is not in queue or
                        newer f_score is lower*/
                        else if ((!queue.contains(child)) ||
                        		(temp_f_scores < child.getF_scores())) {
                        	child.setParent(current);
                        	child.setF_scores(temp_f_scores);
                        	child.setArrivalTime((int) temp_arr_time_actual);
                        	child.setDepartureTime((int) temp_dep_time_actual);
                        	child.setCongestion_score(congestion);
                        	if (queue.contains(child)) {
                        		queue.remove(child);
                        	}

                        	queue.add(child);
                        }
			}
		}

		if (null == goal) {
			throw new RuntimeException("Unable to find path");
		}

		//printPath(graph, goal);
		return getPath(graph, goal);
		//		return printPath(graph, goal);
	}





	public List<Node> AstarSearch(
			CityData cityData,
			POI start_place,
			POI end_place,
			String start_time,
			Map<String, Map<Integer, Node>> graph,
			double crowd_preference) throws IOException {

		final double MAX_CONGESTION = cityData.retrieveGridMaxCrowding();
		final double MAX_OCCUPANCY = cityData.findMaxOccupancy();

		//try {
		logger.info("start:" + start_place.getPlace_id());
		logger.info("end:" + end_place.getPlace_id());
		logger.info("start_time:" + start_time);
		logger.info("graph:" + graph);

		System.err.println("start:" + start_place.getPlace_id());
		System.err.println("end:" + end_place.getPlace_id());
		System.err.println("start_time:" + start_time);
		System.err.println("graph:" + graph);

		Node source = findStartNode(graph, start_place.getPlace_id());
		String source_name;
		if (source.getName().equals("0")) {
			source_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
		} else {
			source_name = source.getName();
		}
		String goalPOI = end_place.getPlace_id();
		Node goal = null;

		int time_h = Integer.parseInt(start_time.split(":")[0]);
		int time_m = Integer.parseInt(start_time.split(":")[1]);


		Set<Node> explored = new HashSet<Node>();
		PriorityQueue<Node> queue = new PriorityQueue<Node>(100, new NodeComparator());

		//time = start
		source.setDepartureTime(time_h * 60 + time_m);

		queue.add(source);

		//System.out.println("From Node: "+source.getId());
		//System.out.println("To Poi: "+goalPOI);
		while ((!queue.isEmpty())) {
			Node current = queue.poll(); //the POI in queue having the lowest f_score value

			explored.add(current);

			//goal found
			if (current.getName().equals(goalPOI) && current.getId() != source.getId()) {
				//System.out.println("\tTo Node: "+current.getId());
				goal = current;
				break;
			}
			//check every child of current POI
			for (Edge e : current.getAdjacencies()) {
				Node child = e.getTarget();
				String current_name;
				String child_name;
				if (current.getName().equals("0")) {
					current_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
				} else if (current.getName().equals("00")) {
					current_name = cityData.retrieveClosestActivity(end_place).getPlace_id();
				} else {
					current_name = current.getName();
				}

				if (child.getName().equals("0")) {
					child_name = cityData.retrieveClosestActivity(start_place).getPlace_id();
				} else if (child.getName().equals("00")) {
					child_name = cityData.retrieveClosestActivity(end_place).getPlace_id();
				} else {
					child_name = child.getName();
				}
				//logger.info("Travel time from "+current_name+" to "+child_name+" at "+TimeUtils.getTimeSlot(current.getDepartureTime()));
				//double time_cost = round(RandomValue.get(times.get(current_name).get(child_name).get(TimeUtils.getTimeSlot(current.getDepartureTime()))), 5);
				//the crowding matrix is triangular, so I look for couples in reverse order if the original order doesn't provide a correspondence

				//					logger.info("k1:"+cong_k1);
				//					logger.info("k2:"+cong_k2);
				//					logger.info("crowd_pref:"+crowd_preference);
				//					logger.info("departure:"+TimeUtils.getTimeSlot(current.getDepartureTime()));
				//					logger.info("congestion:"+congestions.get(cong_k1).get(cong_k2).get(TimeUtils.getTimeSlot(current.getDepartureTime())));
				//					logger.info("random:"+RandomValue.get(congestions.get(cong_k1).get(cong_k2).get(TimeUtils.getTimeSlot(current.getDepartureTime()))));

				System.err.println("\t\tfrom:"+current_name);
				System.err.println("\t\tto:"+child_name);
				//System.err.println(times);
				System.err.println("\t\t1:"+current.getDepartureTime());
				System.err.println("\t\t2:"+TimeUtils.getTimeSlot(current.getDepartureTime()));
				double time = cityData.getTime(current_name,child_name,current.getDepartureTime());
				//System.out.println("\t\ttime:"+time);
				double distance = cityData.getDistance(current_name,child_name);
				//System.out.println("\t\tgetDistance:"+getDistance);


				double congestion = cityData.getCrowd(current_name,child_name,current.getDepartureTime());
				//System.out.println("\t\tcongestion:"+congestion);
				double actual_speed = (distance/time) * (1d - Math.pow(Math.E, (-GAMMA*(1d/(congestion/MAX_CONGESTION)-1d))));
				//System.out.println("\t\tactual_speed:"+actual_speed);
				double travel_cost_actual = 1 / actual_speed * distance;
				//System.out.println("\t\ttravel_cost_actual:"+travel_cost_actual);
				double travel_cost_pref = crowd_preference * travel_cost_actual;
				//System.out.println("\t\ttravel_cost_pref:"+travel_cost_pref);

				//double congestion_cost = round(crowd_preference * absolute_congestion_cost, 5);
			    //double cost = time * (1 + congestion_cost);

				double temp_arr_time_actual = ((current.getDepartureTime() + travel_cost_actual) >= 1440) ? ((current.getDepartureTime() + travel_cost_actual) - 1440) : (current.getDepartureTime() + travel_cost_actual);
				//System.out.println("\t\ttemp_arr_time_actual:"+temp_arr_time_actual);
				double temp_arr_time_pref = ((current.getDepartureTime() + travel_cost_pref) >= 1440) ? ((current.getDepartureTime() + travel_cost_pref) - 1440) : (current.getDepartureTime() + travel_cost_pref);
				//System.out.println("\t\ttemp_arr_time_pref:"+temp_arr_time_pref);

				double visit_time = cityData.getTime(child_name,child_name,(int) temp_arr_time_actual);
				//System.out.println("\t\tvisit_time:"+visit_time);
				double visit_distance = visit_time * MAX_SPEED;
				//System.out.println("\t\tvisit_distance:"+visit_distance);
				int occupancy = cityData.getOccupancy(child_name,(int) temp_arr_time_actual);
				//System.out.println("\t\toccupancy:"+occupancy);

				double actual_visit_speed = MAX_SPEED * (1d - Math.pow(Math.E, (-GAMMA*(1d/(occupancy/MAX_OCCUPANCY)-1d))));
				//System.out.println("\t\tactual_visit_speed:"+actual_visit_speed);
				double visit_cost_actual = 1 / actual_visit_speed * visit_distance;
				//System.out.println("\t\tvisit_cost_actual:"+visit_cost_actual);
				double visit_cost_pref = crowd_preference * visit_cost_actual;
				//System.out.println("\t\tvisit_cost_pref:"+visit_cost_pref);

				double temp_dep_time_actual = temp_arr_time_actual + visit_cost_actual;
				//System.out.println("\t\ttemp_dep_time_actual:"+temp_dep_time_actual);
				double temp_dep_time_pref = temp_arr_time_pref + visit_cost_pref;
				//System.out.println("\t\ttemp_dep_time_pref:"+temp_dep_time_pref);

				double time_h_cost = cityData.getTime(source_name,child_name,source.getDepartureTime());
				//System.out.println("\t\ttime_h_cost:"+time_h_cost);
				double temp_f_scores = temp_dep_time_pref + time_h_cost;
				//System.out.println("\t\ttemp_f_scores:"+temp_f_scores);


				/*if child POI has been evaluated and
                        the newer f_score is higher, skip*/
				if ((explored.contains(child)) &&
						(temp_f_scores >= child.getF_scores())) {
					continue;
				}

				/*else if child POI is not in queue or
                        newer f_score is lower*/
				else if ((!queue.contains(child)) ||
						(temp_f_scores < child.getF_scores())) {
					child.setParent(current);
					child.setF_scores(temp_f_scores);
					child.setArrivalTime((int) temp_arr_time_actual);
					child.setDepartureTime((int) temp_dep_time_actual);
					child.setCongestion_score(congestion);

					if (queue.contains(child)) {
						queue.remove(child);
					}

					queue.add(child);
				}
			}
		}

		if (null == goal) {
			throw new RuntimeException("Unable to find path");
		}

		//printPath(graph, goal);
		return getPath(graph, goal);
	}
}
