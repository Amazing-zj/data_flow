package com.mcg.entity.flow.connector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class OutputUpdate {
    @XmlAttribute
    private List<String> targetList;
    @XmlElement
    private List<ParamData> deleteList;
    @XmlElement
    private List<ParamData> increaseList;

    public List<String> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<String> targetList) {
        this.targetList = targetList;
    }

    public List<ParamData> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<ParamData> deleteList) {
        this.deleteList = deleteList;
    }

    public List<ParamData> getIncreaseList() {
        return increaseList;
    }

    public void setIncreaseList(List<ParamData> increaseList) {
        this.increaseList = increaseList;
    }
}
