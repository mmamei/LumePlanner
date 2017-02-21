package io;

import model.Cell;
import model.POI;
import model.POI2POICrowding;
import model.UncertainValue;
import org.apache.log4j.Logger;
import services.ComputeP2PCrowdings;
import services.LoadFiles;
import util.RandomValue;
import util.TimeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Misc.round;

/**
 * Created by marco on 19/10/2016.
 */
public class CityData {
    private Logger logger = Logger.getLogger(RESTController.class);
    private String city;

    private Mongo dao;
    private List<POI> activities;
    private Map<String, HashMap<String, List<UncertainValue>>> travel_times ;
    private List<Cell> grid;
    private Map<String, List<UncertainValue>> grid_crowdings;

    public Map<String, Map<String, Map<Integer, Map<String, Double>>>> p2p_cell_paths;

    public CityData(String city) {
        this.city = city;
    }

    public void init() {
        dao = new Mongo(CityProp.getInstance().get(city).getDB());

        logger.info("Crowding Module initialization started");

        grid_crowdings = new LoadFiles().load(dao, city);
        logger.info("Grid Crowdings imported ("+grid_crowdings.size()+")");

        activities = dao.retrieveActivities();
        logger.info("Activities retrieved from Mongodb (count "+activities.size()+")");

        //writeCrowdings();

        grid = dao.retrieveGrid();

        travel_times = dao.retrieveTravelTimes();
        logger.info("Travel times retrieved from Mongodb (count "+travel_times.size()+")");

        ComputeP2PCrowdings p2p = new ComputeP2PCrowdings();

        if (!dao.checkCellPaths()) {
            p2p_cell_paths = p2p.insertCellPaths(dao, activities);
            logger.info("Cell Paths imported ("+p2p_cell_paths.size()+")");
        } else {
            p2p_cell_paths = dao.retrieveCellPaths();
            logger.info("CellPaths retriedved from Mongodb ("+p2p_cell_paths.size()+")");
        }
        if (!dao.checkCrowdingLevels()) {
            List<POI2POICrowding> crs = p2p.run(this);
            logger.info("POI2POI Crowdings imported ("+crs.size()+")");
        }
    }


    public void retrieveGridCrowdings() {
        grid_crowdings = dao.retrieveGridCrowdings();
    }


    public long getTime(String from, String to, long time) {
        return ((long)RandomValue.get(travel_times.get(from).get(to).get(TimeUtils.getTimeSlot(time))))*60*1000l;
    }

    public Long getTravelTime(String poi_start, String poi_end, Long time_slot) {
        UncertainValue travel_time = null;
        try {

            travel_time = travel_times.get(poi_start).get(poi_end).get(TimeUtils.getTimeSlot(TimeUtils.getMillis15MRoundTime(time_slot)));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((long)RandomValue.get(travel_time))*60*1000l;
    }

    public Double findCrowding(String current_cell, Long arr_time_inCell, Long dep_time_fromCell) {
        Long start_time = TimeUtils.getMillis15MRoundTime(arr_time_inCell);
        Long end_time = TimeUtils.getMillis15MRoundTime(dep_time_fromCell);
        if (end_time<start_time) end_time += 86400000L;
        int number_time_slots = 1 + ((int)(end_time - start_time))/900000;

        //logger.info(current.getId()+" ("+arr_time_inCell+ "-"+dep_time_fromCell+") ["+number_time_slots+"] ("+start_time+"-"+end_time+")");

        double max_mean = Double.MIN_VALUE;
        for (int i=0; i<number_time_slots; i++) {
            //logger.info("grid_crowding:"+current.getId()+"("+TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)+"):::"+grid_crowdings.get(current.getId()).get(TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)).getDistribution());
            double mean = RandomValue.get(grid_crowdings.get(current_cell).get(TimeUtils.getTimeSlot((start_time+900000*i)%86400000L)));
            max_mean = (mean > max_mean) ? mean : max_mean;
        }

        return max_mean;
    }


    public Cell retrieveCell(String poi_start, String poi_end, int i) {
        for (Cell cell : grid) {
            if (cell.getId().equals(p2p_cell_paths.get(poi_start).get(poi_end).get(i).keySet().iterator().next())) {
                return cell;
            }
        }
        return null;
    }


    public void updateCrowding(POI2POICrowding crowding) {
        dao.updateCrowding(crowding);
    }

    public void updateGridCrowdings(Cell current, Long arr_time_inCell, Long dep_time_fromCell, String inc_dec) {
        dao.updateGridCrowdings(current,arr_time_inCell,dep_time_fromCell,inc_dec);
    }

}
