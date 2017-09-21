package services;

import io.Mongo;

import java.io.*;

import io.RESTController;
import model.POI;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SavePOIs2DB {



	public static void main(String[] args) throws Exception  {
		SavePOIs2DB g = new SavePOIs2DB();
		String city = "Modena";
		Mongo dao = new Mongo();
		String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois";
		g.run(city, dao,dir);
	}


	private Logger logger = Logger.getLogger(RESTController.class);


	public void run(String city, Mongo dao, String pois_dir) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			File dir = new File(pois_dir);
			for(File f: dir.listFiles()) {

				String file = f.getAbsolutePath();
				if(file.endsWith("json")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
					JSONArray parsed = new JSONArray(br.readLine());
					for (int j = 0; j < parsed.length(); j++) {
						JSONObject currentJPOI = (JSONObject) parsed.get(j);

						POI currentPOI = null;
						try {
							currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
						} catch(Exception pe) {
							logger.warn("Error parsing " + currentJPOI.toString());
							pe.printStackTrace();
						}
							//logger.info("**** " + currentPOI.getDisplay_name());

						if(currentPOI!=null)
							dao.insertActivity(city, currentPOI);
					}
					br.close();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}