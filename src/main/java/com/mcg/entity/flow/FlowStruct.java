/*
 * @Copyright (c) 2018 缪聪(mcg-helper@qq.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");  
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at  
 *     
 *     http://www.apache.org/licenses/LICENSE-2.0  
 *     
 * Unless required by applicable law or agreed to in writing, software  
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 */

package com.mcg.entity.flow;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mcg.entity.flow.data.FlowDatas;
import com.mcg.entity.flow.end.FlowEnd;
import com.mcg.entity.flow.java.FlowJavas;
import com.mcg.entity.flow.linux.FlowLinuxs;
import com.mcg.entity.flow.python.FlowPythons;
import com.mcg.entity.flow.script.FlowScripts;
import com.mcg.entity.flow.sqlexecute.FlowSqlExecutes;
import com.mcg.entity.flow.sqlquery.FlowSqlQuerys;
import com.mcg.entity.flow.start.FlowStart;
import com.mcg.entity.flow.text.FlowTexts;
import com.mcg.entity.flow.wonton.FlowWontons;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class FlowStruct implements Serializable {

    private static final long serialVersionUID = 5270098230104370957L;
    /* 流程实例中控件的数量 */
    @XmlAttribute
    private int totalSize;
    @XmlAttribute
    private String mcgId;
    @XmlElement
    private FlowStart flowStart;
    @XmlElement
    private FlowSqlExecutes flowSqlExecutes;    
    @XmlElement
    private FlowSqlQuerys flowSqlQuerys;     
    @XmlElement
    private FlowDatas flowDatas;
    @XmlElement
    private FlowTexts flowTexts;
    @XmlElement
    private FlowScripts flowScripts;
    @XmlElement
    private FlowJavas flowJavas;
    @XmlElement
    private FlowEnd flowEnd;
	@XmlElement
    private FlowPythons flowPythons;
    @XmlElement
    private FlowLinuxs flowLinuxs;
    @XmlElement
    private FlowWontons flowWontons;
    
    public String getMcgId() {
        return mcgId;
    }
    public void setMcgId(String mcgId) {
        this.mcgId = mcgId;
    }
    public FlowStart getFlowStart() {
        return flowStart;
    }
    public void setFlowStart(FlowStart flowStart) {
        this.flowStart = flowStart;
    }
	public FlowTexts getFlowTexts() {
		return flowTexts;
	}
	public FlowEnd getFlowEnd() {
		return flowEnd;
	}
	public void setFlowEnd(FlowEnd flowEnd) {
		this.flowEnd = flowEnd;
	}
	public void setFlowTexts(FlowTexts flowTexts) {
		this.flowTexts = flowTexts;
	}
	public FlowScripts getFlowScripts() {
		return flowScripts;
	}
	public void setFlowScripts(FlowScripts flowScripts) {
		this.flowScripts = flowScripts;
	}
	public FlowJavas getFlowJavas() {
		return flowJavas;
	}
	public void setFlowJavas(FlowJavas flowJavas) {
		this.flowJavas = flowJavas;
	}
	public FlowDatas getFlowDatas() {
		return flowDatas;
	}
	public void setFlowDatas(FlowDatas flowDatas) {
		this.flowDatas = flowDatas;
	}
    public int getTotalSize() {
        return totalSize;
    }
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
    public FlowSqlQuerys getFlowSqlQuerys() {
        return flowSqlQuerys;
    }
    public void setFlowSqlQuerys(FlowSqlQuerys flowSqlQuerys) {
        this.flowSqlQuerys = flowSqlQuerys;
    }
	public FlowSqlExecutes getFlowSqlExecutes() {
		return flowSqlExecutes;
	}
	public void setFlowSqlExecutes(FlowSqlExecutes flowSqlExecutes) {
		this.flowSqlExecutes = flowSqlExecutes;
	}
	public FlowPythons getFlowPythons() {
		return flowPythons;
	}
	public void setFlowPythons(FlowPythons flowPythons) {
		this.flowPythons = flowPythons;
	}
    public FlowLinuxs getFlowLinuxs() {
		return flowLinuxs;
	}
	public void setFlowLinuxs(FlowLinuxs flowLinuxs) {
		this.flowLinuxs = flowLinuxs;
	}
	public FlowWontons getFlowWontons() {
		return flowWontons;
	}
	public void setFlowWontons(FlowWontons flowWontons) {
		this.flowWontons = flowWontons;
	}


}