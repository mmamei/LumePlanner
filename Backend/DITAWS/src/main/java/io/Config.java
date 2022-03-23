package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class Config {

    private HashMap<String,String> config = new HashMap<>();

    public Config() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try {
            BufferedReader br = new BufferedReader(new FileReader("/home/mamei/lumeplannerconfig.txt"));
            String line;
            while((line = br.readLine())!=null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//"))
                    continue;
                String[] kv = line.split("=");
                config.put(kv[0].trim(),kv[1].trim());
            }

            br.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return config.get(key);
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String v = new Config().get("OSMFile");
        System.out.println(v);
    }
}
