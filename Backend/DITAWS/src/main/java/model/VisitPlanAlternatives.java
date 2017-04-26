package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VisitPlanAlternatives {

	private String city;
	private VisitPlan 	asis;
	private VisitPlan 	shortest;
	private VisitPlan 	crowd;
	private double		crowd_preference; //-1=fullyCrowded; -0.5=mainlyCrowded; +0.5=mainlyUncrowded; +1=fullyUncrowded
	private String selected;

	public String getCity() {
		return city;
	}

	public VisitPlan getAsis() {
		return asis;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setAsis(VisitPlan asis) {
		this.asis = asis;
	}

	public void setShortest(VisitPlan shortest) {
		this.shortest = shortest;
	}

	public void setCrowd(VisitPlan crowd) {
		this.crowd = crowd;
	}

	public void setCrowd_preference(double crowd_preference) {
		this.crowd_preference = crowd_preference;
	}
	public double getCrowd_preference() {
		return crowd_preference;
	}
	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public VisitPlan getShortest() {
		return shortest;
	}

	public VisitPlan getCrowd() {
		return crowd;
	}



	public VisitPlanAlternatives() {
		this.asis = new VisitPlan();
		this.shortest = new VisitPlan();
		this.crowd = new VisitPlan();
		this.crowd_preference = 1d;
		this.selected = "null";
	}

	public VisitPlanAlternatives(String city, VisitPlan asis, VisitPlan shortest, VisitPlan crowd, double crowd_preference) {
		this.city = city;
		this.asis = asis;
		this.shortest = shortest;
		this.crowd = crowd;
		this.crowd_preference = crowd_preference;
		this.selected = "null";
	}

	public VisitPlan getVisitPlanSelected() {
		if(selected.equals("asis")) return asis;
		if(selected.equals("shortest")) return shortest;
		if(selected.equals("crowd")) return crowd;
		return null;
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
