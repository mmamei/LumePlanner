package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Mongo;
import io.RESTController;
import model.City;
import model.Itinerary;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;

public class SaveItineraries2DB {

	public static void main(String[] args) throws Exception  {
		SaveItineraries2DB g = new SaveItineraries2DB();
		List<City> cities = City.getInstance();
		Mongo dao = new Mongo();
		for(City c: cities) {
			String file = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\"+c.getName()+"\\itineraries\\itineraries.json";
			g.run(c.getName(), dao, file);
		}
	}

	private Logger logger = Logger.getLogger(RESTController.class);

	public void run(String city, Mongo dao, String sfile) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


		try {
			File file = new File(sfile);
			if (file.exists()) {

				StringBuffer json = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
				String line;
				while((line=br.readLine())!=null)
					json.append(line.trim());
				br.close();

				JSONArray parsed = new JSONArray(json.toString());
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

			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}