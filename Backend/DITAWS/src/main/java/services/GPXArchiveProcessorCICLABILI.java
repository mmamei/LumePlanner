package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import model.City;
import model.POI;
import org.apache.poi.POITextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static services.GPXItinerary.passBy;


/**
 * Created by marco on 20/06/2018.
 */
public class GPXArchiveProcessorCICLABILI {

    public static void main(String[] args) throws Exception {

        List<City> cities = City.getInstance();
        Map<String, List<GPXItinerary>> hm = new HashMap<>();
        for (City city : cities)
            hm.put(city.getName(), new ArrayList<GPXItinerary>());

        File basedir = new File("D:\\LUME-GPX\\CICLABILI");
        File[] dirs = basedir.listFiles();
        for(File cityDir: dirs) {
            if(!cityDir.isDirectory()) continue;
            System.out.println(cityDir);

            for(File itiner: cityDir.listFiles()) {
                if(!itiner.isDirectory()) continue;

                File gpx = new File(itiner.getAbsolutePath()+"/"+itiner.getName()+".GPX");
                File doc = new File(itiner.getAbsolutePath()+"/"+itiner.getName()+".doc");
                File img = new File(itiner.getAbsolutePath()+"/"+itiner.getName()+"_ProfAlt.jpg");
                if(!gpx.isFile() || !doc.isFile() || !img.isFile())
                    System.err.println(gpx.isFile()+" "+doc.isFile()+" "+img.isFile()+" "+itiner.getAbsolutePath());

                String itinerary_id = "";
                String display_name = "";
                String approx_time = "";
                String description = "";
                WordExtractor we = new WordExtractor(new HWPFDocument(new FileInputStream(doc)));
                //System.out.println(we.getText());
                String[] paragraphText = we.getParagraphText();
                for(int i = 0; i < paragraphText.length; i++) {
                    String s = paragraphText[i].toString();
                    if(s.startsWith("NR ITINERARIO")) itinerary_id = s.split(":")[1].trim();
                    if(s.startsWith("TITOLO")) display_name = s.split(":")[1].trim() + " (Bici)";
                    if(s.startsWith("TEMPO MEDIO DI PERCORRENZA")) approx_time = s.split(":")[1].trim();
                    if (s.contains("DESCRIZIONE")) description = paragraphText[i + 1].toString();
                }

                //System.out.println(itinerary_id+"\t"+display_name+"\t"+approx_time+"\t"+description);


                List<Track> l = GPX.read(gpx.getAbsolutePath()).getTracks();
                for (City cp : cities)
                        if (passBy(l,cp))
                            hm.get(cp.getName()).add(new GPXItinerary(itiner.getAbsolutePath(),itinerary_id,gpx.getName(),display_name,approx_time,description,img.getName()));

            }
        }



        File out_dir = new File("C:\\Tomcat7\\webapps\\DITA\\files\\gpx_itineraries");
        for(String name: hm.keySet()) {
            List<GPXItinerary> lgpx = hm.get(name);
            if(lgpx.size() > 0) {
                File dir = new File(out_dir.getAbsoluteFile()+"/"+name);
                dir.mkdirs();
                //System.out.println(dir.getAbsolutePath());
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File f = new File(dir+"/itineraries.json");
                mapper.writeValue(f,lgpx);
                for(GPXItinerary i: lgpx) {
                    String src_dir = i.getOrig_dir();
                    String dest_dir = dir.getAbsolutePath();
                    Files.copy(new File(src_dir+"/"+i.getGpx()).toPath(), new File(dest_dir+"/"+i.getGpx()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(new File(src_dir+"/"+i.getImg()).toPath(), new File(dest_dir+"/"+i.getImg()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        System.out.println("Done");







    }
}


