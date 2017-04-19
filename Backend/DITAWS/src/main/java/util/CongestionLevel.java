package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.POI;

public class CongestionLevel {
	
	public static Map<String, HashMap<String, ArrayList<Double>>> initCongestionLevelFromPOIs(List<POI> POIs) {
		Map<String, HashMap<String, ArrayList<Double>>> result = new HashMap<String, HashMap<String, ArrayList<Double>>>();

		for (POI POIorig : POIs) {
			for (POI POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig.getPlace_id())==null) {
						result.put(POIorig.getPlace_id(), new HashMap<String, ArrayList<Double>>());
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<Double>(24));
					} else if (result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id())==null)
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<Double>(24));
					
					double congestion_level = Math.random();
										
					result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id()).add(time, congestion_level);
				}
			}
		}

		return result;
	}

	public static Map<String, HashMap<String, ArrayList<Double>>> getCongestionLevelFromPOIs(List<POI> POIs) {
		Map<String, HashMap<String, ArrayList<Double>>> result = new HashMap<String, HashMap<String, ArrayList<Double>>>();

		for (POI POIorig : POIs) {
			for (POI POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig.getPlace_id())==null) {
						result.put(POIorig.getPlace_id(), new HashMap<String, ArrayList<Double>>());
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<Double>(24));
					} else if (result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id())==null)
						result.get(POIorig.getPlace_id()).put(POIdest.getPlace_id(), new ArrayList<Double>(24));
					
					double congestion_level = Math.random();
										
					result.get(POIorig.getPlace_id()).get(POIdest.getPlace_id()).add(time, congestion_level);
				}
			}
		}

		return result;
	}
	
	public Map<String, HashMap<String, List<Double>>> getCongestionLevelFromIDs(List<String> POIs) {
		Map<String, HashMap<String, List<Double>>> result = new HashMap<String, HashMap<String, List<Double>>>();

		for (String POIorig : POIs) {
			for (String POIdest : POIs) {
				for (int time = 0; time<24; time++) {

					if (result.get(POIorig)==null) {
						result.put(POIorig, new HashMap<String, List<Double>>());
						result.get(POIorig).put(POIdest, new ArrayList<Double>(24));
					} else if (result.get(POIorig).get(POIdest)==null)
						result.get(POIorig).put(POIdest, new ArrayList<Double>(24));
					
					double congestion_level;
					if (POIorig.equals(POIdest)) congestion_level = 0d;
					else congestion_level =  Math.random();
					
					result.get(POIorig).get(POIdest).add(time, congestion_level);
				}
			}
		}

		return result;
	}

	public static Map<String, HashMap<String, ArrayList<Double>>> updateCongestionLevel(
			Map<String, HashMap<String, ArrayList<Double>>> congestion_levels, int time) {

		for (String orig : congestion_levels.keySet()) {
			for (String dest : congestion_levels.get(orig).keySet()) {
				
				if (orig.equals(dest)) continue;
				
				double congestion_level = Math.random();
				
				congestion_levels.get(orig).get(dest).set(time, congestion_level);
			}
		}
		
		
		return congestion_levels;
	}
}