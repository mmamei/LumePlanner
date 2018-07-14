package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import model.City;

import java.util.List;

/**
 * Created by marco on 13/07/2018.
 */
public class GPXItinerary {
    private String orig_dir;
    private String itinerary_id;
    private String gpx;
    private String display_name;
    private String approx_time;
    private String description;
    private String img;



    GPXItinerary(String orig_dir, String itinerary_id, String gpx, String display_name, String approx_time, String description, String img) {
        this.orig_dir = orig_dir;
        this.itinerary_id = itinerary_id;
        this.gpx = gpx;
        this.display_name = display_name;
        this.approx_time = approx_time;
        this.description = description;
        this.img = img;
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

    public static boolean passBy(List<Track> gpx, City cp) {
        double in = 0;
        double tot = 0;
        for(Track t: gpx)
            for(TrackSegment ts: t.getSegments())
                for(WayPoint p: ts.getPoints()) {
                    if (cp.contains(p.getLatitude().doubleValue(), p.getLongitude().doubleValue()))
                        in++;
                    tot ++;
                }
        return (in/tot) > 0.1;

    }


    public String getOrig_dir() {
        return orig_dir;
    }

    public void setOrig_dir(String orig_dir) {
        this.orig_dir = orig_dir;
    }

    public String getItinerary_id() {
        return itinerary_id;
    }

    public void setItinerary_id(String itinerary_id) {
        this.itinerary_id = itinerary_id;
    }

    public String getGpx() {
        return gpx;
    }

    public void setGpx(String gpx) {
        this.gpx = gpx;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getApprox_time() {
        return approx_time;
    }

    public void setApprox_time(String approx_time) {
        this.approx_time = approx_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
