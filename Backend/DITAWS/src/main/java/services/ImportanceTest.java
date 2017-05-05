package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Mongo;
import io.RESTController;
import model.POI;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ImportanceTest {



	public static void main(String[] args) throws Exception  {
		ImportanceTest g = new ImportanceTest();
		String city = "Venezia";
		Mongo dao = new Mongo();
		String file = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\nominatim.json";
		g.run(city, dao, file);
	}


	private Logger logger = Logger.getLogger(RESTController.class);



	public void run(String city, Mongo dao, String file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {

			DescriptiveStatistics ds = new DescriptiveStatistics();
			BufferedReader br = new BufferedReader(new FileReader(file));
			JSONArray parsed = new JSONArray(br.readLine());
			for (int j = 0; j < parsed.length(); j++) {
				JSONObject currentJPOI = (JSONObject) parsed.get(j);
				POI currentPOI = null;
				try {
					currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
					ds.addValue(currentPOI.getImportance());
				} catch(Exception pe) {
					logger.warn("Error parsing " + currentJPOI.toString());
					pe.printStackTrace();
				}
			}
			br.close();

			for(int i=10;i<=100;i=i+10)
				System.out.println(i+"% = "+ds.getPercentile(i));



		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}