package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Mongo;
import io.RESTController;
import model.Itinerary;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SaveItineraries2DB {



	public static void main(String[] args) throws Exception  {
		SaveItineraries2DB g = new SaveItineraries2DB();
		String city = "ReggioEmilia";
		Mongo dao = new Mongo();
		String file = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\itineraries.json";
		g.run(city, dao,file);
	}


	private Logger logger = Logger.getLogger(RESTController.class);


	public void run(String city, Mongo dao, String sfile) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			File file = new File(sfile);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				JSONArray parsed = new JSONArray(br.readLine());
				for (int j = 0; j < parsed.length(); j++) {
					JSONObject currentJPOI = (JSONObject) parsed.get(j);
					Itinerary currentIT = null;
					try {
						currentIT = mapper.readValue(currentJPOI.toString(), Itinerary.class);
					} catch (Exception pe) {
						logger.warn("Error parsing " + currentJPOI.toString());
						pe.printStackTrace();
					}
					if (currentIT != null)
						dao.insertItinerary(city,currentIT);
				}
				br.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}