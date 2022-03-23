package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import model.City;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by marco on 20/06/2018.
 */
public class GPXArchiveProcessorDISABILI {

    public static void main(String[] args) throws Exception {
        //process("D:\\LUME-GPX\\DISABILI\\BOLOGNA OK","BOLOGNA itinerari con schede.xls");
        //process("D:\\LUME-GPX\\DISABILI\\RAVENNA OK","RAVENNA itinerari con schede.xls");
        //process("D:\\LUME-GPX\\DISABILI\\RIMINI OK","RIMINI itinerari con schede.xls");

        //process("D:\\LUME-GPX\\DISABILI\\FERRARA OK","FERRARA itinerari con schede.xls");
        //process("D:\\LUME-GPX\\DISABILI\\PARMA OK","PARMA itinerari con schede.xls");
        //process("D:\\LUME-GPX\\DISABILI\\PIACENZA OK","PIACENZA itinerari con schede.xls");
        //process("D:\\LUME-GPX\\DISABILI\\FORLI CESENA OK","FORLI CESENA itinerari con schede.xls");
        process("D:\\LUME-GPX\\DISABILI\\REGGIO EMILIA OK","REGGIO EMILIA itinerari con schede.xls");


        System.out.println("Done");
    }

    public static void process(String indir, String infile) throws Exception {

        Workbook workbook = WorkbookFactory.create(new File(indir+"\\"+infile));

        Sheet sheet = workbook.getSheetAt(0);
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        Map<String, List<GPXItinerary>> h = new HashMap<>();

        int r = 0;
        for (Row row: sheet) {
            if(r > 1) {

                String comune = null;
                String file = null;
                String cartella = null;
                String da = null;
                String a = null;
                String pinteresse = null;
                String mt = null;
                String durata = null;
                String diff = null;
                boolean manuale = false;
                boolean accompagnatore = false;
                boolean triride = false;
                boolean elettrica = false;
                boolean bagniH = false;
                String itinerario = null;
                String pendenze = null;
                String pavimentazione = null;
                String monumenti = null;
                String note = null;



                int c = 0;
                for (Cell cell : row) {
                    String v = dataFormatter.formatCellValue(cell);
                    if(c == 1) comune = v.trim();
                    if(c == 2) file = v.trim();
                    if(c == 3) cartella = v.trim();
                    if(c == 4) da = v.trim();
                    if(c == 5) a = v.trim();
                    if(c == 6) pinteresse = v.trim();
                    if(c == 7) mt = v.trim();
                    if(c == 8) durata = v.trim()+"min";
                    if(c == 9) diff = v.trim();
                    if(c == 10) manuale = v.equalsIgnoreCase("x");
                    if(c == 11) accompagnatore = v.equalsIgnoreCase("x");
                    if(c == 12) triride = v.equalsIgnoreCase("x");
                    if(c == 13) elettrica = v.equalsIgnoreCase("x");
                    if(c == 14) bagniH = v.equalsIgnoreCase("x");
                    if(c == 15) itinerario = v.trim();
                    if(c == 16) pendenze = v.trim();
                    if(c == 17) pavimentazione = v.trim();
                    if(c == 18) monumenti = v.trim();
                    if(c == 19) note = v.trim();
                    c++;
                }
                System.out.println(file);


                String description = pinteresse != null ? "Questo itinerario passa tra i seguenti punti di interesse: "+pinteresse+". ":
                                                               "Questo itinerario parte da "+da+" e arriva a "+a+". ";
                description +=  "La pavimentazione è "+pavimentazione+". ";
                if(manuale) description+= "Adatto a carrozzina manuale. ";
                if(accompagnatore) description+= "Adatto a carrozzina con accompagnatore. ";
                if(triride) description+= "Adatto a triride. ";
                if(elettrica) description+= "Adatto a carrozzina elettrica. ";
                if(bagniH) description+= "Offre bagni per disabili. ";
                if(monumenti != null) description+=monumenti+". ";
                if(note != null) description+=note+". ";


                if(comune!=null && !comune.isEmpty()) {
                    List<GPXItinerary> lgpx = h.get(comune);
                    if (lgpx == null) {
                        lgpx = new ArrayList<>();
                        h.put(comune, lgpx);
                    }
                    String display_name = "da " + da + " a " + a+ " (Percorso accessibile)";
                    lgpx.add(new GPXItinerary(indir+"\\"+cartella, "dis" + cartella, file, display_name, durata, description, null));
                }

            }
            r++;
        }

        File out_dir = new File("C:\\Tomcat7\\webapps\\DITA\\files\\gpx_itineraries");
        for(String name: h.keySet()) {
            List<GPXItinerary> lgpx = h.get(name);
            if(lgpx.size() > 0) {
                File dir = new File(out_dir.getAbsoluteFile()+"/"+name);
                System.out.println(dir);
                dir.mkdirs();
                //System.out.println(dir.getAbsolutePath());
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File f = new File(dir+"/itineraries_disabili.json");
                mapper.writeValue(f,lgpx);
                for(GPXItinerary i: lgpx) {
                    String src_dir = i.getOrig_dir();
                    String dest_dir = dir.getAbsolutePath();
                    Files.copy(new File(src_dir+"/"+i.getGpx()).toPath(), new File(dest_dir+"/"+i.getGpx()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}


