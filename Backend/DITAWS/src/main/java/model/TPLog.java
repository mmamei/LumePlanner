package model;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by marco on 27/02/2018.
 */
public class TPLog {
    private String user;
    private int selected;
    private ArrayList<TPChoice> alt;


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(new GregorianCalendar().getTime()+",");
        sb.append(user+",");
        sb.append(selected+",");
        for(TPChoice tpc: alt)
            sb.append(tpc+",");
        return sb.toString();
    }



    public TPLog() {
        alt = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public ArrayList<TPChoice> getAlt() {
        return alt;
    }

    public void setAlt(ArrayList<TPChoice> alt) {
        this.alt = alt;
    }
}