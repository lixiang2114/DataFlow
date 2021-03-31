package com.github.lixiang2114.flow.comps;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.plugins.face.Procedure;

/**
 * @author Lixiang
 * @description 插件
 */
public class Plugin extends Comp{
	/**
	 * 默认实例
	 */
	public Object instance;
	
	/**
	 * 插件类型
	 */
	public PluginType pluginType;
	
	/**
	 * 默认引导类
	 */
	public Class<?> bootClass;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(Plugin.class);
	
	/**
	 * 引导类方法字典表
	 */
	private ConcurrentHashMap<String,Method> methodDict=new ConcurrentHashMap<String,Method>();

	public Plugin(){}
	
	/**
	 * 插件实例构造器
	 * @param pluginName 插件名称
	 * @param bootType 插件引导类型
	 * @param classLoader 插件类装载器
	 */
	public Plugin(String pluginName,String bootType,ClassLoader classLoader){
		super(CompType.plugins,pluginName);
		
		try {
			this.bootClass=Class.forName(bootType, true, classLoader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		try {
			this.instance=this.bootClass.newInstance();
			Method[] allMethods=bootClass.getMethods();
			for(Method method:allMethods){
				if(!Procedure.class.isAssignableFrom(method.getDeclaringClass())) continue;
				methodDict.put(method.getName(), method);
			}
		} catch (Exception e) {
			log.error("bootClass oparation when plugin initing occur error:",e);
			throw new RuntimeException(e);
		}
	}
	
	public String toString() {
		return super.compName;
	}

	/**
	 * 获取插件中的接口方法
	 * @param methodName 接口方法名
	 * @return 接口方法
	 */
	public Method getFace(String method){
		return methodDict.get(method);
	}
	
	/**
	 * 调用插件中的暴露的接口
	 * @param method 接口方法
	 * @param params 接口参数表
	 * @return 调用结果
	 * @throws Exception
	 */
	public Object callFace(String methodName,Object... params) throws Exception{
		Method method=methodDict.get(methodName);
		if(null==method) {
			log.error("can not found method name: {} from {}",methodName,bootClass.getName());
			return null;
		}
		return method.invoke(instance, params);
	}

	/**
	 * 绑定插件类型
	 * @param pluginType 插件类型
	 * @return 插件
	 */
	public Plugin bindPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
		if(Context.checkPluginFace && !pluginType.faceType.isAssignableFrom(bootClass)) {
			log.error("plugin bootClass: {} does not implement the interface: {}",bootClass.getName(),pluginType.faceType.getName());
		}
		return this;
	}
}
