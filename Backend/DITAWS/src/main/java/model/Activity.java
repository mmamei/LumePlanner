package model;

public class Activity {
	
	private String	arrival_time;
	private String	departure_time;
	private POI		visit;
	
	public Activity(){}


	public String getArrival_time() {
		return arrival_time;
	}

	public void setArrival_time(String arrival_time) {
		this.arrival_time = arrival_time;
	}

	public String getDeparture_time() {
		return departure_time;
	}

	public void setDeparture_time(String departure_time) {
		this.departure_time = departure_time;
	}

	public POI getVisit() {
		return visit;
	}

	public void setVisit(POI visit) {
		this.visit = visit;
	}

	@Override
	public String toString() {
		return "Activity [arrival_time=" + arrival_time + ", departure_time="
				+ departure_time + ", visit=" + visit + "]";
	}

	
	
	

}
