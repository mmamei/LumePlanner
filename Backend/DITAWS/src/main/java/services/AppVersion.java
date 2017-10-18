package services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.Buffer;

/**
 * Created by marco on 17/10/2017.
 */
public class AppVersion {

    public static void  main(String[] args) {
        System.out.println(getVersion());
    }

    public static String getVersion() {
        String app_version = null;
        try {

            BufferedReader br = new BufferedReader(new FileReader("G:\\CODE\\IJ-IDEA\\LumePlanner\\FrontEnd3\\config.xml"));
            br.readLine();
            int start = "<widget id=\"it.unimore.morselli.lume\" version=\"".length();
            String line = br.readLine();
            app_version = line.substring(start,line.indexOf("\"",start));
            br.close();

        }catch(Exception e) {
            e.printStackTrace();
        }
        return app_version;
    }

}
