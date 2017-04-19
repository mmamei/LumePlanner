package model;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {

	private String 			city;
	private String 			id;
	private String 			email;
	private String 			password;
	private double 	low_crowding;
	private double 	lowAvg_crowding;
	private double 	avgHig_crowding;
	private double 	hig_crowding;
	private double 	overall_crowding;
	private boolean 		liked_crowding;
	private boolean 		liked_plan;

	public User() {
		this("","");
	}

	public User(String email, String password) {
		//this.id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
		this.setEmail(email);
		this.setPassword(password);
		this.setLow_crowding(0);
		this.setLowAvg_crowding(0);
		this.setAvgHig_crowding(0);
		this.setHig_crowding(0);
		this.setOverall_crowding(0);
		this.setLiked_crowding(false);
		this.setLiked_plan(false);
	}


	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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

	public double getLow_crowding() {
		return low_crowding;
	}

	public void setLow_crowding(double low_crowding) {
		this.low_crowding = low_crowding;
	}

	public double getLowAvg_crowding() {
		return lowAvg_crowding;
	}

	public void setLowAvg_crowding(double lowavg_crowding) {
		this.lowAvg_crowding = lowavg_crowding;
	}

	public double getAvgHig_crowding() {
		return avgHig_crowding;
	}

	public void setAvgHig_crowding(double avghig_crowding) {
		this.avgHig_crowding = avghig_crowding;
	}

	public double getHig_crowding() {
		return hig_crowding;
	}

	public void setHig_crowding(double hig_crowding) {
		this.hig_crowding = hig_crowding;
	}

	public double getOverall_crowding() {
		return overall_crowding;
	}

	public void setOverall_crowding(double overall_crowding) {
		this.overall_crowding = overall_crowding;
	}

	public boolean isLiked_crowding() {
		return liked_crowding;
	}

	public void setLiked_crowding(boolean liked_crowding) {
		this.liked_crowding = liked_crowding;
	}

	public boolean isLiked_plan() {
		return liked_plan;
	}

	public void setLiked_plan(boolean liked_plan) {
		this.liked_plan = liked_plan;
	}
	
	

}
