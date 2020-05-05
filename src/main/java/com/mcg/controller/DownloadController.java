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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mcg.entity.common.McgResult;
import com.mcg.entity.flow.Node.NodeText;
import com.mcg.entity.flow.connector.ParamData;
import com.mcg.entity.flow.text.FlowText;
import com.mcg.plugin.ehcache.*;
import com.mcg.util.SSH;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mcg.common.Constants;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @ClassName:   DownloadController   
 * @Description: TODO(下载功能) 
 * @author:      缪聪(mcg-helper@qq.com)
 * @date:        2018年3月9日 下午5:02:39  
 *
 */
@Controller
public class DownloadController {

	private static Logger logger = LoggerFactory.getLogger(DownloadController.class);
	private static Map<String, FlowText> map = null;
    private static int static_index = 0;

	public static void setMap(Map map1){
	    map = map1;
    }

	//使用downloadFlow替代download
//	@RequestMapping("download")
//	public String download(String filePath, String fileName, HttpServletRequest request, HttpServletResponse response) {
//
//		response.setCharacterEncoding(Constants.CHARSET.toString());
//		response.setContentType("multipart/form-data");
//		response.setHeader("Content-Disposition", "attachment;fileName=" + convertCharacterEncoding(request, fileName));
//		OutputStream os = null;
//		InputStream inputStream = null;
//        try {
//            os = response.getOutputStream();
//            inputStream = new FileInputStream(new File(filePath));
//
//            byte[] b = new byte[1024];
//            int length;
//            while ((length = inputStream.read(b)) > 0) {
//                os.write(b, 0, length);
//            }
//
//        }catch (IOException e) {
//        	logger.error("文件下载出错，异常信息：{}", e.getMessage());
//        }finally {
//            try {
//            	if(os != null) {
//            		os.close();
//            	}
//            	if(inputStream != null) {
//            		inputStream.close();
//            	}
//            } catch (IOException e) {
//            	logger.error("文件下载后关闭流出错，异常信息：{}", e.getMessage());
//            }
//        }
//
//		return null;
//	}

    @RequestMapping("/testOutput")
    public void test(String id){
	    Set<ParamData> list = (Set) OutputCache.get(id);
	    if(list == null) {
            logger.debug("null set");
        }else{
	        for(ParamData paramData : list){
	            logger.info(paramData.toString());
            }
        }
    }

    @RequestMapping("/testInput")
    public void testInput(String id){
	    String source = id.substring(0,36);
	    String target = id.substring(36);
	    Map<String, Set> map = (Map)InputCache.get(target);
	    if(map == null){
	        logger.debug("null map");
        }else{
	        if(map.get(source) == null){
	            logger.debug("null map value");
            }else{
	            logger.error("values");
            }
        }
    }


    @RequestMapping("downloadFlow")
    public String downloadFlow(String fileName, HttpServletRequest request, HttpServletResponse response) {

        response.setCharacterEncoding(Constants.CHARSET.toString());
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + convertCharacterEncoding(request, fileName));
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            if(os != null) {
                CodeTrans(os);
                os.close();
            }
//              Des des = new Des(Constants.DES_KEY);
//            byte[] bytes = des.encrypt(LevelDbUtil.get(flowId.getBytes(Constants.CHARSET)));
//            os.write(bytes);
        }catch (Exception e) {
        	logger.error(e.getMessage());
        }finally {
            try {
                if(os != null) {
                    os.close();
                }
            } catch (IOException e) {
            	logger.error( e.getMessage());
            }
        }
        
        return null;
    }
    /**
     * DESC : download file from remote service
     * DATA : 2020/3/9 13:26
     * AUTHOR : UDEAN
     */
    @RequestMapping("downloadCode")
    public void down(String host, String userName, String password, String Dir, String saveDir, String fileName, HttpServletResponse response){
        response.setCharacterEncoding(Constants.CHARSET.toString());
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        logger.info("from " + Dir + "/" + fileName + " to " + saveDir );
        OutputStream os =  null;
        try{
            os = response.getOutputStream();
            if(os != null){
                logger.info("---------start downloading-------------");
                File file = SSH.download(host, userName, password, Dir, saveDir, fileName);
                if(file.exists()){
                    BufferedReader bw = new BufferedReader(new FileReader(file));
                    String s = null;
                    while((s = bw.readLine()) != null){
                        os.write(s.getBytes());
                        os.write(newline);
                    }
                }
                logger.info("---------download finish-------------");
                os.close();
            }
        }catch (Exception e){
            logger.debug(e.getMessage());
        }

    }

    /**
     * DESC : invoke compiler to transform code and save file to local
     * DATE : 2019/12/16 15:06
     * AUTHOR : UDEAN
     */
    @RequestMapping("compilerCode")
    @ResponseBody
    public McgResult compiler(String text, String host, String password, String userName, String Dir, HttpServletResponse response) {
        McgResult result = new McgResult();
            SSH.upload(host, userName, password, Dir, text);
            response.setStatus(100);
            result.setStatusCode(1);
        return result;
    }

    private static byte [] newline = "\n".getBytes(Constants.CHARSET);
    private void CodeTrans(OutputStream os)throws Exception{
        int dependency = DependencyCache.size();
        int i = 0;
        int index = static_index;
        int name_index = 0;
        String Parm = "Source_";
        Date start = new Date();
        while(i < dependency){
            List<String> list = (List)DependencyCache.get(i++);
            for(String str: list){
                FlowText flowText = null;
                List<String>nameList = new LinkedList<>();
                List<String>aliasList = new LinkedList<>();
                if(map != null) {
                    flowText = map.get(str);
                }
                str = str.substring(0,18) ;
                Set<ParamData> paramOutDataSet = (Set) OutputCache.get(str);
                Map<String,ParamData>paramInDataMap = (HashMap) InputCache.get(str);
                StringBuilder text;
                if(StringUtils.isNotBlank(flowText.getType())) {
                     text = new StringBuilder(flowText.getType());
                }else{
                    text = new StringBuilder("void");
                }
                if(StringUtils.isNotBlank(flowText.getTextProperty().getName()) && isNameLegal(flowText.getTextProperty().getName())) {
                    text.append(" ");
                    text.append(flowText.getTextProperty().getName()+"(");
                }else{
                    text.append(" Process_");
                    text.append(++name_index + "(");
                }
                if(paramInDataMap != null && paramInDataMap.size() != 0){
                    for(String mapKey : paramInDataMap.keySet()){
                        Set<ParamData>paramDataSet = (Set)paramInDataMap.get(mapKey);
                        if(paramDataSet != null && paramDataSet.size() != 0){
                            for(ParamData data: paramDataSet){
                                nameList.add(data.getName());
                                aliasList.add(data.getAlias());
                                text.append(data.getType()+data.getAlias()+",");
                            }
                        }
                    }
                }
                if(text.indexOf(",",text.length()-1) != -1){
                    text.deleteCharAt(text.length()-1);
                }
                text.append(";");
                if(paramOutDataSet != null && paramOutDataSet.size() != 0){
                    for(ParamData data : paramOutDataSet){
                        if(data.getAlias() == null){
                            data.setAlias(Parm+index++);
                        }
                        nameList.add(data.getName());
                        aliasList.add(data.getAlias());
                        text.append(data.getType()+data.getAlias()+",");
                    }
                }
                if(text.indexOf(",",text.length()-1) != -1){
                    text.deleteCharAt(text.length()-1);
                }
                text.append("){");
                os.write(text.toString().getBytes());
                os.write(newline);
                if(flowText != null && flowText.getTextCore() != null) {
                    os.write(replaceSource(flowText.getTextCore().getSource(), nameList, aliasList ).getBytes());
                    os.write(newline);
                }
                os.write("}".getBytes());
                os.write(newline);
                os.write(newline);
            }
            static_index = index;
        }
        logger.info("trans code time: "+ (new Date().getTime() - start.getTime())+ " ms");
        try {
            saveSingle(os);
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        try {
            saveNode(os);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    private void notDepency(OutputStream os){
        try {
            saveSingle(os);
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        try {
            saveNode(os);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    private void saveSingle(OutputStream os) throws Exception{
        List list =CachePlugin.getAll();
        if(list.size() >0){
            StringBuffer string = new StringBuffer();
            char index= 'A';
            for(FlowText flowText :(List<FlowText>) list) {
                if (flowText.isSolo()) {
                    string.append(flowText.getType()+ " ");
                    if (StringUtils.isNotBlank(flowText.getTextProperty().getName()) && isNameLegal(flowText.getTextProperty().getName())){
                        string.append(flowText.getTextProperty().getName());
                    }else{
                        string.append("Singel_"+index++);
                    }
                    string.append("(;){\n" + flowText.getTextCore().getSource()+"\n}\n");
                }
            }
            os.write(string.toString().getBytes());
        }
    }

    private void saveNode(OutputStream os) throws Exception{
        List<NodeText> list = NodeCache.getAll();
        logger.debug(String.valueOf(list.size()));
        if(list.size()>0){
            byte [] newline = "\n".getBytes(Constants.CHARSET);
            for(NodeText nodeText : list){
                os.write(nodeText.getNodeCore().getSource().getBytes());
                os.write(newline);
            }
        }
    }

    private void addNode(OutputStream os) throws Exception{
        List<String>list = NodeCache.getKeys();
        if(list != null && list.size() != 0){
            for(String key : list){
                NodeText text = (NodeText)NodeCache.get(key);
                os.write(text.getNodeCore().getSource().getBytes());
            }
            // TODO: 2020/2/11 13:18 delete NodeCache cache data
        }
    }

    private boolean isNameLegal(String name){
        return name.matches("[A-Za-z](\\w*_*)");
    }

    private String replaceSource(String source, List<String> name, List<String> alias){
//	    logger.info(source);
        if(source == "" || source == null) {
            return  "";
        } else if( name.size() == 0){
          return source;
        } else{
            int i;
            int i_list = 0;
            int len = name.size() ;
//            String str [] = source.split("");
//            StringBuilder s = new StringBuilder(str[0]);
//            String preRegex = "((\\w*\\W+)|(\\W*))";
//            String fixRegex = "((\\W+\\w*\\W*)|(\\W*))";
//            while(i < str.length){
//                i_list = 0;
//                while(i_list < len){
//                    if(str[i].matches(preRegex+name.get(i_list)+fixRegex)){
//                        break;
//                    }
//                    i_list ++;
//                }
//                if(i_list < len){
//                    s.append(str[i].replaceAll(name.get(i_list),alias.get(i_list))+" ");
//                } else {
//                    s.append(str[i]+" ");
//                }
//                i++;
//            }
            while(i_list < len){
                String nameStr = name.get(i_list);
                String aliasStr = alias.get(i_list);
                String str[] = source.split(nameStr);
                i = 1;
                int index;
                StringBuilder s = new StringBuilder(str[0]);
                while(i < str.length){
                    if( str[i-1].substring(str[i-1].length()-1).matches("\\W") &&
                            ( (index = str[i-1].lastIndexOf("\"")) == -1 || !str[i-1].substring(index).matches("\"\\s*") ) &&
                            str[i].charAt(0) != '_' &&
                            ( (index = str[i].indexOf("\"")) == -1 || !str[i].substring(0,index).matches("\\s*\"") )&&
                            str[i].substring(0,1).matches("\\W")
                    ){
                        s.append(aliasStr);
                    }else{
                        s.append(nameStr);
                    }
                    s.append(str[i]);
                    i++;
                }
                source = s.toString();
                i_list++;
            }
            return source;
        }
    }
	
	public static String convertCharacterEncoding(HttpServletRequest request, String fileName) {
	     String agent = request.getHeader("USER-AGENT").toLowerCase();
	     //根据浏览器类型处理文件名称
	     if(agent.indexOf("msie")>-1){
	         try {
				fileName = java.net.URLEncoder.encode(fileName, Constants.CHARSET.toString());
			} catch (UnsupportedEncodingException e) {
				logger.error("设置浏览器下载文件名的编码为utf-8，异常信息：{}", e.getMessage());
			}
	     }else{
	         try {
				fileName = new String(fileName.getBytes(Constants.CHARSET), "ISO8859-1");
			} catch (UnsupportedEncodingException e) {
				logger.error("设置浏览器下载文件名的编码为ISO8859-1，异常信息：{}", e.getMessage());
			}
	     }	
	     return fileName;
	}
	
}