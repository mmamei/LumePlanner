package services;

import io.Mongo;
import model.POI;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by marco on 13/10/2017.
 */
public class POIComparator implements Comparator<POI> {

    private Map<String,Double> user_prefs = null;

    public POIComparator(Mongo dao, String user) {
        user_prefs = dao.getPrefs(user);
    }

    @Override
    public int compare(POI a, POI b) {

        double aimp = a.getImportance() + 2 * user_prefs.get(a.getCategory());
        double bimp = b.getImportance() + 2 * user_prefs.get(b.getCategory());

        if(aimp < bimp) return 1;
        if(aimp > bimp) return -1;
        return 0;
    }

}
