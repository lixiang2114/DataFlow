package com.github.lixiang2114.flow.service.impl;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.comps.Plugin;
import com.github.lixiang2114.flow.comps.PluginType;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.util.CommonUtil;

/**
 * @author Lixiang
 * @description 运维管理服务侧基础实现
 */
@SuppressWarnings("unchecked")
public abstract class BaseService {
	/**
	 * 英文逗号正则式
	 */
	public static final Pattern COMMA_REGEX = Pattern.compile(",");
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(ETLServiceImpl.class);
	
	/**
	 * 确保指定的流程已经被装载
	 * @param flowName 流程名称
	 * @return 流程是否被装载
	 */
	public static final boolean ensureFlowExists(String flowName) {
		if(Context.hasFlow(flowName)) return true;
		try {
			Context.loadFlow(flowName);
		} catch (Exception e) {
			log.error("load flow: "+flowName+" occur error",e);
		}
		return Context.hasFlow(flowName);
	}
	
	/**
	 * 获取插件状态(参数信息)
	 * @param flowName 流程名称
	 * @param pluginType 插件类型名
	 * @param params 参数值列表
	 * @return 插件调用结果
	 */
	public static final Object status(String flowName,PluginType pluginType,Object... params) {
		Flow flow=Context.getFlow(flowName);
		Plugin plugin=flow.getPlugin(pluginType);
		try {
			return plugin.callFace("config",params);
		} catch (Exception e) {
			log.error("call config face occur error: ",e);
		}
		return null;
	}

	/**
	 * 通用透传服务接口
	 * @param flowName 流程名称
	 * @param pluginType 插件类型名
	 * @param faceName 插件接口方法名
	 * @param params 参数表(使用英文逗号分隔参数值)
	 * @return 插件调用结果
	 */
	public static final Object throughService(String flowName,PluginType pluginType,String faceName,String params) {
		Flow flow=Context.getFlow(flowName);
		Plugin plugin=flow.getPlugin(pluginType);
		Method method=plugin.getFace(faceName);
		Class<?>[] paramTypes=method.getParameterTypes();
		Object[] faceArgs   =  new Object[paramTypes.length];
		String logInfo="flow: "+flowName+" plugin: "+plugin.compName+" face: "+faceName+" param count is: "+faceArgs.length;
		
		if(0!=faceArgs.length) {
			if(null==params) {
				log.error(logInfo);
				return logInfo;
			}
			
			String trimParam=params.trim();
			if(trimParam.isEmpty()) {
				log.error(logInfo);
				return logInfo;
			}
			
			String[] paramArray=COMMA_REGEX.split(trimParam);
			if(faceArgs.length>paramArray.length) {
				log.error(logInfo);
				return logInfo;
			}
			
			for(int i=0;i<faceArgs.length;faceArgs[i]=CommonUtil.transferType(paramArray[i], paramTypes[i]),i++);
		}
		
		try {
			return method.invoke(plugin.instance, faceArgs);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("call flow face occur error: ",e);
			return "call flow face occur error: "+e.getMessage();
		}
	}
}
