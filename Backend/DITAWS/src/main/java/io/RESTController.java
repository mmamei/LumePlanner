package io;

import citylive.DataPipeDownload;
import model.*;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import services.FindSimplePath;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/")
public class RESTController {

	//private static RestTemplate restTemplate;
	private Logger logger = Logger.getLogger(RESTController.class);
	private boolean initialized = false;

	private static Map<String,CityData> cityDataMap;

	public RESTController() {
		cityDataMap = new HashMap<>();
		for(String city: CityProp.getInstance().keySet())
			cityDataMap.put(city,new CityData(city));
		init();
	}
	

	@RequestMapping(value = "signin", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody Integer performLogin(@RequestBody User user) {
		String city = "Modena";
		return cityDataMap.get(city).login(user);
	}

	@RequestMapping(value = "signup", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean performSignup(@RequestBody User user) {
		String city = "Modena";
		return cityDataMap.get(city).signup(user);
	}


	/**
	 * Retrieve POIs, compute haversine distances, integrate POIs with visiting times from venice
	 * @return Status of the initialisation process
	 * @throws IOException 
	 */
	public void init() {
		logger.info("Server initialization started");

		/*
		try {
			DisableSSLCertificateCheckUtil.disableChecks();
			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(new TrustSelfSignedCertHttpClientFactory().getObject());
			restTemplate = new RestTemplate(requestFactory);
			//restTemplate = new RestTemplate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		for(String city : cityDataMap.keySet()) {
			logger.info("\n*********************************** "+city+" ***********************************\n");
			cityDataMap.get(city).init();
		}

		initialized = true;

		logger.info("\n\n\n\t\t*********************************************\n"
				+ "\t\t*******Server successfully initialized*******\n"
				+ "\t\t*********************************************\n\n\n");

	}


	/**
	 * Load the POIs from the DB (if necessary) and compute the congestion_levels and the travel_times for the day
	 * @return Status of the update process
	 * @throws IOException 
	 */
	@RequestMapping(value = "activities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI> sendActivities(@RequestParam(value="city", defaultValue="unknown") String city) {
		logger.info(city);
		return cityDataMap.get(city).retrieveActivities();
	}


	/**
	 * Compute the visiting plan for the POIs 
	 * @param plan_request sent w/ POST containing the list of POIs selected from user in json format
	 * @return Suggested visiting sequence for the requested set of POIs
	 */

	@RequestMapping(value = "newplan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody VisitPlanAlternatives getNewVisitPlan(@RequestBody PlanRequest plan_request) {
		String city = plan_request.getCity();
		POI start = plan_request.getStart_place();
		POI end = plan_request.getEnd_place();
		String start_time = plan_request.getStart_time();
		List<String> pois = plan_request.getVisits();
		List<String> POIsList = new ArrayList<String>();
		POI departure = null;
		POI arrival = null;


		//List<Activity> result = new ArrayList<Activity>();
		if (start.getPlace_id().equals("0")) {
			//if (!new PointWithinBBox(city).check(start.getGeometry().getCoordinates())) {
				//result.add(new Activity("0"));
			//	return new VisitPlanAlternatives(city,new VisitPlan(), new VisitPlan(), new VisitPlan(new POI("0")), 0);
			//}
			departure = new POI("0", 
					start.getGeometry().getCoordinates().getLatitude(), 
					start.getGeometry().getCoordinates().getLongitude(), 
					"Current Location");
		} else {
			//departure = dao.retrieveActivity(start.getPlace_id());
			departure = start;
		}
		if (end.getPlace_id().equals("0")) {
			//if (!new PointWithinBBox(city).check(end.getGeometry().getCoordinates())) {
			//	//result.add(new Activity("00"));
			//	return new VisitPlanAlternatives(city, new VisitPlan(), new VisitPlan(), new VisitPlan(new POI("00")), 0);
			//}
			arrival = new POI("00", 
					end.getGeometry().getCoordinates().getLatitude(), 
					end.getGeometry().getCoordinates().getLongitude(), 
					"Current Location");
		} else {
			//arrival = dao.retrieveActivity(end.getPlace_id());
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
		logger.info("PLAN REQUEST: "+POIsList.toString()+"\n");
		logger.info("DEP: "+departure.toString());
		logger.info("ARR: "+arrival.toString());


		VisitPlan simple = new FindSimplePath().newPlan(cityDataMap.get(city), plan_request.getUser(), departure, arrival, start_time, POIsList);
		logger.info("Simple computed " + simple.getUser());

		logger.info("newPlan user:"+plan_request.getUser());
		//return new VisitPlanAlternatives(city, greedy, shortest, lesscrowded, plan_request.getCrowd_preference());
		return new VisitPlanAlternatives(city, simple, simple, simple, plan_request.getCrowd_preference());
	}


	@RequestMapping(value = "accept_plan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean acceptVisitPlan(@RequestBody VisitPlanAlternatives plans) {
		String city = plans.getCity();
		return acceptVisitPlanWithType(cityDataMap.get(city), 0, plans);
	}

	public boolean acceptVisitPlanWithType(CityData cityData, int type, VisitPlanAlternatives plans) {
		if (!cityData.insertPlan(plans)) return false;

		VisitPlan plan_accepted = null;

		switch (type) {
		case 1: // greedy
			plan_accepted = plans.getGreedy();
			break;
		case 2: // shortest
			plan_accepted = plans.getShortest();
			break;
		default:
			plan_accepted = plans.getCrowd_related();
		}
		return true;
	}


	@RequestMapping(value = "plan", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives getPlan(@RequestBody User user) {
		String city = "Modena";
		return cityDataMap.get(city).retrievePlan(user.getEmail());
	}



	@RequestMapping(value = "visited", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives addVisitedAndReplan(@RequestBody Visit new_visited) {
		String city = new_visited.getCity();
		logger.info("++++++++++++++++++++++"+city);
		return addVisitedAndReplanWithType(cityDataMap.get(city), 0, new_visited);

	}


	@Scheduled(fixedRate = 300000) // every fife minutes
	public void downloadData() {

		DateFormat hourFormatter = new SimpleDateFormat("hh");
		DateFormat minuteFormatter = new SimpleDateFormat("mm");
		Date d = new Date();
		hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		String hour = hourFormatter.format(d);
		String minute = minuteFormatter.format(d);

		logger.info("download datapipe data at "+hour+":"+minute);

		new DataPipeDownload().download();
	}


	public VisitPlanAlternatives addVisitedAndReplanWithType(CityData cityData, int type, Visit new_visited) {
		VisitPlanAlternatives plans = cityData.updatePlan(new_visited);
		if (null == plans) return null;

		//logger.info(plans.toString());
		VisitPlan currentP = null;


		String city = new_visited.getCity();

		switch (type) {
		case 1: // greedy
			currentP = plans.getGreedy();
			break;
		case 2: // shortest
			currentP = plans.getShortest();
			break;
		default: //0
			currentP = plans.getCrowd_related();			
		}

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

			VisitPlan newP = null;
			
			switch (type) {
			case 1: // greedy
				newP = new FindSimplePath().updatePlan(cityData, new_visited, plans.getGreedy(), pois);
				break;
			case 2: // shortest
				newP = new FindSimplePath().updatePlan(cityData, new_visited, plans.getShortest(), pois);
				break;
			default: //0
				newP = new FindSimplePath().updatePlan(cityData, new_visited, plans.getCrowd_related(), pois);
			}


			switch (type) {
			case 1: // greedy
				return new VisitPlanAlternatives(
						city,
						newP,
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getShortest(), pois),
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getCrowd_related(), pois),
						plans.getCrowd_preference());
			case 2: // shortest
				return new VisitPlanAlternatives(
						city,
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getGreedy(), pois),
						newP,
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getCrowd_related(), pois),
						plans.getCrowd_preference());
			default: //0
				return new VisitPlanAlternatives(
						city,
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getGreedy(), pois),
						new FindSimplePath().updatePlan(cityData, new_visited, plans.getShortest(), pois), newP, plans.getCrowd_preference());
			}
			
		}

		return plans;
	}

	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody User user) {
		logger.info("User "+user.getEmail()+" completed his visiting plan in "+user.getCity());
		String city = user.getCity();
		return cityDataMap.get(city).deletePlan(user.getEmail());
	}
}