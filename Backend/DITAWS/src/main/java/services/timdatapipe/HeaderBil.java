package services.timdatapipe;

import java.util.Map;

/**
 * Created by marco on 16/06/2017.
 */
public class HeaderBil {
    Map<String,String> header;
    int[][] bil;
    HeaderBil(Map<String,String> header,int[][] bil) {
        this.header = header;
        this.bil = bil;
    }
}
