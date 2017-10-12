package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import io.Mongo;
import io.RESTController;
import model.City;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.*;

import static util.StringUtils.array2string;
import static util.StringUtils.removeAccent;

public class ComuniERGeoJsonParser {

	public static void main(String[] args) throws Exception  {
		run();
		System.out.println("Done");
	}

	public static void run() throws Exception  {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode root = mapper.readValue(new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\ComuniER.geojson"),JsonNode.class).get("features");
		int idx = 0;
		JsonNode comune;


		File img_dir = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\img\\cities");
		Set<String> img_files = new HashSet<>();
		for(String f: img_dir.list())
			img_files.add(f.substring(0,f.length()-4));
		System.out.println("Available imgs: "+img_files);

		Set<String> found_img_files = new HashSet<>();


		List<City> top_three = new ArrayList<>(); // this is to move Bologna,Modena,Reggio Emilia to the top
		List<City> cities = new ArrayList<>();

		while ((comune = root.get(idx)) != null) {
			String pretty_name = comune.get("properties").get("COMUNE").toString().replaceAll("\"","");
			String name = removeAccent(pretty_name).replaceAll(" ","_");


			String img = "city"+(int)(1+3*Math.random())+".jpg";
			if(img_files.contains(name)) {
				found_img_files.add(name);
				img = name+".jpg";
			}

			JsonNode bboxNode = comune.get("bbox");
			double[] bbox =  new double[4];
			if (bboxNode.isArray()) {
				int i=0;
				for (final JsonNode n : bboxNode) {
					bbox[i++] = n.asDouble();
				}
			}


			if(name.equals("Bologna") || name.equals("Modena") || name.equals("Reggio_nell'Emilia"))
				top_three.add(new City(name,pretty_name,img,bbox));
			else cities.add(new City(name,pretty_name,img,bbox));

			idx++;
		}
		System.out.println("Found imgs: "+found_img_files);


		top_three.addAll(cities);
		mapper.writeValue(new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.json"),top_three);
	}

}
