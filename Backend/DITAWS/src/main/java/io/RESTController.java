package io;

import citylive.DataPipeDownload;
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
			cities.add(city);
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
		return new FindPath().getNewVisitPlan(dao,plan_request);
	}


	@RequestMapping(value = "accept_plan", method = RequestMethod.POST, headers = {"content-type=application/json"})
	public @ResponseBody boolean acceptVisitPlan(@RequestBody VisitPlanAlternatives plans) {
		String city = plans.getCity();
		int type = 0;
		if (!dao.insertPlan(plans)) return false;
		/*
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
		*/
		return true;
	}


	@RequestMapping(value = "plan", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives getPlan(@RequestBody User user) {
		return dao.retrievePlan(user.getEmail());
	}



	@RequestMapping(value = "visited", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody VisitPlanAlternatives addVisitedAndReplan(@RequestBody Visit new_visited) {
		//String city = new_visited.getCity();
		return new FindPath().addVisitedAndReplanWithType(dao,0, new_visited);

	}


	@RequestMapping(value = "finish", headers="Accept=application/json", method = RequestMethod.POST)
	public @ResponseBody boolean removePlan(@RequestBody User user) {
		logger.info("User "+user.getEmail()+" completed his visiting plan in "+user.getCity());
		String city = user.getCity();
		return dao.deletePlan(user.getEmail());
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

}