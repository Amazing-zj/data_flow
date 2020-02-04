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

package com.mcg.entity.flow.wonton;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class WontonMemRule implements Serializable {

	private static final long serialVersionUID = 1L;
	private String Stressor;
	private Integer StressorCount;
	private Integer Timeout;
	private String TimeoutUnit;
	private Boolean Abort;
	private Boolean Metric;
	private boolean switchState;
	
	@JSONField(name = "Stressor")
	public String getStressor() {
		return Stressor;
	}
	public void setStressor(String stressor) {
		Stressor = stressor;
	}
	@JSONField(name = "StressorCount")
	public Integer getStressorCount() {
		return StressorCount;
	}
	public void setStressorCount(Integer stressorCount) {
		StressorCount = stressorCount;
	}
	@JSONField(name = "Timeout")
	public Integer getTimeout() {
		return Timeout;
	}
	public void setTimeout(Integer timeout) {
		Timeout = timeout;
	}
	@JSONField(name = "TimeoutUnit")
	public String getTimeoutUnit() {
		return TimeoutUnit;
	}
	public void setTimeoutUnit(String timeoutUnit) {
		TimeoutUnit = timeoutUnit;
	}
	@JSONField(name = "Abort")
	public Boolean getAbort() {
		return Abort;
	}
	public void setAbort(Boolean abort) {
		Abort = abort;
	}
	@JSONField(name = "Metric")
	public Boolean getMetric() {
		return Metric;
	}
	public void setMetric(Boolean metric) {
		Metric = metric;
	}
	public boolean isSwitchState() {
		return switchState;
	}
	public void setSwitchState(boolean switchState) {
		this.switchState = switchState;
	}
}
