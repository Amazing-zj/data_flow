package com.mcg.entity.flow.connector;

import java.io.Serializable;

public class ConnectorData implements Comparable, Serializable {

    private static final long serialVersionUID = -7752950538316618860L;
    private String sourceId;
    private String targetId;

    @Override
    public String toString() {
        return "source:" + sourceId +" target: "+targetId;
    }

    @Override
    public int compareTo(Object o) {
        ConnectorData temp = (ConnectorData) o;
        String source = temp.getSourceId();
        if(source.equals(sourceId)){
            String target = temp.getTargetId();
            if(target.equals(targetId)) {
                return 0;
            }else{
                if(target.compareTo(targetId)>0){
                    return  -1;
                }else{
                    return 1;
                }
            }
        }else{
            if(source.compareTo(sourceId) > 0){
                return -1;
            }else{
                return 1;
            }
        }
    }


    public ConnectorData(String sourceId , String targetId){
        setSourceId(sourceId);
        setTargetId(targetId);
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
