package com.mcg.entity.flow.connector;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class ParamData implements Comparable, Serializable {
    private static final long serialVersionUID = 7841766934588415698L;
    @XmlAttribute
    private String id ;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String name;
    private String alias = null;

    public ParamData(String id, String type, String name){
        this(id);
        this.type = type;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String getType(){
        return type+" ";
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ParamData(String id){
        this.id = id;
    }

    @Override
    public String toString() {
        return id + "_" + type + "_" +name ;
    }

    @Override
    public int compareTo(Object o) {
        if(o == null)
            return -1;
        ParamData t = (ParamData)o;
        int value = id.compareTo(t.id);
        if(value == 0) {
            value = type.compareTo(t.type);
            if (value == 0) {
                value = name.compareTo(t.name);
                return value;
            }
        }
        return value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullErrorMessage(){
        return id+" "+type +" "+ name;
    }

    public String getOutputMessage(){
        return id+" "+type +" "+ name+" "+alias;
    }

    public String getTN(){
        return type +" "+ name+ " " +alias;
    }

}
