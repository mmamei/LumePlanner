package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Created by marco on 20/04/2017.
 */
// {"itinerary_id":"reggio_iti_1","visits":["53149397","dismi1"],"display_name":"A tour from Calatrava to DISMI"}
public class Itinerary {
    private String itinerary_id;
    private String display_name;
    private List<String> visits;
    private String approx_time;

    public Itinerary() {
    }

    public Itinerary(String itinerary_id, String display_name, List<String> visits, String approx_time) {
        this.itinerary_id = itinerary_id;
        this.display_name = display_name;
        this.visits = visits;
        this.approx_time = approx_time;
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


    public String getItinerary_id() {
        return itinerary_id;
    }

    public void setItinerary_id(String itinerary_id) {
        this.itinerary_id = itinerary_id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public List<String> getVisits() {
        return visits;
    }

    public void setVisits(List<String> visits) {
        this.visits = visits;
    }

    public String getApprox_time() {
        return approx_time;
    }

    public void setApprox_time(String approx_time) {
        this.approx_time = approx_time;
    }



}
