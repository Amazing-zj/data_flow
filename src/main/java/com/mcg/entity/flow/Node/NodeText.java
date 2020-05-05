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

package com.mcg.entity.flow.Node;

import com.mcg.entity.flow.FlowBase;
import com.mcg.entity.generate.ExecuteStruct;
import com.mcg.entity.generate.RunResult;
import com.mcg.plugin.execute.ProcessContext;
import com.mcg.plugin.execute.strategy.FlowTextStrategy;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class NodeText extends FlowBase implements Comparable{

    private static final long serialVersionUID = -6575685975247235282L;
    @NotBlank(message = "{flowText.nodeId.notBlank}")
    @XmlAttribute
    private String nodeId;
    @Valid
    @XmlElement
    private NodeProperty nodeProperty;
    @Valid
    @XmlElement
    private NodeCore nodeCore;

    public NodeProperty getNodeProperty() {
        return nodeProperty;
    }

    public void setNodeProperty(NodeProperty nodeProperty) {
        this.nodeProperty = nodeProperty;
    }

    public NodeCore getNodeCore() {
        return nodeCore;
    }

    public void setNodeCore(NodeCore nodeCore) {
        this.nodeCore = nodeCore;
    }

    @Override
    public String toString() {

        return  "name: " +nodeProperty.getName() + " code: " +nodeCore.getSource();
    }

    @Override
    public int compareTo(Object o) {
        NodeText o1 = (NodeText)o;
        return nodeId.compareTo(o1.nodeId);
    }

    @Override
    public void prepare(ArrayList<String> sequence, ExecuteStruct executeStruct) throws Exception {
        ProcessContext processContext = new ProcessContext();
        processContext.setProcessStrategy(new FlowTextStrategy());
        processContext.prepare(sequence, this, executeStruct);
    }

    @Override
    public RunResult execute(ExecuteStruct executeStruct) throws Exception {
        ProcessContext processContext = new ProcessContext();
        processContext.setProcessStrategy(new FlowTextStrategy());
        RunResult runResult = processContext.run(this, executeStruct);
        return runResult;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }


}
