package services;

import io.Config;
import io.RESTController;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.Buffer;

/**
 * Created by marco on 17/10/2017.
 */
public class AppVersion {
    private static Logger logger = Logger.getLogger(RESTController.class);
    public static void  main(String[] args) {
        System.out.println(getVersion());
    }

    public static String getVersion() {
        String app_version = null;
        try {
            Config config = new Config();
            BufferedReader br = new BufferedReader(new FileReader(config.get("APP_DIR")+"/config.xml"));
            br.readLine();
            int start = "<widget id=\"it.unimore.morselli.lume\" version=\"".length();
            String line = br.readLine();
            app_version = line.substring(start,line.indexOf("\"",start));
            br.close();

        }catch(Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        return app_version;
    }

}
