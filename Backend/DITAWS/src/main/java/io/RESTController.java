package io;

import org.json.JSONObject;
import services.*;
import services.timdatapipe.BILMean;
import services.timdatapipe.CrowdDataManager;
import services.timdatapipe.DataPipeDownload;
import model.*;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import services.pathfinder.FindPath;
import util.StringUtils;

import javax.crypto.spec.PSource;
import java.io.*;
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
	CrowdDataManager cdm;
	private List<City> cities;
	private String app_version;
	public RESTController() {
		logger.info("Server initialization started");
		dao = new Mongo();
		gHopper = new GHopper();
		cdm = new CrowdDataManager();
		cities = City.getInstance();
		app_version = AppVersion.getVersion();

		System.out.println("**********************************************************************************");
		System.out.println("Cities");
		System.err.println("**********************************************************************************");
		System.err.println("Cities");
		for(City c: cities) {
			String city = c.getName();
			//cities.add(city+","+lonlatBB[0][0]+","+lonlatBB[0][1]+","+lonlatBB[1][0]+","+lonlatBB[1][1]);
			if (!dao.checkActivities(city)) {
				//String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois";
				new SavePOIs2DB().run(city, dao, this.getClass().getResource("/../data/cities/"+city+"/pois").getPath());
				logger.info("POIs collected");
			}
			new SaveItineraries2DB().run(city, dao,this.getClass().getResource("/../data/cities/"+city).getPath()+"/itineraries/itineraries.json");
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
	public @ResponseBody List<City> sendCities() {
		return cities;
	}

	@RequestMapping(value = "version", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody String version() {
		return app_version;
	}

	@RequestMapping(value = "updatepref", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean updatePreferences(@RequestBody UserPreferences up) {
		dao.updatePrefs(up.getUser(),up.getPrefs());
		tracelog.info("user "+up.getUser()+" update preferences "+up.getPrefs());
		return true;
	}

	@RequestMapping(value = "loadpref", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody Map<String,Double> getPreferences(@RequestBody String user) {
		user = user.substring(1,user.length()-1);
		Map<String,Double> prefs = dao.getPrefs(user);
		tracelog.info("user "+user+" got preferences "+prefs);
		return prefs;
	}


	@RequestMapping(value = "activities", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<POI> sendActivities(@RequestParam(value="city", defaultValue="unknown") String city,
												  @RequestParam(value="user", defaultValue="unknown") String user) {
		tracelog.info("user "+user+ " got activities of " +city);
		return dao.retrieveActivities(city);
	}


	@RequestMapping(value = "itineraries", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody List<Itinerary> sendItineraries(@RequestParam(value="city", defaultValue="unknown") String city,
														 @RequestParam(value="user", defaultValue="unknown") String user,
														 @RequestParam(value="lat", defaultValue="0") String lat,
														 @RequestParam(value="lng", defaultValue="0") String lng) {
		tracelog.info("user "+user+ " got itineraries of " +city + " from position "+lat+","+lng);

		double[] latlng = new double[]{Double.parseDouble(lat),Double.parseDouble(lng)};

		List<Itinerary> itineraries = dao.retrieveItineraries(city);

		ItineraryGenerator ig = new ItineraryGenerator(dao, city, user, latlng);
		if(!lat.equals("unknown") && !lng.equals("unknown"))
			ig.overrideTimeOfVisit(itineraries);
		itineraries.addAll(ig.generate());

		Collections.sort(itineraries,new Comparator<Itinerary>() {
			@Override
			public int compare(Itinerary i1, Itinerary i2) {
				int t1 = Integer.parseInt(i1.getApprox_time().split(" ")[0]);
				int t2 = Integer.parseInt(i2.getApprox_time().split(" ")[0]);

				if(t1 > t2) return 1;
				if(t1 < t2) return -1;
				return 0;
			}
		});

		return itineraries;
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

	@RequestMapping(value = "log", headers="Accept=application/json", method = RequestMethod.GET)
	public @ResponseBody boolean log(@RequestParam(value="txt") String txt) {
		tracelog.info(txt);
		return true;
	}

	@RequestMapping(value = "tp_log", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean logpost(@RequestBody TPLog log) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(
					new File("C:\\Users\\marco\\Dropbox\\LumePlanner\\tp_log.txt"),
					true));
			out.println(log);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}


	@RequestMapping(value = "help", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean help(@RequestBody HelpLog h) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(
					new File("C:\\Tomcat7\\help.txt"),
					true));
			out.println(h.getUserid()+";"+h.getEmail()+";"+h.getText());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
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
	public @ResponseBody VisitPlanAlternatives getPlan(@RequestBody String user) {

		return dao.retrievePlan(user);
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
        dao.updatePrefs(new_visited.getUser(),new_visited.getVisited().getCategory(),1);
		return new FindPath().addVisitedAndReplanWithType(dao,new_visited);

	}


	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody String user) {
		user = user.substring(1,user.length()-1);
		tracelog.info("user "+user+" completed his visiting plan");
		return dao.deletePlan(user);
	}

	@Scheduled(fixedRate = 5*60*1000) // every five minutes
	public void downloadData() {

		DateFormat hourFormatter = new SimpleDateFormat("hh");
		DateFormat minuteFormatter = new SimpleDateFormat("mm");
		Date d = new Date();
		hourFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		String hour = hourFormatter.format(d);
		String minute = minuteFormatter.format(d);
		System.out.println("download datapipe data at "+hour+":"+minute);
		new DataPipeDownload().download();
		cdm.processCrowdInfo(false);
	}

	@Scheduled(fixedRate = 2*7*24*60*1000) // every 2 weeks
	public void comppute2WeeksMeanCrowdValues() {
		BILMean.saveMean();
	}

}