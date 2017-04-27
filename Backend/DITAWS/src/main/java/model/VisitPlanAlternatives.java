package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class VisitPlanAlternatives {

	private String city;
	private String user;
	private Map<String,VisitPlan> plans;
	private String selected;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Map<String, VisitPlan> getPlans() {
		return plans;
	}

	public void setPlans(Map<String, VisitPlan> plans) {
		this.plans = plans;
	}

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public VisitPlanAlternatives() {
		plans = new HashMap<>();
		this.selected = "null";
	}

	public VisitPlanAlternatives(String city, String user) {
		this.city = city;
		this.user = user;
		plans = new HashMap<>();
		this.selected = "null";
	}

	public void add(String name, VisitPlan plan) {
		plans.put(name,plan);
	}

	public VisitPlan get(String key) {
		return plans.get(key);
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
