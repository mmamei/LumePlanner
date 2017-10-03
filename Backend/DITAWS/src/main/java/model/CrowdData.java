package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by marco on 02/10/2017.
 */
public class CrowdData {

    private String time;

    private double ulxmap;
    private double ulymap;
    private double xdim;
    private double ydim;

    private int minj;
    private int maxi;

    private int maxj;
    private int mini;

    private int nrows;
    private int ncols;

    private double ox;
    private double oy;

    private int[][] avalues;
    private float[][] mvalues;

    public CrowdData(){}
    public CrowdData(String time, double ulxmap,double ulymap,double xdim,double ydim,int minj,int maxi,
            int maxj,int mini,int nrows,int ncols,double ox,double oy,int[][] avalues,float[][] mvalues){
        this.time = time;
        this.ulxmap = ulxmap;
        this.ulymap = ulymap;
        this.xdim = xdim;
        this.ydim = ydim;
        this. minj = minj;
        this.maxi = maxi;
        this.maxj = maxj;
        this.mini = mini;
        this.nrows = nrows;
        this.ncols = ncols;
        this.ox = ox;
        this.oy = oy;
        this.avalues = avalues;
        this.mvalues = mvalues;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getUlxmap() {
        return ulxmap;
    }

    public void setUlxmap(double ulxmap) {
        this.ulxmap = ulxmap;
    }

    public double getUlymap() {
        return ulymap;
    }

    public void setUlymap(double ulymap) {
        this.ulymap = ulymap;
    }

    public double getXdim() {
        return xdim;
    }

    public void setXdim(double xdim) {
        this.xdim = xdim;
    }

    public double getYdim() {
        return ydim;
    }

    public void setYdim(double ydim) {
        this.ydim = ydim;
    }

    public int getMinj() {
        return minj;
    }

    public void setMinj(int minj) {
        this.minj = minj;
    }

    public int getMaxi() {
        return maxi;
    }

    public void setMaxi(int maxi) {
        this.maxi = maxi;
    }

    public int getMaxj() {
        return maxj;
    }

    public void setMaxj(int maxj) {
        this.maxj = maxj;
    }

    public int getMini() {
        return mini;
    }

    public void setMini(int mini) {
        this.mini = mini;
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public double getOx() {
        return ox;
    }

    public void setOx(double ox) {
        this.ox = ox;
    }

    public double getOy() {
        return oy;
    }

    public void setOy(double oy) {
        this.oy = oy;
    }

    public int[][] getAvalues() {
        return avalues;
    }

    public void setAvalues(int[][] avalues) {
        this.avalues = avalues;
    }

    public float[][] getMvalues() {
        return mvalues;
    }

    public void setMvalues(float[][] mvalues) {
        this.mvalues = mvalues;
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


}
