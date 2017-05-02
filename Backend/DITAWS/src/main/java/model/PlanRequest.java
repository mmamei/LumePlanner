package model;

import java.util.List;

public class PlanRequest {
	
	private String			user;
	private String 			city;
	private POI 			start_place;
	private POI 			end_place;
	private String 			start_time;
	private List<String> 	visits;
	
	public PlanRequest(){}

	public PlanRequest(String user, String city, POI start_place,
			POI end_place, String start_time, List<String> visits) {
		this.user = user;
		this.city = city;
		this.start_place = start_place;
		this.end_place = end_place;
		this.start_time = start_time;
		this.visits = visits;
	}

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

	public POI getStart_place() {
		return start_place;
	}

	public void setStart_place(POI start_place) {
		this.start_place = start_place;
	}

	public POI getEnd_place() {
		return end_place;
	}

	public void setEnd_place(POI end_place) {
		this.end_place = end_place;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public List<String> getVisits() {
		return visits;
	}

	public void setVisits(List<String> visits) {
		this.visits = visits;
	}

	public String toString() {
		return "plan request for "+user+" in "+city+" from "+start_place.getPlace_id()+" to "+end_place.getPlace_id()+" at time "+start_time + " to visit: "+visits;
	}

}
