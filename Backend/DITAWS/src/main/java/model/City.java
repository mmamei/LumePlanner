package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 19/10/2016.
 */
public class City {
    private String name;
    private String pretty_name;
    private String imgFile;
    private double[] lonLatBBox;


    public City() {
    }

    public City(String name, String pretty_name, String imgFile, double[] lonLatBBox) {
        this.name = name;
        this.pretty_name = pretty_name;
        this.imgFile = imgFile;
        this.lonLatBBox = lonLatBBox;
    }


    public boolean contains(double lat, double lon) {
        return (lonLatBBox[0] < lon && lon <lonLatBBox[2] &&
                lonLatBBox[1] < lat && lat <lonLatBBox[3]);

    }

    //"G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.csv"
    public static List<City> getInstance() {
        List<City> cities = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            cities.addAll((List<City>)mapper.readValue(new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\citiesExtra.json"),new TypeReference<List<City>>(){}));
            cities.addAll((List<City>)mapper.readValue(new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities.json"),new TypeReference<List<City>>(){}));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cities;
    }

    public static City getInstance(String name) {
        for(City city: getInstance())
            if(city.getName().equals(name))
                return city;
        return null;
    }

    public String toString() {
        return name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPretty_name() {
        return pretty_name;
    }

    public void setPretty_name(String pretty_name) {
        this.pretty_name = pretty_name;
    }

    public String getImgFile() {
        return imgFile;
    }

    public void setImgFile(String imgFile) {
        this.imgFile = imgFile;
    }

    public double[] getLonLatBBox() {
        return lonLatBBox;
    }

    public void setLonLatBBox(double[] lonLatBBox) {
        this.lonLatBBox = lonLatBBox;
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
    @Override
    public boolean equals(Object o) {
        if(o instanceof City) return name.equals(((City)o).name);
        return false;
    }


    public static void main(String[] args) {
        List<City> cities = City.getInstance();
        System.out.println(cities.size());
    }
}
