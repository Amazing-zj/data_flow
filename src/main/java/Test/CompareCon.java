package Test;

public class CompareCon implements Comparable {
    private String type;
    private String name;

    @Override
    public int compareTo(Object o) {
        CompareCon o1 = (CompareCon) o;
        int value = type.compareTo(o1.type);
        if (value == 0) {
            value = name.compareTo(o1.name);
        }
        return value;
    }

    public CompareCon(String type,String name){
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type +"_" + name;
    }
}
