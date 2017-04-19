package io;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import model.*;
import org.apache.log4j.Logger;
import services.GetPOIs;

import java.util.*;

/**
 * Created by marco on 18/10/2016.
 */
public class CityData {
    public String city;
    private List<POI> activities;
    private List<POI> restaurants;
    private String last_crowding_levels;
    //private Map<String, HashMap<String, List<UncertainValue>>> crowding_levels;
    //private Map<String, List<Integer>> occupancies;
    //private Map<String, HashMap<String, List<UncertainValue>>> travel_times;
    private Mongo dao;
    public GraphHopper hopper;
    private String data_path;
    //private TreeMap<String, TreeMap<String, Double>> distances;
    private static Logger logger = Logger.getLogger(CityData.class);

    public static boolean DESKTOP_RUN = false;


    public CityData(String city) {
        this.city = city;
        activities = new ArrayList<>();
        restaurants = new ArrayList<>();
        last_crowding_levels = String.valueOf(Long.MIN_VALUE);
        //crowding_levels = new HashMap<>();
        //occupancies = new HashMap<>();
        //travel_times = new HashMap<>();

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


    public VisitPlanAlternatives retrievePlan(String email) {
        return dao.retrievePlan(email);
    }

    public VisitPlanAlternatives updatePlan(Visit v) {
        return dao.updatePlan(v);

    }
    public List<POI> retrieveActivities() {
        return dao.retrieveActivities();
    }


    public boolean deletePlan(String email) {
        return dao.deletePlan(email);
    }


    public POI retrieveActivity(String next_id) {
        return dao.retrieveActivity(next_id);
    }


    public POI getPOI(String name) {
        for (POI poi : activities) {
            if (poi.getPlace_id().equals(name))
                return poi;
        }
        return null;
    }



}
