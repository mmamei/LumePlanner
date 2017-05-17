package services;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 13/05/2017.
 */
public class CategoriesDictionary {

    static final Map<String,String> CSV_TO_CAT = new HashMap<>();
    static final Map<String,String> IBC_TO_CAT = new HashMap<>();
    static final Map<String,String> MIBACT_TO_CAT = new HashMap<>();
    static final Map<String,String> NOMINATIM_TO_CAT = new HashMap<>();
    static final String DEFAULT_CAT = "attractions";


    static {
        CSV_TO_CAT.put("attractions", "attractions");
        CSV_TO_CAT.put("parking", "parking");
        CSV_TO_CAT.put("medical", "medical");
        CSV_TO_CAT.put("religious_sites","attractions");
        CSV_TO_CAT.put("eating","eating");

        IBC_TO_CAT.put("HolderOfArchives","attractions");
        IBC_TO_CAT.put("Library","attractions");
        IBC_TO_CAT.put("Museum","attractions");
        IBC_TO_CAT.put("Tree","tree");

        MIBACT_TO_CAT.put("Battistero","attractions");
        MIBACT_TO_CAT.put("Portico","attractions");
        MIBACT_TO_CAT.put("Monumento","attractions");
        MIBACT_TO_CAT.put("Stazione","service");
        MIBACT_TO_CAT.put("Borgo","attractions");
        MIBACT_TO_CAT.put("Convento","attractions");
        MIBACT_TO_CAT.put("Mercato","attractions");
        MIBACT_TO_CAT.put("Oratorio","attractions");
        MIBACT_TO_CAT.put("Ospitale","attractions");
        MIBACT_TO_CAT.put("Colombaio","attractions");
        MIBACT_TO_CAT.put("Canonica","attractions");
        MIBACT_TO_CAT.put("Pubblica","attractions");
        MIBACT_TO_CAT.put("Cisterna","attractions");
        MIBACT_TO_CAT.put("Annesso","attractions");
        MIBACT_TO_CAT.put("Ecclesiastica","attractions");
        MIBACT_TO_CAT.put("Palazzo","attractions");
        MIBACT_TO_CAT.put("Rocca","attractions");
        MIBACT_TO_CAT.put("Scuola","attractions");
        MIBACT_TO_CAT.put("Fienile","attractions");
        MIBACT_TO_CAT.put("Parco","parks");
        MIBACT_TO_CAT.put("Sacrestia","attractions");
        MIBACT_TO_CAT.put("Grotta","attractions");
        MIBACT_TO_CAT.put("Campanile","attractions");
        MIBACT_TO_CAT.put("Piazza","attractions");
        MIBACT_TO_CAT.put("Barchessa","attractions");
        MIBACT_TO_CAT.put("Villa","attractions");
        MIBACT_TO_CAT.put("Edificio","attractions");
        MIBACT_TO_CAT.put("Sinagoga","attractions");
        MIBACT_TO_CAT.put("Elemento","attractions");
        MIBACT_TO_CAT.put("Cimitero","attractions");
        MIBACT_TO_CAT.put("Mulino","attractions");
        MIBACT_TO_CAT.put("Chiesa","attractions");
        MIBACT_TO_CAT.put("Casa","attractions");
        MIBACT_TO_CAT.put("Mura","attractions");
        MIBACT_TO_CAT.put("Stalla","attractions");
        MIBACT_TO_CAT.put("Teatro","attractions");
        MIBACT_TO_CAT.put("Strada","attractions");
        MIBACT_TO_CAT.put("Santuario","attractions");
        MIBACT_TO_CAT.put("Cinema","attractions");
        MIBACT_TO_CAT.put("Ospedale","attractions");
        MIBACT_TO_CAT.put("Bottega","attractions");
        MIBACT_TO_CAT.put("Macello","attractions");
        MIBACT_TO_CAT.put("Impianto","attractions");
        MIBACT_TO_CAT.put("Carcere","attractions");
        MIBACT_TO_CAT.put("Torre","attractions");
        MIBACT_TO_CAT.put("Cappella","attractions");
        MIBACT_TO_CAT.put("Ponte","attractions");
        MIBACT_TO_CAT.put("Porta","attractions");
        MIBACT_TO_CAT.put("Fabbricato","attractions");
        MIBACT_TO_CAT.put("Caserma","attractions");

        NOMINATIM_TO_CAT.put("attractions","attractions");
        NOMINATIM_TO_CAT.put("eating","eating");
        NOMINATIM_TO_CAT.put("historical_sites","attractions");
        NOMINATIM_TO_CAT.put("lifestyle","lifestyle");
        NOMINATIM_TO_CAT.put("monuments","attractions");
        NOMINATIM_TO_CAT.put("museums","attractions");
        NOMINATIM_TO_CAT.put("parks","parks");
        NOMINATIM_TO_CAT.put("religious_sites","attractions");
        NOMINATIM_TO_CAT.put("resting","resting");
    }
}
