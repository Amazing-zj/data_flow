package Test;

import java.io.Serializable;

public class Seri implements Comparable, Serializable {
    private static final long serialVersionUID = 4650822134991456284L;
    private int x;
    private int y;
    private String message;

    public Seri(int x, int y){
        this(x,y,null);
    }

    public Seri(int x, int y, String message){
        this.x = x;
        this.y = y;
        this.message = message;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
