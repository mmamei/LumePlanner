package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import model.City;
import model.POI;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

/**
 * Created by marco on 02/07/2018.
 */
public class CeliachiaProcessor {

    public static void main(String[] args) throws Exception {
        String jt = IOUtils.toString(new BufferedReader(new FileReader("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\celiachia.txt")));
        JSONObject jo = new JSONObject(jt.substring(1,jt.length()-1));
        Set<String> keys = jo.keySet();

        /*
        {"ID":"7","Denominazione":"Ruhental","PartitaIva":"02323730693","Indirizzo":"Via Follani, 138",
        "Cap":"66034","Telefono":"0872.44101","Cellulare":null,"IndirizzoWeb":null,"Email":"marco.berardi1978@hotmail.it",
        "Longitudine":"14.39303","Latitudine":"42.21288","GiornoRiposo":null,"Asporto":"0","CostoMedioPasto":"10",
        "ServizioRistorazione":"0","ConoSenzaGlutine":"0","Stagionale":"0","PrimoPiattoSurgelato":"0","Market":"0",
        "DenominazioneAlimViaggio":null,"Altro":"0","SpaziGlutenFree":"0","UnitaLocale":"Nessuna","NumeroStelle":null,
        "CodiceFiscale":null,"RagioneSociale":null,"PeriodoChiusura":"mar e a pr","Note":null,"NomeRegione":"Abruzzo",
        "NomeRegioneItalia":"Abruzzo","NomeProvincia":"Chieti","SiglaProvincia":"CH","NomeLocalita":"Lanciano","Titolare":null,
        "ReferenteSG":null,"CategoriaGuida":"Pizzerie","Tipo1":"P","Tipo2":"Tr","Tipo3":null,"Tipo4":null,"Tipo5":null,
        "TipoEsteso":"Pizzeria Trattoria ","ElencoCollaborazioni":null}
         */

        List<POI> list = new ArrayList<>();
        for(String k: keys) {
            //System.out.println(k);
            JSONObject jp = jo.getJSONObject(k);
            String id = "celi"+k;
            double lat = jp.getDouble("Latitudine");
            double lon = jp.getDouble("Longitudine");
            String name = jp.getString("Denominazione");
            String category = "eating";
            String type = "celiachia";
            String img = null;
            String desc = jp.getString("ConoSenzaGlutine").equals("0")? "Glutine" : "Senza Glutine";
            Object owww = jp.get("IndirizzoWeb");
            String www = owww == null ? null : String.valueOf(owww);
            list.add(new POI(id, lat, lon, name, category, type, 0.1f , "", 0, "ok", "ok", 0, img, desc, www));
        }

        System.out.println("TOTAL POIS IN FILE = " + list.size());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // map to cities
        List<City> cities = City.getInstance();
        Map<String, List<POI>> hm = new HashMap<>();
        for (City city : cities)
            hm.put(city.getName(), new ArrayList<POI>());

        for (POI p : list)
            for (City cp : cities)
                if (cp.contains(p.getGeometry().getCoordinates().getLatitude(), p.getGeometry().getCoordinates().getLongitude()))
                    hm.get(cp.getName()).add(p);

        for (String city : hm.keySet()) {
            System.out.println(city + " ==> " + hm.get(city).size());
            for (POI p : hm.get(city))
                System.out.print(p.getPlace_id() + ",");
            System.out.println();


            if (hm.get(city).size() > 0) {
                File dir = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities-celiachia\\" + city + "\\pois");
                dir.mkdirs();
                File f = new File(dir+"/celiachia.json");
                System.out.println(f.getAbsolutePath());
                mapper.writeValue(f, hm.get(city));
            }
        }
    }
}
