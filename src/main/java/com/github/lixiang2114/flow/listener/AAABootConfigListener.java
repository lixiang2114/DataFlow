package com.github.lixiang2114.flow.listener;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import org.springframework.core.Ordered;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.github.lixiang2114.flow.util.ApplicationUtil;

/**
 * @author Lixiang
 * Bean初始化期间的前导配置监听器
 */
public class AAABootConfigListener implements Ordered{
	/**
	 * Bean配置
	 */
	private static Object beanConfig;
	
	/**
	 * Bean配置键
	 */
	private static final String SPRING_BOOT_BEANCONFIG="spring.boot.config";

	public AAABootConfigListener(){
		callBackAppFace("beforeBeanInstance");
	}
	
	@PostConstruct
	public void init(){
		callBackAppFace("beforeBeanInitialize");
	}
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
	
	/**
	 * 回调应用层接口
	 * @param methodName 回调方法名
	 */
	private static void callBackAppFace(String methodName){
		try {
			if(null==beanConfig){
				String beanConfigStr=ApplicationUtil.getProperty(SPRING_BOOT_BEANCONFIG);
				if(null==beanConfigStr || beanConfigStr.trim().isEmpty()) return;
				Class<?> type=Class.forName(beanConfigStr);
				beanConfig=type.newInstance();
			}
			
			Method targetMethod=null;
			Method[] methods=beanConfig.getClass().getDeclaredMethods();
			for(Method method:methods){
				if(methodName.equals(method.getName())) {
					targetMethod=method;
					break;
				}
			}
			
			if(null==targetMethod || 1!=targetMethod.getParameterCount()) return;
			if(!targetMethod.getParameterTypes()[0].isAssignableFrom(GenericWebApplicationContext.class)) return;
			
			targetMethod.invoke(beanConfig,ApplicationUtil.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
