package io;

import citylive.DataPipeDownload;
import model.*;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import services.FindSimplePath;
import services.SaveItineraries2DB;
import services.SavePOIs2DB;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/")
public class RESTController {

	//private static RestTemplate restTemplate;
	private Logger logger = Logger.getLogger(RESTController.class);

	private  GHopper gHopper;
	private Mongo dao;

	public RESTController() {
		logger.info("Server initialization started");
		dao = new Mongo();
		gHopper = new GHopper();

		for(CityProperties cp: CityProperties.getInstance(this.getClass().getResource("/../data/cities.csv").getPath())) {
			String city = cp.getName();
			if (!dao.checkActivities(city)) {
				logger.info("/../data/"+cp.getDataDir());
				//String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois";
				new SavePOIs2DB().run(city, dao, this.getClass().getResource("/../data/"+cp.getDataDir()+"/pois").getPath());
				logger.info("POIs collected from OSM API");
			}
			new SaveItineraries2DB().run(city, dao,this.getClass().getResource("/../data/"+cp.getDataDir()).getPath()+"/itineraries.json");
		}

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
	}
	

	@RequestMapping(value = "signin", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody Integer performLogin(@RequestBody User user) {
		return dao.login(user);
	}

	@RequestMapping(value = "signup", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean performSignup(@RequestBody User user) {
		return dao.signup(user);
	}


	@RequestMapping(value = "activities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI> sendActivities(@RequestParam(value="city", defaultValue="unknown") String city) {
		return dao.retrieveActivities(city);
	}

	@RequestMapping(value = "itineraries", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<Itinerary> sendItineraries(@RequestParam(value="city", defaultValue="unknown") String city) {
		logger.info(city);
		return dao.retrieveItineraries(city);
	}


	@RequestMapping(value = "route", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody Path route(@RequestParam(value="vechicle", defaultValue="foot") String vechicle,
									@RequestParam(value="start", defaultValue="unknown") String start,
									@RequestParam(value="end", defaultValue="unknown") String end) {
		String[] s = start.split(",");
		String[] e = end.split(",");
		return gHopper.route(vechicle, Double.parseDouble(s[0]),Double.parseDouble(s[1]),Double.parseDouble(e[0]),Double.parseDouble(e[1]));
	}


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


		VisitPlan simple = new FindSimplePath().newPlan(city,dao,plan_request.getUser(), departure, arrival, start_time, POIsList);
		logger.info("Simple computed " + simple.getUser());

		logger.info("newPlan user:"+plan_request.getUser());
		//return new VisitPlanAlternatives(city, greedy, shortest, lesscrowded, plan_request.getCrowd_preference());
		return new VisitPlanAlternatives(city, simple, simple, simple, plan_request.getCrowd_preference());
	}


	@RequestMapping(value = "accept_plan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean acceptVisitPlan(@RequestBody VisitPlanAlternatives plans) {
		String city = plans.getCity();
		return acceptVisitPlanWithType(0, plans);
	}

	public boolean acceptVisitPlanWithType(int type, VisitPlanAlternatives plans) {
		if (!dao.insertPlan(plans)) return false;
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
		return dao.retrievePlan(user.getEmail());
	}



	@RequestMapping(value = "visited", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives addVisitedAndReplan(@RequestBody Visit new_visited) {
		String city = new_visited.getCity();
		logger.info("++++++++++++++++++++++"+city);
		return addVisitedAndReplanWithType(0, new_visited);

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


	public VisitPlanAlternatives addVisitedAndReplanWithType(int type, Visit new_visited) {
		VisitPlanAlternatives plans = dao.updatePlan(new_visited);
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
				newP = new FindSimplePath().updatePlan(city,dao, new_visited, plans.getGreedy(), pois);
				break;
			case 2: // shortest
				newP = new FindSimplePath().updatePlan(city,dao, new_visited, plans.getShortest(), pois);
				break;
			default: //0
				newP = new FindSimplePath().updatePlan(city,dao, new_visited, plans.getCrowd_related(), pois);
			}


			switch (type) {
			case 1: // greedy
				return new VisitPlanAlternatives(
						city,
						newP,
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getShortest(), pois),
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getCrowd_related(), pois),
						plans.getCrowd_preference());
			case 2: // shortest
				return new VisitPlanAlternatives(
						city,
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getGreedy(), pois),
						newP,
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getCrowd_related(), pois),
						plans.getCrowd_preference());
			default: //0
				return new VisitPlanAlternatives(
						city,
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getGreedy(), pois),
						new FindSimplePath().updatePlan(city,dao, new_visited, plans.getShortest(), pois), newP, plans.getCrowd_preference());
			}
			
		}

		return plans;
	}

	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody User user) {
		logger.info("User "+user.getEmail()+" completed his visiting plan in "+user.getCity());
		String city = user.getCity();
		return dao.deletePlan(user.getEmail());
	}
}