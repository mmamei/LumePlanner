package io;

import org.json.JSONObject;
import services.CheckUser;
import services.timdatapipe.DataPipeDownload;
import model.*;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import services.pathfinder.FindPath;
import services.SaveItineraries2DB;
import services.SavePOIs2DB;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/")
public class RESTController {

	//private static RestTemplate restTemplate;
	private Logger logger = Logger.getLogger(RESTController.class);
	static final Logger tracelog = Logger.getLogger("reportsLogger");
	private  GHopper gHopper;
	private Mongo dao;

	private List<String> cities;

	public RESTController() {
		logger.info("Server initialization started");
		dao = new Mongo();
		gHopper = new GHopper();
		cities = new ArrayList<>();
		for(CityProperties cp: CityProperties.getInstance(this.getClass().getResource("/../data/cities.csv").getPath())) {
			String city = cp.getName();
			double[] lonlat = cp.getCenterLonLat();
			cities.add(city+","+lonlat[0]+","+lonlat[1]);
			if (!dao.checkActivities(city)) {
				logger.info("/../data/"+cp.getDataDir());
				//String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois";
				new SavePOIs2DB().run(city, dao, this.getClass().getResource("/../data/"+cp.getDataDir()+"/pois").getPath());
				logger.info("POIs collected");
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

	@RequestMapping(value = "cities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<String> sendCities() {
		return cities;
	}


	@RequestMapping(value = "signin", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody Integer performLogin(@RequestBody User user) {
		tracelog.info("user "+user.getEmail()+" signin");
		return dao.login(user);
	}

	@RequestMapping(value = "signup", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean performSignup(@RequestBody User user) {
		tracelog.info("user "+user.getEmail()+" signup");
		return dao.signup(user);
	}


	@RequestMapping(value = "activities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI> sendActivities(@RequestParam(value="city", defaultValue="unknown") String city,
												  @RequestParam(value="user", defaultValue="unknown") String user) {
		tracelog.info("user "+user+ " got activities of " +city);
		return dao.retrieveActivities(city);
	}

	@RequestMapping(value = "itineraries", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<Itinerary> sendItineraries(@RequestParam(value="city", defaultValue="unknown") String city,
														 @RequestParam(value="user", defaultValue="unknown") String user) {
		tracelog.info("user "+user+ " got itineraries of " +city);
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

	@RequestMapping(value = "checkuser", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody UserLog checkUser(@RequestParam(value="userid") String userid) {
		return CheckUser.checkUser(dao,userid);
	}


	@RequestMapping(value = "newplan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody VisitPlanAlternatives getNewVisitPlan(@RequestBody PlanRequest plan_request) {
		tracelog.info("user "+plan_request.getUser()+" request newplan "+plan_request);
		return new FindPath().getNewVisitPlan(dao,plan_request);
	}

	@RequestMapping(value = "fb", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean saveFacebookData(@RequestBody String fbdata) {

		JSONObject obj = new JSONObject(fbdata);
		tracelog.info("user "+obj.get("id")+" logged with facebook");
		dao.insertFBData(fbdata);
		return true;
	}




	@RequestMapping(value = "accept_plan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean acceptVisitPlan(@RequestBody VisitPlanAlternatives plans) {
		tracelog.info("user "+plans.get(plans.getSelected()).getUser()+" selected plan "+plans.getSelected());
		return dao.insertPlan(plans);
	}

	// Questo metodo per ora non viene usato. Serve se devo recuperare un piano precedente non terminato
	@RequestMapping(value = "plan", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives getPlan(@RequestBody User user) {
		return dao.retrievePlan(user.getEmail());
	}


	// the service visited is for the actual visit of a place in an itinerary
	// this is just to log people activities
	@RequestMapping(value = "localize", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean localize(@RequestParam(value="lat", defaultValue="unknown") String lat,
									  @RequestParam(value="lon", defaultValue="unknown") String lon,
									  @RequestParam(value="user", defaultValue="unknown") String user) {
		tracelog.info("user "+user+ " localized at "+lat+","+lon);
		return true;
	}


	// the service visited is for the actual visit of a place in an itinerary
	// this is just to log people activities
	@RequestMapping(value = "look", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean look(@RequestParam(value="poi", defaultValue="unknown") String poi,
									  @RequestParam(value="user", defaultValue="unknown") String user) {
		tracelog.info("user "+user+ " visited "+poi);
		return true;

	}

	@RequestMapping(value = "visited", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives addVisitedAndReplan(@RequestBody Visit new_visited) {
		tracelog.info("user "+new_visited.getUser()+ " visited (in plan) "+new_visited.toString());
		return new FindPath().addVisitedAndReplanWithType(dao,new_visited);

	}


	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody User user) {
		tracelog.info("user "+user.getEmail()+" completed his visiting plan in "+user.getCity());
		return dao.deletePlan(user.getEmail());
	}


	@Scheduled(fixedRate = 300000) // every five minutes
	public void downloadData() {

		DateFormat hourFormatter = new SimpleDateFormat("hh");
		DateFormat minuteFormatter = new SimpleDateFormat("mm");
		Date d = new Date();
		hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		String hour = hourFormatter.format(d);
		String minute = minuteFormatter.format(d);
		System.out.println("download datapipe data at "+hour+":"+minute);
		new DataPipeDownload().download();
	}

}