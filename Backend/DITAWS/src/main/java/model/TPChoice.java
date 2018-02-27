package model;

/**
 * Created by marco on 27/02/2018.
 */
public class TPChoice {
    private String id;
    private String partenza;
    private String arrivo;
    private String durata;
    private int cambi;

    public TPChoice() {

    }

    public String toString() {
        return id+","+partenza+","+arrivo+","+durata+","+cambi;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartenza() {
        return partenza;
    }

    public void setPartenza(String partenza) {
        this.partenza = partenza;
    }

    public String getArrivo() {
        return arrivo;
    }

    public void setArrivo(String arrivo) {
        this.arrivo = arrivo;
    }

    public String getDurata() {
        return durata;
    }

    public void setDurata(String durata) {
        this.durata = durata;
    }

    public int getCambi() {
        return cambi;
    }

    public void setCambi(int cambi) {
        this.cambi = cambi;
    }
}