package services;

import io.Mongo;
import model.Itinerary;
import model.POI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static util.Misc.haverDist;

/**
 * Created by marco on 09/10/2017.
 */
public class ItineraryGenerator {

    public static void main(String[] args) {

        String user = "0.75634758345786";
        String city = "Modena"; double[] latlng = new double[]{44.6290051,10.8701162};
        //String city = "ReggioEmilia"; double[] latlng = new double[]{44.687561,10.667276};


        Mongo dao = new Mongo();
        ItineraryGenerator ig = new ItineraryGenerator(dao,city,user,latlng);
        List<Itinerary> itineraries = dao.retrieveItineraries(city);
        ig.overrideTimeOfVisit(itineraries);
        List<Itinerary> gen_itineraries = ig.generate();
        itineraries.addAll(gen_itineraries);

        Collections.sort(itineraries,new Comparator<Itinerary>() {
            @Override
            public int compare(Itinerary i1, Itinerary i2) {
                int t1 = Integer.parseInt(i1.getApprox_time().split(" ")[0]);
                int t2 = Integer.parseInt(i2.getApprox_time().split(" ")[0]);

                if(t1 > t2) return 1;
                if(t1 < t2) return -1;
                return 0;
            }
        });

        for(Itinerary it: itineraries)
        System.out.println("Itinerary "+it.getDisplay_name()+" with "+it.getVisits().size()+" steps, approx_time "+it.getApprox_time());
    }


    private Mongo dao;
    private String city;
    private String user;
    private double[] latlng;
    private int count;

    public ItineraryGenerator(Mongo dao, String city, String user, double[] latlng) {
        this.dao = dao;
        this.city = city;
        this.user = user;
        this.latlng = latlng;
        this.count = 1;
    }

    public void overrideTimeOfVisit(List<Itinerary> itineraries) {
        for(Itinerary it:itineraries)
            computeApproxTimeMins(latlng,it);
    }

    public static final int MAX_STOPS = 5;
    public List<Itinerary> generate() {
        List<POI> pois = dao.retrieveActivities(city);
        POIComparator comparator = new POIComparator(dao,user);
        List<Itinerary> itineraries = new ArrayList<>();
        double[] mins = new double[]{60,120,240};
        for(int i=0; i<mins.length;i++)
            for(Itinerary iti: generate(pois,comparator,mins[i]*60,MAX_STOPS))
                if(!contains(itineraries,iti))
                    itineraries.add(iti);
        return itineraries;
    }

    private boolean contains(List<Itinerary> itineraries, Itinerary a) {
        for(Itinerary i: itineraries)
            if(compare(i,a)) return true;
        return false;
    }

    private boolean compare(Itinerary a, Itinerary b) {
        return a.getVisits().toString().equals(b.getVisits().toString());
    }


    private List<Itinerary> generate(List<POI> pois, POIComparator comparator, double maxtime, int max_stops) {
        List<Itinerary> itineraries = new ArrayList<>();

        List<POI> pois_in_reach = new ArrayList<>();

        double maxd = maxtime / 2 / 0.78; // Divido per 2 per tenere conto di andata e ritorno. Divido per 0.78 per convertire in metri

        for(POI p: pois) {
            double d = haverDist(latlng,new double[]{p.getGeometry().getCoordinates().getLatitude(),
                                                     p.getGeometry().getCoordinates().getLongitude()});
            if(d < maxd)
                pois_in_reach.add(p);
        }

        Collections.sort(pois_in_reach,comparator);

        if(pois_in_reach.size() > 0) {

            List<String> visits = new ArrayList<>(); // this is to create the itinerary
            for (int i = 0; i < Math.min(max_stops,pois_in_reach.size()); i++)
                visits.add(pois_in_reach.get(i).getPlace_id());

            Itinerary it = new Itinerary("auto_geg_iti"+count, "Itinerario "+count, visits, null, null, "Un itinerario tra le "+visits.size()+" attrazioni più interessanti vicino a te");
            computeApproxTimeMins(latlng, it);
            itineraries.add(it);
            count++;
        }
        return itineraries;
    }


    private int computeApproxTimeMins(double[] latlng, Itinerary itinerary) {
        double tot_dist = 0;
        double[] from = latlng;

        List<String> to_visit = new ArrayList<>();
        for(String s: itinerary.getVisits())
            to_visit.add(s);


        while (!to_visit.isEmpty()) {
            double min_distance = Double.MAX_VALUE;
            POI closest = null;
            for (String poi : to_visit) {
                POI current = dao.retrieveActivity(city,poi);
                double current_distance = haverDist(
                        from,
                        new double[] {current.getGeometry().getCoordinates().getLatitude(), current.getGeometry().getCoordinates().getLongitude()});
                if (current_distance < min_distance) {
                    min_distance = current_distance;
                    closest = current;
                }
            }
            tot_dist += min_distance;
            to_visit.remove(closest.getPlace_id());
            from = new double[] {closest.getGeometry().getCoordinates().getLatitude(), closest.getGeometry().getCoordinates().getLongitude()};
        }
        tot_dist += haverDist(from,latlng);
        int tot_time = (int)(tot_dist * 0.78 / 60);
        itinerary.setApprox_time(tot_time+" mins");
        return tot_time;
    }
}
