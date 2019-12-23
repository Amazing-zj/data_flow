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

package com.mcg.controller;

import java.io.*;
import java.util.*;

import com.mcg.entity.flow.connector.ConnectorData;
import com.mcg.entity.flow.connector.ParamData;
import com.mcg.entity.flow.text.FlowText;
import com.mcg.entity.flow.text.TextCore;
import com.mcg.entity.flow.text.TextProperty;
import com.mcg.plugin.ehcache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mcg.common.Constants;
import com.mcg.controller.base.BaseController;
import com.mcg.util.PageData;

/**
 * DESC : for upload and down
 * DATA : 2019/11/6 16:17
 * AUTHOR : UDEAN
 */
@Controller
@RequestMapping(value="/tool")
public class ToolController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(ToolController.class);

	@RequestMapping(value = "down", method = RequestMethod.POST)
	public ModelAndView down() {
		ModelAndView modeAndView = new ModelAndView("redirect:/downloadFlow");
		PageData pd = this.getPageData();
		String id = null;
		if (pd != null && (id = pd.getString("flowId")) != null && pd.get("flowName") != null) {
			modeAndView.addObject("flowId", id);
			modeAndView.addObject("fileName", pd.getString("flowName") + Constants.EXTENSION);
		}
		int len = Integer.valueOf((String) pd.get("length"));
		int i = 1;
		Map<String, FlowText> map = new TreeMap();
		while (i <= len) {
			if (pd.get(Constants.ID_PREX + i) != null) {
				FlowText object = CachePlugin.getFlowEntity(id, pd.getString(Constants.ID_PREX + i));
					if (object != null) {
					map.put(object.getTextId(), object);
				}
			}
			i++;
		}

		DownloadController.setMap(map);
		codeTrans(map);
		return modeAndView;
	}

	/**
	 * DESC : invoke compiler function
	 * DATE : 2019/12/16 16:40
	 * AUTHOR : UDEAN
	 */
	@RequestMapping(value = "/compiler", method = RequestMethod.POST)
	public ModelAndView invokeCompiler(String text, String random) {
		ModelAndView mv = new ModelAndView("redirect:/compilerCode");
		logger.debug(text);
		mv.addObject("text", text.replaceAll("Level", "P"));
		mv.addObject("random", random);
		return mv;
	}

	private void codeTrans(Map map) {
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(new File("E://recovery//persistent.txt"));
			bw = new BufferedWriter(fw);
			testAlias(bw);
//			bw.flush();
//			bw.close();
//			fw.close();
//			fw = new FileWriter(new File("E://recovery//code.txt"));
			bw.flush();
			bw.close();
			fw.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * DESC : code trans to DFC code and save as local dfc file
	 * DATE : 2019/12/12 16:28
	 * AUTHOR : UDEAN
	 */
	private void testCodeTrans(BufferedWriter bw, Map map) throws Exception {
		int dependency = DependencyCache.size();
		int i = 0;
		int index = 0;
		int name_index = 0;
		String Parm = "Source_";
//        String Program = "Program_";
		while (i < dependency) {
			List<String> list = (List) DependencyCache.get(i++);
			for (String str : list) {
				FlowText flowText = (FlowText) map.get(str);
				str = str.substring(0, 18);
//                Set<ParamData>paramOutDataSet = (Set)OutputCache.get(str);
//                Map<String,ParamData>paramInDataMap = (HashMap) InputCache.get(str);
//                StringBuilder text = new StringBuilder("void Program_");
//                text.append(++name_index+"(");
//                if(paramInDataMap != null && paramInDataMap.size() != 0){
//                    for(String mapKey : paramInDataMap.keySet()){
//                        Set<ParamData>paramDataSet = (Set)paramInDataMap.get(mapKey);
//                        if(paramDataSet != null && paramDataSet.size() != 0){
//                            for(ParamData data: paramDataSet){
//                                text.append(data.getType()+data.getAlias()+",");
//                            }
//                        }
//                    }
//                }
//                if(text.indexOf(",",text.length()-1) != -1){
//                    text.deleteCharAt(text.length()-1);
//                }
//                text.append(";");
//                if(paramOutDataSet != null && paramOutDataSet.size() != 0){
//                    for(ParamData data : paramOutDataSet){
//                        if(data.getAlias() == null){
//                            data.setAlias(Parm+index++);
//                        }
//                        text.append(data.getType()+data.getAlias()+",");
//                    }
//                }
//                if(text.indexOf(",",text.length()-1) != -1){
//                    text.deleteCharAt(text.length()-1);
//                }
//                text.append("){");
//                bw.write(text.toString());
				bw.newLine();
				bw.write(flowText.getTextCore().getSource());
				bw.newLine();
				bw.write("}");
				bw.newLine();
				bw.newLine();
			}
		}

	}

	private void testAlias(BufferedWriter bw) throws Exception {
		bw.write("Input:");
		bw.newLine();
		int dependency = DependencyCache.size();
		int i = 0;
		int index = 0;
		String Parm = "Parm";// TODO: 2019/12/5 19:13 adopt data saving format, check with ZJ
		while (i < dependency) {
			bw.write("Level " + i + ":");
			bw.newLine();
			List<String> list = (List) DependencyCache.get(i++);
			for (String str : list) {
				str = str.substring(0, 18);
				Set<ParamData> paramDataList = (Set) OutputCache.get(str);
				logger.info("acquire output cache with key: " + str);
				if (paramDataList == null)
					continue;
				bw.write(str + "----");
				for (ParamData paramData : paramDataList) {
					//save more than one time occur
//					if(paramData.getAlias() != null){
//						logger.error(paramData.getAlias());
//					}
					paramData.setAlias(Parm + index);
					bw.write(paramData.getTN());
					bw.newLine();
					index++;
				}
			}
		}
		i = 1;
		bw.write("Output:");
		bw.newLine();
		while (i < dependency) {
			List<String> list = (List) DependencyCache.get(i++);
			for (String str : list) {
				str = str.substring(0, 18);
				logger.info("acquire input data with key: " + str);
				Map<String, ParamData> paramDataMap = (HashMap) InputCache.get(str);
				if (paramDataMap == null) {
					continue;
				}
				bw.write(str + "-----");
				for (String source : paramDataMap.keySet()) {
					Set<ParamData> paramDataSet = (Set) paramDataMap.get(source);
					if (paramDataSet == null || paramDataSet.size() == 0) {
						bw.write("there is not input data from: " + source);
						continue;
					}
					for (ParamData paramData : paramDataSet) {
						if (paramData.getAlias() == null) {
							logger.error(paramData.getFullErrorMessage());
						}
						bw.write(paramData.getOutputMessage());
						bw.newLine();
					}
				}
			}
		}
	}

	private void saveData(Set<FlowText> set) {
		FileWriter fw;
		BufferedWriter bw;
//		SpendTime time = new SpendTime();
//		time.clockUp();
		try {
			fw = new FileWriter(new File("E://recovery//persistent.txt"));
			bw = new BufferedWriter(fw);
			if (set.size() != 0) {
				for (FlowText temp : set) {
					TextProperty property = temp.getTextProperty();
					TextCore core = temp.getTextCore();
					bw.write(temp.getTextId());
					bw.newLine();
					bw.write(property.getName() + " " + property.getKey());
					bw.newLine();
					bw.write(core.getSource());
					bw.newLine();
				}
			}
			ConnectController.cachePersistent(bw);
			bw.flush();

			bw.close();
			fw.close();
//			time.clockOver();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cachePersistent(ConnectorData a[], BufferedWriter bw) throws Exception {
		if (a.length == 0)
			return;
		try {
			for (ConnectorData data : a) {
				bw.write(data.getSourceId() + " " + data.getTargetId());
				bw.newLine();
			}
		} finally {
			ConnectorCache.removeAll();
		}
	}
}
    
//    @RequestMapping(value="downFlowGenFile", method=RequestMethod.POST)
//    public ModelAndView downFlowGenFile() {
//        ModelAndView modeAndView = new ModelAndView("redirect:/download");
//    	PageData pd = this.getPageData();
//    	String path = (String)pd.get("path");
//
//        if(StringUtils.isNotEmpty(path)) {
//            modeAndView.addObject("filePath", path);
//            int pos = path.lastIndexOf(File.separator);
//        	String fileName = path.substring(pos+1);
//            modeAndView.addObject("fileName", fileName);
//
//        }
//
//    	return modeAndView;
//    }
//
//    @RequestMapping(value="upload", method=RequestMethod.POST)
//    @ResponseBody
//    public McgResult upload(@RequestParam(value = "flowFile", required = false) MultipartFile file, @RequestParam String flowId, HttpSession session) {
//        Message messageComplete = MessagePlugin.getMessage();
//        messageComplete.getHeader().setMesType(MessageTypeEnum.NOTIFY);
//        NotifyBody notifyBody = new NotifyBody();
//    	McgResult result = new McgResult();
//
//    	if(file == null) {
//    		logger.error("导入文件丢失");
//            result.setStatusCode(0);
//            result.setStatusMes("导入文件丢失");
//            notifyBody.setContent("导入文件丢失，请刷新页面后重试！");
//            notifyBody.setType(LogTypeEnum.ERROR.getValue());
//            messageComplete.setBody(notifyBody);
//            MessagePlugin.push(session.getId(), messageComplete);
//            return result;
//    	}
//
//    	if(file.getOriginalFilename().endsWith(Constants.EXTENSION)){
//
//	    	File targetFile = new File(Constants.DATA_PATH + flowId + Constants.EXTENSION);
//	        try {
//	        	if (!targetFile.getParentFile().exists()) {
//	        		targetFile.getParentFile().mkdirs();
//	        	}
//
//	        	file.transferTo(targetFile);
//	        	byte[] bytes = FileUtils.readFileToByteArray(targetFile);
//	        	Des des = new Des(Constants.DES_KEY);
//	        	LevelDbUtil.put(flowId.getBytes(Constants.CHARSET), des.decrypt(bytes));
//	        	FileUtils.forceDelete(targetFile);
//
//		        notifyBody.setContent("导入流程文件成功！");
//		        notifyBody.setType(LogTypeEnum.SUCCESS.getValue());
//			} catch (Exception e) {
//				logger.error("导入流程失败，异常信息：{}", e.getMessage());
//				result.setStatusCode(0);
//				result.setStatusMes("导入失败");
//		        notifyBody.setContent("导入流程文件异常！");
//		        notifyBody.setType(LogTypeEnum.ERROR.getValue());
//			}
//    	} else {
//    		logger.error("流程文件格式无效！文件名：{}", file.getOriginalFilename());
//	        notifyBody.setContent("流程文件格式无效！");
//	        notifyBody.setType(LogTypeEnum.ERROR.getValue());
//    	}
//
//        messageComplete.setBody(notifyBody);
//        MessagePlugin.push(session.getId(), messageComplete);
//        return result;
//    }
//}