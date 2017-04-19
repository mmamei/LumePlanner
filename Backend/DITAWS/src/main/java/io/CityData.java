package io;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import model.*;
import org.apache.log4j.Logger;
import services.ComputeDistances;
import services.GetPOIs;
import util.Occupancies;
import util.RandomValue;
import util.TimeUtils;
import util.TravelTime;

import java.io.IOException;
import java.util.*;

import static util.Misc.round;

/**
 * Created by marco on 18/10/2016.
 */
public class CityData {
    public String city;
    private List<POI> activities;
    private List<POI> restaurants;
    private String last_crowding_levels;
    private Map<String, HashMap<String, List<UncertainValue>>> crowding_levels;
    private Map<String, List<Integer>> occupancies;
    private Map<String, HashMap<String, List<UncertainValue>>> travel_times;
    private Mongo dao;
    public GraphHopper hopper;
    private String data_path;
    private TreeMap<String, TreeMap<String, Double>> distances;
    private static Logger logger = Logger.getLogger(CityData.class);

    public static boolean DESKTOP_RUN = false;


    public CityData(String city) {
        this.city = city;
        activities = new ArrayList<>();
        restaurants = new ArrayList<>();
        last_crowding_levels = String.valueOf(Long.MIN_VALUE);
        crowding_levels = new HashMap<>();
        occupancies = new HashMap<>();
        travel_times = new HashMap<>();

        try {

            dao = new Mongo(CityProp.getInstance().get(city).getDB());
            hopper = new GraphHopper().forServer();

            if(!DESKTOP_RUN)
                data_path = this.getClass().getResource("/../data/"+CityProp.getInstance().get(city).getDataDir()).getPath();
            if(DESKTOP_RUN)
                data_path = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\target\\DITA\\WEB-INF\\data\\"+city+"\\";

            hopper.setInMemory();
            hopper.setOSMFile(data_path+"bbox.osm");
            hopper.setGraphHopperLocation(data_path+"graph");
            hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
            hopper.importOrLoad();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setPath(String path) {
        data_path = path;
        hopper.setInMemory();
        hopper.setOSMFile(data_path+"bbox.osm");
        hopper.setGraphHopperLocation(data_path+"graph");
        hopper.setEncodingManager(new EncodingManager(EncodingManager.FOOT));
        hopper.importOrLoad();
    }

    public void init() {

        if (!dao.checkActivities()) {
            logger.info("/../data/"+CityProp.getInstance().get(city).getDataDir());
            //String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois";
            new GetPOIs().run(dao, this.getClass().getResource("/../data/"+CityProp.getInstance().get(city).getDataDir()+"/pois").getPath());
            logger.info("POIs collected from OSM API");
        }
        activities = dao.retrieveActivities();
        restaurants = dao.retrieveRestaurants();
        logger.info("Activities retrieved from Mongodb (count "+activities.size()+")");
        logger.info("Restaurants retrieved from Mongodb (count "+restaurants.size()+")");

        if (!dao.checkDistances()) {
            new ComputeDistances().run(dao, activities);
            logger.info("Distances among activities computed");
        }
        distances = dao.retrieveDistances();
        logger.info("Look-up table for distances initialized (count "+distances.size()+")");

        if (!dao.checkTravelTimes()) {
            travel_times = new TravelTime(city).initTravelTimeFromPOIs(dao, activities);
        } else {
            travel_times = dao.retrieveTravelTimes();
        }
        logger.info("Look-up table for travel times initialized (count "+travel_times.size()+")");

        occupancies = new Occupancies().init(activities);

        logger.info("Look-up table for POIs occupancies initialized (count "+occupancies.size()+")");



        logger.info("Look-up table for congestion levels was initialized at "+last_crowding_levels);
        last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
        logger.info("Look-up table for congestion levels initialized at "+last_crowding_levels);
    }

    public void updateCongestions() {
        last_crowding_levels = dao.retrieveCrowdingLevels(crowding_levels, last_crowding_levels);
        logger.info("Look-up table for congestion levels initialized at "+ TimeUtils.getStringTime(Long.parseLong(last_crowding_levels)));
    }

    public Integer login(User user) {
        return dao.Login(user);
    }

    public boolean signup(User user) {
        return dao.Signup(user);
    }

    public boolean insertPlan(VisitPlanAlternatives plan) {
        return dao.insertPlan(plan);
    }

    public POI retrieveClosestActivity(POI poi) {
        return dao.retrieveClosestActivity(poi);
    }

    public boolean updateUser(CrowdingFeedback fdbk, UncertainValue value) {
        return dao.updateUser(fdbk,value);
    }

    public VisitPlanAlternatives retrievePlan(String email) {
        return dao.retrievePlan(email);
    }

    public VisitPlanAlternatives updatePlan(Visit v) {
        return dao.updatePlan(v);

    }
    public List<POI> retrieveActivities() {
        return dao.retrieveActivities();
    }

    public boolean updateUserOv_Cr(OverallFeedback fdbk) {
        return dao.updateUserOv_Cr(fdbk);
    }


    public boolean updateUserOv_Pl(OverallFeedback fdbk) {
        return dao.updateUserOv_Pl(fdbk);
    }

    public boolean deletePlan(String email) {
        return dao.deletePlan(email);
    }

    public List<GridCrowding> retrieveGridCrowding() {
        return dao.retrieveGridCrowding();
    }


    public double retrieveGridMaxCrowding() {
       return dao.retrieveGridMaxCrowding();
    }


    public int findMaxOccupancy() {
        int result = Integer.MIN_VALUE;

        for (String poi : occupancies.keySet()) {
            for (Integer value : occupancies.get(poi)) {
                if (value > result) {
                    result = value;
                }
            }
        }

        if (result<1000) {
            result = 1000;
        }

        return result;
    }


    public POI retrieveActivity(String next_id) {
        return dao.retrieveActivity(next_id);
    }


    public double getTime(String from, String to, int time) {
        return round(RandomValue.get(travel_times.get(from).get(to).get(TimeUtils.getTimeSlot(time))), 5);
    }

    public double getCrowd(String from, String to, int time) {

        String cong_k1;
        String cong_k2;
        if (crowding_levels.containsKey(from) && crowding_levels.get(from).containsKey(to)) {
            cong_k1 = from;
            cong_k2 = to;
        } else {
            cong_k2 = from;
            cong_k1 = to;
        }
        return RandomValue.get(crowding_levels.get(cong_k1).get(cong_k2).get(TimeUtils.getTimeSlot(time)));
    }


    public Double getDistance(String from, String to) {
        if(distances.get(from)==null || distances.get(from).get(to) == null) {
            System.err.println("getDistance.getDistance "+from+" "+to+" not found");
            return 100000.0;
        }
        return distances.get(from).get(to);
    }

    //int occupancy = cityData.
    public int getOccupancy(String child_name, int temp_arr_time_actual) {
        return occupancies.get(child_name).get(TimeUtils.getTimeSlot(temp_arr_time_actual));
    }



    public POI getPOI(String name) {
        for (POI poi : activities) {
            if (poi.getPlace_id().equals(name))
                return poi;
        }
        return null;
    }



}
