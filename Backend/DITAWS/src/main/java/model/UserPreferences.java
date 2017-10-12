package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Created by marco on 12/10/2017.
 */
public class UserPreferences {
    String user;
    Map<String,Double> prefs;

    public UserPreferences() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, Double> getPrefs() {
        return prefs;
    }

    public void setPrefs(Map<String, Double> prefs) {
        this.prefs = prefs;
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
}
