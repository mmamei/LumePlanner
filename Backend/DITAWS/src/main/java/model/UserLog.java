package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 13/09/2017.
 */
public class UserLog {
    String userid;
    private List<double[]> latLon;
    private Map<String,List<POI>> itineraries;
    private boolean gotPrize = false;


    public UserLog() {
        latLon = new ArrayList<>();
        itineraries = new HashMap<>();
    }

    public void print() {
        System.out.println("User ID: "+userid);
        System.out.println("latLon:");
        for(double[] ll: latLon)
            System.out.println(ll[0]+","+ll[1]);
        System.out.println("Itineraries");
        for(String i: itineraries.keySet())
            System.out.println(i);
    }

    public String toJSONString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<double[]> getLatLon() {
        return latLon;
    }

    public void setLatLon(List<double[]> latLon) {
        this.latLon = latLon;
    }

    public Map<String, List<POI>> getItineraries() {
        return itineraries;
    }

    public void setItineraries(Map<String, List<POI>> itineraries) {
        this.itineraries = itineraries;
    }

    public boolean isGotPrize() {
        return gotPrize;
    }

    public void setGotPrize(boolean gotPrize) {
        this.gotPrize = gotPrize;
    }
}
