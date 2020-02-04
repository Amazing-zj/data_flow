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

package com.mcg.entity.flow.linux;

import java.util.ArrayList;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotBlank;

import com.mcg.entity.flow.FlowBase;
import com.mcg.entity.generate.ExecuteStruct;
import com.mcg.entity.generate.RunResult;
import com.mcg.plugin.execute.ProcessContext;
import com.mcg.plugin.execute.strategy.FlowLinuxStrategy;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class FlowLinux extends FlowBase {

	private static final long serialVersionUID = 6228566251754129624L;
	
	@NotBlank(message = "{flowLinux.id.notBlank}")
	@XmlAttribute
	private String id;
	@Valid
	@XmlElement
	private LinuxCore linuxCore;
	@Valid
	@XmlElement
	private LinuxProperty linuxProperty;
	
	@Override
	public void prepare(ArrayList<String> sequence, ExecuteStruct executeStruct) throws Exception {
        ProcessContext processContext = new ProcessContext();
        processContext.setProcessStrategy(new FlowLinuxStrategy());
        processContext.prepare(sequence, this, executeStruct);
	}

	@Override
	public RunResult execute(ExecuteStruct executeStruct) throws Exception {
        ProcessContext processContext = new ProcessContext();
        processContext.setProcessStrategy(new FlowLinuxStrategy());
        RunResult runResult = processContext.run(this, executeStruct);
        return runResult;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LinuxCore getLinuxCore() {
		return linuxCore;
	}

	public void setLinuxCore(LinuxCore linuxCore) {
		this.linuxCore = linuxCore;
	}

	public LinuxProperty getLinuxProperty() {
		return linuxProperty;
	}

	public void setLinuxProperty(LinuxProperty linuxProperty) {
		this.linuxProperty = linuxProperty;
	}

}
