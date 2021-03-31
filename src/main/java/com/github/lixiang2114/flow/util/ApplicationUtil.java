package com.github.lixiang2114.flow.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * @author Lixiang
 * SpringBoot应用工具
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ApplicationUtil {
	/**
	 * SpringBoot应用命令行启动参数
	 */
	private static String[] cmdLineArgs;
	
	/**
     * Bean标识模式
     */
    public enum BeanIdMode{BEAN_NAME,BEAN_TYPE}
	
	/**
	 * SpringBoot应用上下文环境
	 */
	private static SpringApplication springApplication;
	
	/**
	 * SpringBoot环境
	 */
	private static StandardEnvironment environment;
	
	/**
	 * SpringBoot生命周期字典
	 */
	private static ConcurrentHashMap<String,Object> lifecycleDict;
	
    /**
     * 工程类路径配置
     */
    private static final String PROJECT_CLASS_PATH  = "java.class.path";
    
    /**
     * Email地址分隔符正则式
     */
    private static final Pattern EMAIL_SEPARATOR_REGEX=Pattern.compile("@");
    
    /**
     * WebSocket容器配置
     */
    private static final String WS_SERVER_CONTANER="javax.websocket.server.ServerContainer";
    
    /**
	 * SpringBoot配置文件后缀键(默认支持Nacos)
	 */
	private static final String[] CONFIG_SUFFIX={".yml]",".yaml]",".properties]","DEFAULT_GROUP"};
    
    /**
     * 类路径分隔符正则式
     */
    private static final Pattern CLASSPATH_SEPARATOR_REGEX=Pattern.compile(File.pathSeparator);
    
    /**
     * Spring应用上下文字典
     */
    private static final LinkedHashMap<Class<?>,GenericApplicationContext> CONTEXT_DICT=new LinkedHashMap<Class<?>,GenericApplicationContext>();
    
	/**
	 * 获取Spring上下文字典
	 * @return
	 */
	public static LinkedHashMap<Class<?>, GenericApplicationContext> getContextDict() {
		return new LinkedHashMap<Class<?>,GenericApplicationContext>(CONTEXT_DICT);
	}

	/**
	 * 获取命令行参数列表
	 * @return
	 */
	public static String[] getCmdLineArgs() {
		return cmdLineArgs;
	}
	
	/**
	 * 获取Web应用服务器地址
	 * @return IP地址
	 */
	public static String getServerIP() {
		try {
			return IPUtil.getLinuxServerIp();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return "127.0.0.1";
	}
	
	/**
	 * 获取Web应用服务器端口
	 * @return 端口
	 */
	public static Integer getServerPort() {
		return getProperty("server.port",Integer.class);
	}
	
	/**
	 * 获取SpringBoot应用名
	 * @return 端口
	 */
	public static String getApplicationName() {
		return getProperty("spring.application.name");
	}
	
	/**
	 * 获取系统部署环境
	 * @return dev/test/pre/prod..etc
	 */
	public static String getDeployEnvironment(){
		String[] profiles=getEnvironment().getActiveProfiles();
		if(null==profiles || 0==profiles.length) return null;
    	return profiles[0];
	}
	
	/**
	 * 获取Web应用上下文路径(默认空串)
	 * @return 上下文路径
	 */
	public static String getContextPath() {
		String appName=null;
		StandardEnvironment environment=getEnvironment();
		if(null!=environment) {
			appName=environment.getProperty("server.servlet.contextPath");
			if(null==appName || appName.trim().isEmpty()) appName=environment.getProperty("server.servlet.context-path");
			if(null==appName || appName.trim().isEmpty()) appName=environment.getProperty("server.servlet.context_path");
		}
		if(null!=appName && !appName.trim().isEmpty()) return appName;
		
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null!=applicationContext) appName=applicationContext.getApplicationName();
		if(null==appName || appName.trim().isEmpty()) return "";
		return appName;
	}

	/**
	 * 获取Servlet配置
	 * @return Servlet配置
	 */
	public static ServletConfig getServletConfig() {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==applicationContext) return null;
		if(!(applicationContext instanceof GenericWebApplicationContext)) return null;
		return ((GenericWebApplicationContext)applicationContext).getServletConfig();
	}

	/**
	 * 获取Servlet上下文
	 * @return Servlet上下文
	 */
	public static ServletContext getServletContext() {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==applicationContext) return null;
		if(!(applicationContext instanceof GenericWebApplicationContext)) return null;
		return ((GenericWebApplicationContext)applicationContext).getServletContext();
	}
	
	/**
	 * 获取底层服务器实现的WebSocket容器
	 * @return WebSocket容器
	 */
	public static ServerContainer getWebSocketContainer() {
		ServletContext servletContext=ApplicationUtil.getServletContext();
		if(null==servletContext) return null;
		
		Object object=servletContext.getAttribute(WS_SERVER_CONTANER);
		if(null==object || !ServerContainer.class.isInstance(object)) return null;
		
		return ServerContainer.class.cast(object);
	}
	
	/**
	 * 绑定EndPoint
	 * @param path ForExample:"/ws/chat"
	 * @param endpoint 继承至javax.websocket.Endpoint类的子类
	 * @throws DeploymentException
	 */
	public static void bindEndpoint(String path,Class<?> endpoint) throws DeploymentException{
		ServerContainer container=getWebSocketContainer();
		if(null==container) throw new RuntimeException("Not Found WebSocket Container");
		container.addEndpoint(ServerEndpointConfig.Builder.create(endpoint, path).build());
	}
	
	/**
	 * 绑定EndPoint
	 * @param endpoint 使用注解ServerEndpoint(value = "/ws/chat")标注的类
	 * @throws DeploymentException
	 */
	public static void bindEndpoint(Class<?> endpoint) throws DeploymentException{
		ServerContainer container=getWebSocketContainer();
		if(null==container) throw new RuntimeException("Not Found WebSocket Container");
		container.addEndpoint(endpoint);
	}
	
	/**
	 * 递归扫描并绑定指定基础包下的所有EndPoint
	 * @param basePackage WebSocket-EndPoint基础包
	 * @param endpointMaps 基类javax.websocket.Endpoint与API之间的映射字典
	 * @description 
	 * 注解映射ServerEndpoint(value = "/ws/chat")的优先级高于继承javax.websocket.Endpoint基类的优先级
	 * 任何不完整的注解映射都将导致扫描回退到继承javax.websocket.Endpoint基类并应用映射字典来进行处理,若均未完善则忽略接口映射类
	 * @throws Exception
	 */
	public static void scanEndpointPackages(String basePackage,Map<Class<? extends Endpoint>,String>... endpointMaps) throws Exception{
		if(null==basePackage || 0==basePackage.trim().length()) return;
		ServerContainer container=getWebSocketContainer();
		if(null==container) throw new RuntimeException("Not Found WebSocket Container");
		Map<Class<? extends Endpoint>,String> endpointMap=null==endpointMaps || 0==endpointMaps.length?Collections.EMPTY_MAP:null==endpointMaps[0]?Collections.EMPTY_MAP:endpointMaps[0];
		
		ArrayList<Class<?>> classList=getClasses(basePackage);
		for(Class<?> classType:classList) {
			Set<Annotation> annotations=CommonUtil.getTypeAnnotations(CommonUtil.ClassType.ALL, classType);
			if(null!=annotations) {
				ServerEndpoint endpointAnnotation=null;
				for(Annotation annotation:annotations) if(ServerEndpoint.class.isInstance(annotation)) endpointAnnotation=(ServerEndpoint)annotation;
				if(null!=endpointAnnotation && null!=endpointAnnotation.value() && 0!=endpointAnnotation.value().trim().length()) {
					container.addEndpoint(classType);
					continue;
				}
			}
			
			if(!Endpoint.class.isAssignableFrom(classType)) continue;
			String path=endpointMap.get(classType);
			if(null==path || 0==path.trim().length()) continue;
			container.addEndpoint(ServerEndpointConfig.Builder.create(classType, path).build());
		}
	}
	
	/**
	 * 动态获取SpringBoot配置参数字典
	 * @param suffixStr 配置文件后缀
	 * @return 配置字典
	 * @description
	 * SpringBoot环境配置参数必须是动态获取,不能静态初始化,
	 * 因为在SpringCloud应用中可以通过配置管理中心动态更新环境配置参数值
	 */
	public static ConcurrentHashMap<String,Object> getPropertiesConfig(String... suffixStr){
		StandardEnvironment environment=getEnvironment();
		ConcurrentHashMap<String,Object> properties=new ConcurrentHashMap<String,Object>();
		String[] suffixStrs=(null==suffixStr||0==suffixStr.length)?CONFIG_SUFFIX:suffixStr;
		Set<String> configSuffixs=new HashSet<String>(Arrays.asList(suffixStrs));
		environment.getPropertySources().stream().filter(propertySource->{
			for(String e:configSuffixs)if(propertySource.getName().endsWith(e))return true;
			return false;
		}).forEach(propertySource->{
    		Object source=propertySource.getSource();
    		if(!(source instanceof Map))return;
    		((Map<Object,Object>)source).forEach((k,v)->{
    			Object strVal=(v instanceof String)?(String)v:((OriginTrackedValue)v).getValue();
    			if(null!=strVal) strVal=resolvePlaceHolder(strVal.toString());
    			properties.put((String)k,strVal);
    		});
    	});
		return properties;
	}
	
	/**
	 * 递归解析配置占位符
	 * @param placeHolder 占位符 
	 * @return 字串值
	 */
	public static String resolvePlaceHolder(String placeHolder) {
		if(null==placeHolder) return null; 
		placeHolder=placeHolder.trim();
		if(placeHolder.isEmpty()) return "";
		StandardEnvironment environment=getEnvironment();
		if(!placeHolder.startsWith(SystemPropertyUtils.PLACEHOLDER_PREFIX) || !placeHolder.endsWith(SystemPropertyUtils.PLACEHOLDER_SUFFIX)) {
			return placeHolder;
		}
		String placeHolderKey=placeHolder.substring(2, placeHolder.length()-1);
		String valueHolder=environment.getProperty(placeHolderKey);
		if(null==valueHolder||valueHolder.isEmpty()) return placeHolder;
		return resolvePlaceHolder(valueHolder);
	}
	
	public static ConcurrentHashMap<String, Object> getLifecycleDict() {
		return lifecycleDict;
	}
	
	/**
	 * 获取SpringBoot原始配置环境
	 * @return SpringBoot配置环境
	 */
	public static StandardEnvironment getRawEnvironment() {
		return environment;
	}

	/**
	 * 获取SpringBoot配置环境
	 * @param levels 上下文层级(从0开始计算)
	 * @return SpringBoot配置环境
	 */
	public static StandardEnvironment getEnvironment(Integer... levels) {
		GenericApplicationContext applicationContext=getApplicationContext(levels);
		if(null==applicationContext) return environment;
		return (StandardEnvironment)applicationContext.getEnvironment();
	}

	/**
	 * 获取SpringBoot应用容器
	 * @return SpringBoot应用容器
	 */
	public static SpringApplication getSpringApplication() {
		return springApplication;
	}

	/**
	 * 获取Spring之BeanFactory
	 * @param levels 上下文层级
	 * @return BeanFactory
	 */
	public static DefaultListableBeanFactory getBeanFactory(Integer... levels) {
		GenericApplicationContext applicationContext=getApplicationContext(levels);
		return null==applicationContext?null:applicationContext.getDefaultListableBeanFactory();
	}
	
	/**
	 * 获取Spring应用上下文
	 * @param levels 上下文层级(从0开始计算)
	 * @return ApplicationContext
	 */
	public static GenericApplicationContext getApplicationContext(Integer... levels) {
		if(0==CONTEXT_DICT.size()) return null;
		GenericApplicationContext applicationContext=null;
		int index=null==levels||0==levels.length?CONTEXT_DICT.size()-1:levels[0].intValue();
		Iterator<Map.Entry<Class<?>,GenericApplicationContext>> contextIterator=CONTEXT_DICT.entrySet().iterator();
		for(int i=0;i<=index&&contextIterator.hasNext();applicationContext=contextIterator.next().getValue(),i++) ;
		return applicationContext;
	}

	/**
	 * 获取操作系统属性字典
	 * @return 字典类型
	 */
	public static Map<String,Object> getSystemProperties() {
		return getEnvironment().getSystemEnvironment();
	}
	
	/**
	 * 获取虚拟机系统属性字典
	 * @return 字典类型
	 */
	public static Map<String,Object> getJVMProperties() {
		return getEnvironment().getSystemProperties();
	}
	
	/**
	 * 设置生命周期全局域缓存
	 * @param key 键
	 * @param value 值
	 */
	public static void setValue(String key,Object value){
		lifecycleDict.put(key, value);
	}
	
	/**
	 * 获取生命周期全局域缓存
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static Object getValue(String key,Object... defaultValue){
		return getValue(key,Object.class,defaultValue);
	}
	
	/**
	 * 获取生命周期全局域缓存
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getValue(String key,Class<R> returnType,R... defaultValue){
		return getValue(key,null,returnType,defaultValue);
	}
	
	/**
	 * 获取生命周期全局域缓存
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getValue(String key,String defaultKey,Class<R> returnType,R... defaultValue){
		R defaultVal=(null==defaultValue||0==defaultValue.length)?null:defaultValue[0];
		Object value=lifecycleDict.get(key);
		if(null==value && null!=defaultKey) value=lifecycleDict.get(defaultKey);
		if(null==value) return defaultVal;
		return returnType.cast(value);
	}
	
	/**
	 * 获取Spring上下文中指定Bean名称对应的Bean对象
	 * @param beanName BeanId
	 * @return 对象类型
	 */
	public static Object getBean(String beanName) {
		if(null==beanName||beanName.trim().isEmpty()) return null;
		return getBean(beanName,Object.class);
	}
	
	/**
	 * 获取Spring上下文中兼容到指定类型的唯一Bean对象
	 * @param beanType Bean类型
	 * @return 泛化类型
	 * @description 
	 * 兼容Bean不唯一或不存在则返回null
	 */
	public static <R> R getBean(Class<R> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanType||null==applicationContext) return null;
		try{
			return applicationContext.getBean(beanType);
		}catch(BeansException e){
			return null;
		}
	}
	
	/**
	 * 获取Spring上下文中给定Bean类型简单名对应的Bean对象
	 * @param beanType Bean类型
	 * @return 泛化类型
	 */
	public static <R> R getBeanOfName(Class<R> beanType) {
		if(null==beanType) return null;
		String beanName=CommonUtil.lowerFirstChar(beanType.getSimpleName());
		return getBean(beanName,beanType);
	}
	
	/**
	 * 获取Spring上下文中兼容到指定类型的随机Bean对象
	 * @param beanType Bean类型
	 * @return 泛化类型
	 * @description 
	 * 兼容Bean不存在则返回null,兼容Bean有多个则随机返回一个
	 */
	public static <R> R getRandomBean(Class<R> beanType) {
		if(null==beanType) return null;
		Map<String, R> beanDict=getBeansOfType(beanType,true,true);
		if(null==beanDict || beanDict.isEmpty()) return null;
		return beanDict.entrySet().iterator().next().getValue();
	}
	
	/**
	 * 获取Spring上下文中的类型兼容Bean字典
	 * @param beanType Bean类型
	 * @return 泛化字典
	 */
	public static <R> Map<String,R> getBeansOfType(Class<R> beanType) {
		if(null==beanType) return null;
		return getBeansOfType(beanType,true,true);
	}
	
	/**
	 * 获取Spring上下文中的类型兼容Bean字典
	 * @param beanType Bean类型
	 * @return 泛化字典
	 */
	public static <R> Map<String,R> getBeansOfType(Class<R> beanType,boolean includeNonSingletons,boolean requireInit) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanType||null==applicationContext) return null;
		return applicationContext.getBeansOfType(beanType,includeNonSingletons,requireInit);
	}
	
	/**
	 * 获取Spring上下文中的Bean对象
	 * @param beanName BeanId
	 * @param beanType Bean类型
	 * @return 泛化类型
	 */
	public static <R> R getBean(String beanName,Class<R> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanName||null==beanType||null==applicationContext||beanName.trim().isEmpty()) return null;
		if(!applicationContext.containsBean(beanName)) return null;
		return applicationContext.getBean(beanName,beanType);
	}
	
	/**
	 * 通过BeanName和beanType在容器中搜索Bean
	 * @param beanName BeanId
	 * @param beanType Bean类型
	 * @return 对象类型
	 */
	public static Object getBeanOfSearch(String beanName,Class<?> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null!=beanName && !beanName.trim().isEmpty() && null!=applicationContext){
			if(applicationContext.containsBean(beanName)) {
				return applicationContext.getBean(beanName);
			}
		}
		if(null==beanType||null==applicationContext) return null;
		Map<String,?> beanDict=applicationContext.getBeansOfType(beanType,true,true);
		if(null==beanDict || beanDict.isEmpty()) return null;
		if(beanDict.size()>1) return beanDict;
		return beanDict.entrySet().iterator().next().getValue();
	}
	
	/**
	 * 获取Spring上下文中的Bean定义
	 * @param beanName BeanId
	 * @return Bean定义对象
	 * @description 从单例对象字典singletonObjects中获取指定Bean名称对应的单例Bean对象
	 */
	public static Object getSingletonBean(String beanName) {
		if(null==beanName||beanName.trim().isEmpty()) return null;
		return getSingletonBean(beanName,Object.class);
	}
	
	/**
	 * 获取Spring上下文中的Bean定义
	 * @param beanName BeanId
	 * @param type Bean类型
	 * @return 泛化类型
	 * @description 从单例对象字典singletonObjects中获取指定Bean名称对应的单例Bean对象
	 */
	public static <R> R getSingletonBean(String beanName,Class<R> type) {
		if(null==beanName||null==type||beanName.trim().isEmpty()) return null;
		DefaultListableBeanFactory beanFactory=getBeanFactory();
		if(!beanFactory.containsSingleton(beanName)) return null;
		return type.cast(beanFactory.getSingleton(beanName));
	}
	
	/**
	 * 获取Spring上下文中的Bean定义
	 * @param beanName BeanId
	 * @return Bean定义对象
	 * @description 从Bean定义字典beanDefinitionMap中获取指定Bean名称对应的Bean定义
	 */
	public static BeanDefinition getBeanDefinition(String beanName) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanName||null==applicationContext||beanName.trim().isEmpty()) return null;
		if(!applicationContext.containsBeanDefinition(beanName)) return null;
		return applicationContext.getBeanDefinition(beanName);
	}
	
	/**
	 * 移除Spring上下文中的Bean定义
	 * @param beanName BeanId
	 * @description 从整个容器中移除指定Bean名称的Bean定义和单例Bean对象
	 */
	public static void removeBean(String beanName) {
		if(null==beanName||beanName.trim().isEmpty()) return;
		DefaultListableBeanFactory beanFactory=getBeanFactory();
		if(beanFactory.containsBeanDefinition(beanName)) {
			beanFactory.removeBeanDefinition(beanName);
		}else{
			beanFactory.destroySingleton(beanName);
		}
	}
	
	/**
	 * 移除Spring上下文中的所有兼容Bean定义
	 * @param beanType 兼容Bean类型
	 * @return 移除的兼容类型Bean数量
	 * @description 从整个容器中移除指定兼容类型的Bean定义和单例Bean对象
	 */
	public static <R> Integer removeBean(Class<R> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanType||null==applicationContext) return null;
		String[] beanNames=applicationContext.getBeanNamesForType(beanType,true,true);
		if(null==beanNames || 0==beanNames.length) return 0;
		for(String beanName:beanNames) removeBean(beanName);
		return beanNames.length;
	}
	
	/**
	 * 检查给定Bean名称对应的Bean是否为单例Bean
	 * @param beanName Bean名称(BeanId)
	 * @return 是否为单例Bean
	 */
	public static Boolean isSingleton(String beanName) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanName||null==applicationContext||beanName.trim().isEmpty()) return null;
		return applicationContext.isSingleton(beanName);
	}
	
	/**
	 * 判断Spring上下文中是否存在指定名称的Bean对象
	 * @param beanType Bean名称
	 * @return 逻辑类型
	 * @description 检查整个容器中是否存在指定Bean名称的Bean对象
	 */
	public static Boolean hasBean(String beanName) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanName||null==applicationContext||beanName.trim().isEmpty()) return null;
		return applicationContext.containsBean(beanName);
	}
	
	/**
	 * 判断Spring上下文中是否存在指定兼容类型的Bean对象
	 * @param beanType Bean类型
	 * @return 逻辑类型
	 * @description 检查整个容器中是否存在指定兼容类型的Bean对象
	 */
	public static <R> Boolean hasBean(Class<R> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanType||null==applicationContext) return null;
		String[] beanNames=applicationContext.getBeanNamesForType(beanType,true,true);
		return null==beanNames||0==beanNames.length?false:true;
	}
	
	/**
	 * 判断Spring上下文中是否存在指定兼容类型的唯一Bean对象
	 * @param beanType Bean类型
	 * @return 逻辑类型
	 * @description 检查整个容器中是否存在指定兼容类型的唯一Bean对象
	 */
	public static <R> Boolean hasUniqueBean(Class<R> beanType) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanType||null==applicationContext) return null;
		String[] beanNames=applicationContext.getBeanNamesForType(beanType,true,true);
		return null==beanNames||0==beanNames.length?false:1<beanNames.length?false:true;
	}
	
	/**
	 * 判断Spring上下文中是否存在指定名称的单例Bean对象
	 * @param beanType Bean名称
	 * @return 逻辑类型
	 * @description 检查单例对象字典singletonObjects中是否存在指定的名称的单例Bean对象
	 */
	public static Boolean hasSingletonBean(String beanName) {
		if(null==beanName||beanName.trim().isEmpty()) return null;
		DefaultListableBeanFactory beanFactory=getBeanFactory();
		return beanFactory.containsSingleton(beanName);
	}
	
	/**
	 * 判断Spring上下文中是否存在指定名称的Bean定义
	 * @param beanType Bean名称
	 * @return 逻辑类型
	 * @description 检查Bean定义字典beanDefinitionMap中是否存在指定的名称的Bean定义
	 */
	public static Boolean hasBeanDefinition(String beanName) {
		GenericApplicationContext applicationContext=getApplicationContext();
		if(null==beanName||null==applicationContext||beanName.trim().isEmpty()) return null;
		return applicationContext.containsBeanDefinition(beanName);
	}
	
	/**
	 * 环境中是否配置指定的属性
	 * @param key 键
	 * @return 逻辑类型
	 */
	public static Boolean hasProperty(String key) {
		return getEnvironment().containsProperty(key);
	}
	
	/**
	 * 环境中是否配置指定的属性前缀
	 * @param prefix 键前缀
	 * @return 是否存在指定的属性前缀
	 */
	public static Boolean hasPropertyPrefix(String prefix) {
		for (PropertySource<?> propertySource : getEnvironment().getPropertySources()) {
			Object source=propertySource.getSource();
			if(null==source || !(source instanceof Map)) continue;
			for(Object key:((Map<Object,Object>)source).keySet()) if(key.toString().startsWith(prefix)) return true;
		}
		return false;
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @return 字串类型
	 */
	public static String getProperty(String key) {
		return getProperty(key,(String)null,(String[])null);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 字串类型
	 */
	public static String getProperty(String key,String defaultValue) {
		return getProperty(key,(String)null,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param defaultValue 默认值
	 * @return 字串类型
	 */
	public static String getProperty(String key,String defaultKey,String... defaultValue) {
		return getProperty(key,defaultKey,String.class,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 对象类型
	 */
	public static Object getProperty(String key,Object... defaultValue) {
		return getProperty(key,Object.class,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param defaultValue 默认值
	 * @return 对象类型
	 */
	public static Object getProperty(String key,String defaultKey,Object... defaultValue) {
		return getProperty(key,defaultKey,Object.class,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getProperty(String key,Class<R> returnType,R... defaultValue) {
		return getProperty(key,(String)null,returnType,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getProperty(String key,String defaultKey,Class<R> returnType,R... defaultValue) {
		R defaultVal=(null==defaultValue||0==defaultValue.length)?null:defaultValue[0];
		
		StandardEnvironment env=getEnvironment();
		String value=env.getProperty(key);
		
		if(null==value && null!=defaultKey) value=env.getProperty(defaultKey);
		if(null==value) return defaultVal;
		String stringValue=value.toString().trim();
		
		try {
			if(Number.class.isAssignableFrom(returnType)){
				return returnType.getConstructor(String.class).newInstance(stringValue);
			}else if(Date.class.isAssignableFrom(returnType)){
				return returnType.getConstructor(long.class).newInstance(DateUtil.stringToMillSeconds(stringValue));
			}else if(Boolean.class==returnType){
				return (R)Boolean.valueOf(stringValue);
			}else if(Character.class==returnType){
				return (R)Character.valueOf(stringValue.charAt(0));
			}else{
				return (R)stringValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取环境配置
	 * @param prefix 配置前缀
	 * @param defaultValue 默认值
	 * @return 字典类型
	 * @throws Exception
	 */
	public static Map<String,Object> getProperties(String prefix,Map<String,Object>... defaultValue) throws Exception {
		return getProperties(prefix,null,Map.class,null,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param prefix 配置前缀
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 实体类型或字典类型
	 * @throws Exception
	 */
	public static <R> R getProperties(String prefix,Class<R> returnType,R... defaultValue) throws Exception {
		return getProperties(prefix,null,returnType,null,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param prefix 配置前缀
	 * @param returnType 返回类型
	 * @param valueType 字典值类型
	 * @param defaultValue 默认值
	 * @return 实体类型或字典类型
	 * @throws Exception
	 */
	public static <R,E> R getProperties(String prefix,Class<R> returnType,Class<E> valueType,R... defaultValue) throws Exception {
		return getProperties(prefix,null,returnType,valueType,defaultValue);
	}
	
	/**
	 * 获取环境配置
	 * @param prefix 配置前缀
	 * @param defaultPrefix 默认配置前缀
	 * @param returnType 返回类型
	 * @param valueType 字典值类型
	 * @param defaultValue 默认值
	 * @return 实体类型或字典类型
	 * @throws Exception
	 */
	public static <R,E> R getProperties(String prefix,String defaultPrefix,Class<R> returnType,Class<E> valueType,R... defaultValue) throws Exception {
		Map<String,Object> dict=new HashMap<String,Object>();
		R defaultVal=(null==defaultValue||0==defaultValue.length)?null:defaultValue[0];
		
		//获取环境配置
		Map<String,Object> properties=getPropertiesConfig();
		
		//过滤出前缀配置组
		properties.entrySet().stream().filter(entry->entry.getKey().startsWith(prefix)).forEach(entry->{
			String attrName=entry.getKey().substring(prefix.length());
			if(!prefix.endsWith("."))attrName=attrName.substring(attrName.indexOf(".")+1);
			dict.put(attrName, entry.getValue());
		});
		
		if(0==dict.size() && null!=defaultPrefix){
			properties.entrySet().stream().filter(entry->entry.getKey().startsWith(defaultPrefix)).forEach(entry->{
				String attrName=entry.getKey().substring(defaultPrefix.length());
				if(!defaultPrefix.endsWith("."))attrName=attrName.substring(attrName.indexOf(".")+1);
				dict.put(attrName, entry.getValue());
			});
		}
		
		if(0==dict.size()) return defaultVal;
		
		//按实体类型解析
		if(!Map.class.isAssignableFrom(returnType)){
			R r=returnType.newInstance();
			dict.forEach((key,value)->recursionInjection(key,value,r));
			return r;
		}
		
		//按普通Map接口类型解析
		if(null==valueType || CommonUtil.isSimpleType(valueType)){
			if(returnType.isInterface())return returnType.cast(dict);
			R r=returnType.newInstance();
			Method method=CommonUtil.findMethod(returnType,"putAll",Map.class);
			method.invoke(r, dict);
			return r;
		}
		
		//按嵌套实体的Map接口类型解析
		Map<String,E> returnMap=new HashMap<String,E>();
		dict.forEach((key,value)->{
			String[] attrs=CommonUtil.DOT_REGEX.split(key);
			if(attrs.length!=2)return;
			String mapKey=CommonUtil.dashToHump(attrs[0]);
			String attrName=CommonUtil.dashToHump(attrs[1]);
			E e=returnMap.get(mapKey);
			try {
				if(null==e)returnMap.put(mapKey, e=valueType.newInstance());
				Field field=CommonUtil.findField(valueType, attrName);
				field.set(e, CommonUtil.transferType(value,field.getType()));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		return returnType.cast(returnMap);
	}
	
	/**
	 * 设置Web应用域缓存
	 * @param key 键
	 * @param value 值
	 */
	public static void setAttribute(String key,Object value){
		getServletContext().setAttribute(key, value);
	}
	
	/**
	 * 获取Web应用域缓存
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 对象类型
	 */
	public static Object getAttribute(String key,Object... defaultValue){
		return getAttribute(key,Object.class,defaultValue);
	}
	
	/**
	 * 获取Web应用域缓存
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getAttribute(String key,Class<R> returnType,R... defaultValue){
		return getAttribute(key,null,returnType,defaultValue);
	}
	
	/**
	 * 获取Web应用域缓存
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R getAttribute(String key,String defaultKey,Class<R> returnType,R... defaultValue){
		R defaultVal=(null==defaultValue||0==defaultValue.length)?null:defaultValue[0];
		ServletContext servletContext=getServletContext();
		Object value=servletContext.getAttribute(key);
		if(null==value && null!=defaultKey) value=servletContext.getAttribute(defaultKey);
		if(null==value) return defaultVal;
		return returnType.cast(value);
	}
	
	/**
	 * 获取当前位置上下文
	 * @param levels 线程栈层级
	 * @return 位置上下文字典
	 */
	public static StackTraceElement getPositionContext(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		return Thread.currentThread().getStackTrace()[level];
	}
	
	/**
	 * 获取当前文件名
	 * @return 当前源文件名
	 */
	public static String getCurrentFileName(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		return Thread.currentThread().getStackTrace()[level].getFileName();
	}
	
	/**
	 * 获取当前文件行号
	 * @return 当前文件行号
	 */
	public static Integer getCurrentLineNumber(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		return Thread.currentThread().getStackTrace()[level].getLineNumber();
	}
	
	/**
	 * 获取当前类名称
	 * @return 当前类对象
	 * @throws ClassNotFoundException 
	 */
	public static Class<?> getCurrentClass(Integer... levels) throws ClassNotFoundException{
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		String className=Thread.currentThread().getStackTrace()[level].getClassName();
		return Class.forName(className);
	}
	
	/**
	 * 获取当前类名称
	 * @return 当前类名
	 */
	public static String getCurrentClassName(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		return Thread.currentThread().getStackTrace()[level].getClassName();
	}
	
	/**
	 * 获取当前方法
	 * @return 当前方法对象
	 * @throws ClassNotFoundException
	 */
	public static Method getCurrentMethod(Integer... levels) throws ClassNotFoundException{
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		StackTraceElement threadStack=Thread.currentThread().getStackTrace()[level];
		String methodName=threadStack.getMethodName();
		Method[] methods=Class.forName(threadStack.getClassName()).getDeclaredMethods();
		for(Method method:methods) if(methodName.equals(method.getName())) return method;
		return null;
	}
	
	/**
	 * 获取当前方法名称
	 * @return 当前方法名
	 */
	public static String getCurrentMethodName(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		return Thread.currentThread().getStackTrace()[level].getMethodName();
	}
	
	/**
	 * 获取当前类路径
	 * 返回此方法调用方的类路径目录
	 * @return 字串类型
	 */
	public static String getCurrentClassPath(Integer... levels){
		Integer level=null==levels||0==levels.length?2:null==levels[0]||levels[0]<2?2:levels[0];
		String callClassName=Thread.currentThread().getStackTrace()[level].getClassName();
		String callPkgPath=callClassName.substring(0, callClassName.lastIndexOf('.')).replace('.', File.separatorChar);
		return new StringBuilder(getClassPathRoot()).append(File.separatorChar).append(callPkgPath).toString();
	}
	
	/**
	 * 获取参数包对应的类路径目录
	 * @param packagePath 包路径
	 * @return 字串类型
	 */
	public static String getPackageClassPath(String packagePath){
		String packageDir=packagePath.replace('.', File.separatorChar);
		return new StringBuilder(getClassPathRoot()).append(File.separatorChar).append(packageDir).toString();
	}
	
	/**
	 * 获取工程类路径根目录
	 * @return 字串类型
	 */
	public static String getClassPathRoot(){
		try {
			return new File(ApplicationUtil.class.getResource("/").toURI()).getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getClassPathList().get(0);
	}
	
	/**
	 * 获取类路径目录列表	
	 * @return 路径列表
	 */
	public static List<String> getClassPathList(){
		String classpaths=getJVMProperties().get(PROJECT_CLASS_PATH).toString();
		return Arrays.asList(CLASSPATH_SEPARATOR_REGEX.split(classpaths));
	}
	
	/**
	 * 获取工程脚本目录
	 * @param isDecode 是否需要中文解码
	 * @description 按常规服务器目录编排获取
	 * @return 工程脚本目录
	 */
	public static File getBinPath(Boolean... isDecode) {
		return new File(getProjectPath(isDecode),"bin");
	}
	
	/**
	 * 获取工程配置目录
	 * @param isDecode 是否需要中文解码
	 * @description 按常规服务器目录编排获取
	 * @return 工程配置目录
	 */
	public static File getConfPath(Boolean... isDecode) {
		return new File(getProjectPath(isDecode),"conf");
	}
	
	/**
	 * 获取工程目录
	 * @param isDecode 是否需要中文解码
	 * @description 按常规服务器目录编排获取
	 * @return 工程目录
	 */
	public static File getProjectPath(Boolean... isDecode) {
		return getLibPath(isDecode).getParentFile();
	}
	
	/**
	 * 获取工程库目录
	 * @param isDecode 是否需要中文解码
	 * @description 按常规服务器目录编排获取
	 * @return 工程库目录
	 */
	public static File getLibPath(Boolean... isDecode) {
		return getJarPkgFile(isDecode).getParentFile();
	}
	
	/**
	 * 获取当前JAR包绝对路径文件
	 * @param isDecode 是否需要中文解码
	 * @return 绝对路径文件
	 */
	public static File getJarPkgFile(Boolean... isDecode) {
		String jarPkgPath=getJarPkgPath(isDecode);
		return new File(jarPkgPath);
	}
	
	/**
	 * 获取当前JAR包绝对路径
	 * @param isDecode 是否需要中文解码
	 * @return JAR包绝对路径
	 */
	public static String getJarPkgPath(Boolean... isDecode) {
		boolean decode=null==isDecode || 0==isDecode.length || null==isDecode[0]?false:isDecode[0];
		String currentClassPath = ApplicationUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		try {
			if(decode) currentClassPath= java.net.URLDecoder.decode(currentClassPath,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		String jarUriPath=currentClassPath;
		int firstSep=currentClassPath.indexOf("!");
		if(-1!=firstSep) jarUriPath=currentClassPath.substring(0, firstSep);
		return new File(jarUriPath).getAbsolutePath();
	}
	
	/**
	 * 递归获取参数包路径下的所有类
	 * @param packagePath 包路径(使用'.'分隔)
	 * @return 类表
	 * @throws ClassNotFoundException
	 */
	public static ArrayList<Class<?>> getClasses(String packagePath) throws ClassNotFoundException{
		if(null==packagePath || packagePath.trim().isEmpty()) return null;
		String classPath=new StringBuilder(getClassPathRoot()).append(File.separatorChar).toString();
		return getClasses(classPath,new File(new StringBuilder(classPath).append(packagePath.replace('.', File.separatorChar)).toString()));
	}
	
	/**
	 * 递归获取指定目录下的所有类
	 * @param classPath 类路径目录(使用'/'分隔并以'/'结尾)
	 * @param directory 目录绝对路径
	 * @return 类表
	 * @throws ClassNotFoundException
	 */
	public static ArrayList<Class<?>> getClasses(String classPath,File directory) throws ClassNotFoundException{
		if(null==classPath || null==directory || 0==classPath.trim().length() || directory.isFile()) return null;
		ArrayList<Class<?>> list=new ArrayList<Class<?>>();
		for(File file:directory.listFiles()) {
			if(file.isFile()){
				String fullPath=file.getAbsolutePath();
				String packgeName=fullPath.substring(classPath.trim().length(),fullPath.length()-6).replace(File.separatorChar, '.');
				list.add(Class.forName(packgeName));
				continue;
			}
			ArrayList<Class<?>> tmpList=getClasses(classPath,file);
			if(null==tmpList || 0==tmpList.size()) continue;
			list.addAll(tmpList);
		}
		return list;
	}
	
	/**
	 * 获取当前Jvm进程ID
	 */
	public static Integer getJvmProcessID(){
		 String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		 if(null==jvmName) return null;
		 jvmName=jvmName.trim();
		 if(0==jvmName.length()) return null;
		 if(-1==jvmName.indexOf("@")) return null;
		 return Integer.parseInt(EMAIL_SEPARATOR_REGEX.split(jvmName)[0]);
	}
	
	/**
	 * 强制杀掉子进程
	 * @param process 子进程
	 */
	public static final Boolean forceKillProcess(Process process) {
		return killProcess(process,true);
	}
	
	/**
	 * 平滑杀掉子进程
	 * @param process 子进程
	 */
	public static final Boolean gracefulKillProcess(Process process) {
		return killProcess(process,false);
	}
	
	/**
	 * 强制杀掉子进程
	 * @param process 子进程ID
	 */
	public static final Boolean forceKillProcess(long processId) {
		return killProcess(processId,true);
	}
	
	/**
	 * 平滑杀掉子进程
	 * @param process 子进程ID
	 */
	public static final Boolean gracefulKillProcess(long processId) {
		return killProcess(processId,false);
	}
	
	/**
	 * 杀掉子进程
	 * @param process 子进程
	 * @param force 是否强制杀掉(true:强制,false:平滑)
	 */
	public static final Boolean killProcess(Process process,boolean force) {
		if(null==process) return null;
		
		Long processId=getProcessID(process);
		if(null==processId) return null;
		
		return killProcess(processId,force);
	}
	
	/**
	 * 杀掉子进程
	 * @param processId 子进程ID
	 * @param force 是否强制杀掉(true:强制,false:平滑)
	 */
	public static final Boolean killProcess(long processId,boolean force) {
		String osName=ManagementFactory.getOperatingSystemMXBean().getName().trim().toLowerCase();
		try {
			if(osName.startsWith("win")) {
				if(force){
					return Runtime.getRuntime().exec("taskkill /T /F /PID "+processId).waitFor(15, TimeUnit.SECONDS);
				}else{
					return Runtime.getRuntime().exec("taskkill /T /PID "+processId).waitFor(15, TimeUnit.SECONDS);
				}
			} else {
				if(force){
					return Runtime.getRuntime().exec("kill -9 "+processId).waitFor(15, TimeUnit.SECONDS);
				}else{
					return Runtime.getRuntime().exec("kill -sigterm "+processId).waitFor(15, TimeUnit.SECONDS);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 获取子进程ID
	 * @param process 子进程
	 * @return 子进程ID
	 */
	public static final Long getProcessID(Process process) {
		if(null==process) return null;
		switch (SourceVersion.latest().toString()) {
			case "RELEASE_0":
			case "RELEASE_1":
			case "RELEASE_2":
			case "RELEASE_3":
			case "RELEASE_4":
			case "RELEASE_5":
			case "RELEASE_6":
			case "RELEASE_7":
			case "RELEASE_8":
				Class<? extends Process> processImpl=process.getClass();
				String processImplName = processImpl.getName();
				try{
					if ("java.lang.UNIXProcess".equals(processImplName)) {
						Field pidField = processImpl.getDeclaredField("pid");
						pidField.setAccessible(true);
						Object value = pidField.get(process);
						if (value instanceof Number) return ((Number)value).longValue();
						return null;
					} else if ("java.lang.Win32Process".equals(processImplName) || "java.lang.ProcessImpl".equals(processImplName)) {
						Field f = process.getClass().getDeclaredField("handle");
						f.setAccessible(true);
						Kernel32 kernel = Kernel32.INSTANCE;
						WinNT.HANDLE handle = new WinNT.HANDLE();
						handle.setPointer(Pointer.createConstant(f.getLong(process)));
						return (long)kernel.GetProcessId(handle);
					}
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			case "RELEASE_9":
				try {
					return ((Number)Process.class.getMethod("getPid").invoke(process)).longValue();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			default:
				try {
					return ((Number)Process.class.getMethod("pid").invoke(process)).longValue();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
		}
	}
	
	/**
	 * 获取Spring切面执行优先级
	 * @param aspectBeanName 切面Bean名称
	 * @param aspectBeanType 切面Bean类型
	 * @return 切面优先级顺序
	 */
	public static Integer getAspectOrder(String aspectBeanName){
		Object aspectBean=ApplicationUtil.getBean(aspectBeanName);
		return Ordered.class.isInstance(aspectBean)?((Ordered)aspectBean).getOrder():null;
	}
	
	/**
	 * 修改Spring事务切面的执行顺序
	 * @param order 切面执行优先级
	 */
	public static void setTransactionAdvisorOrder(Integer order){
		Class<?> advisorType=null;
		try {
			advisorType = ClassLoaderUtil.loadClass("org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		if(null==advisorType) return;
		
		Object advisor=null;
		if(null==(advisor=getBean("org.springframework.transaction.config.internalTransactionAdvisor",advisorType))) return;
		
		try{
			Method setOrder=advisorType.getDeclaredMethod("setOrder", int.class);
			if(null!=order) {
				setOrder.invoke(advisor, order);
			}else{
				setOrder.invoke(advisor, Ordered.HIGHEST_PRECEDENCE);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 注册单例Bean到Spring上下文
	 * @param bean 单例Bean对象
	 */
	public static void registerSingleton(Object bean){
		String simpleName=bean.getClass().getSimpleName();
		String beanName=CommonUtil.lowerFirstChar(simpleName);
		registerSingleton(beanName,bean);
	}
	
	/**
	 * 注册单例Bean到Spring上下文
	 * @param beanName Bean名称(BeanId)
	 * @param bean 单例Bean对象
	 */
	public static void registerSingleton(String beanName,Object bean){
		registerSingleton(beanName,bean,BeanIdMode.BEAN_NAME);
	}
	
	/**
	 * 注册单例Bean到Spring上下文
	 * @param beanName Bean名称(BeanId)
	 * @param bean 单例Bean对象
	 * @param removeNonUnique 重复Bean定义的移除模式
	 * @description 注册单例Bean定义和单例Bean对象
	 */
	public static void registerSingleton(String beanName,Object bean,BeanIdMode removeNonUnique){
		if(null==bean) throw new RuntimeException("bean can not be null...");
		if(null==beanName || beanName.trim().isEmpty()) throw new RuntimeException("beanName can not be empty...");
		registerBeanDefinition(beanName,bean.getClass(),removeNonUnique);
		registerSingletonBean(beanName, bean);
	}
	
	/**
	 * 注册单例Bean到Spring上下文
	 * @param beanName Bean名称(BeanId)
	 * @param bean 单例Bean对象
	 * @description 注册单例Bean对象
	 */
	public static void registerSingletonBean(String beanName,Object bean){
		DefaultListableBeanFactory beanFactory=getBeanFactory();
		if(null==beanFactory) return;
		beanFactory.registerSingleton(beanName, bean);
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanType Bean类型
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(Class<R> beanType){
		registerBeanDefinition(beanType,new HashMap<String,Object>());
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanName Bean名称(beanId)
	 * @param beanType Bean类型
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(String beanName,Class<R> beanType){
		registerBeanDefinition(beanName,beanType,new HashMap<String,Object>());
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanType Bean类型
	 * @param properties Bean属性字典
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(Class<R> beanType,Map<String,Object> properties){
		registerBeanDefinition(beanType,properties,BeanIdMode.BEAN_NAME);
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanName Bean名称(beanId)
	 * @param beanType Bean类型
	 * @param removeNonUnique 重复Bean定义的移除模式
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(String beanName,Class<R> beanType,BeanIdMode removeNonUnique){
		registerBeanDefinition(beanType,beanName,new HashMap<String,Object>(),removeNonUnique);
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanName Bean名称(beanId)
	 * @param beanType Bean类型
	 * @param properties Bean属性字典
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(String beanName,Class<R> beanType,Map<String,Object> properties){
		registerBeanDefinition(beanType,beanName,properties,BeanIdMode.BEAN_NAME);
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanType Bean类型
	 * @param properties Bean属性字典
	 * @param removeNonUnique 重复Bean定义的移除模式
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(Class<R> beanType,Map<String,Object> properties,BeanIdMode removeNonUnique){
		registerBeanDefinition(beanType,null,properties,removeNonUnique);
	}
	
	/**
	 * 注册Bean到Spring上下文
	 * @param beanType Bean类型
	 * @param beanName Bean名称(beanId)
	 * @param properties Bean属性字典
	 * @param removeNonUnique 重复Bean定义的移除模式
	 * @param constructorArgs Bean类构造器参数列表
	 * @return 泛化类型
	 * @description 注册Bean定义
	 */
	public static <R> void registerBeanDefinition(Class<R> beanType,String beanName,Map<String,Object> properties,BeanIdMode removeNonUnique,Object... constructorArgs){
		DefaultListableBeanFactory beanFactory=getBeanFactory();
		if(null==beanFactory) return;
		
		//获取Bean参数定义
		Object scope=null;
		Object lazyInit=null;
		Object initMethod=null;
		Object dependsBeanId=null;
		Object autowireMode=null;
		Object destoryMethod=null;
		if(null!=properties && 0!=properties.size()){
			scope=properties.remove("scope");
			lazyInit=properties.remove("lazyInit");
			initMethod=properties.remove("initMethod");
			dependsBeanId=properties.remove("dependsBeanId");
			autowireMode=properties.remove("autowireMode");
			destoryMethod=properties.remove("destoryMethod");
		}
		
		//设置Bean定义参数
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanType);
		beanDefinitionBuilder.setAutowireMode(null==autowireMode?AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE:(Integer)autowireMode);
		beanDefinitionBuilder.setScope(null==scope?BeanDefinition.SCOPE_SINGLETON:(String)scope);
		beanDefinitionBuilder.setLazyInit(null==lazyInit?true:(Boolean)lazyInit);
		if(null!=dependsBeanId) beanDefinitionBuilder.addDependsOn((String)dependsBeanId);
		if(null!=initMethod) beanDefinitionBuilder.setInitMethodName((String)initMethod);
		if(null!=destoryMethod) beanDefinitionBuilder.setDestroyMethodName((String)destoryMethod);
		
		//添加构造参数值
		if(null!=constructorArgs && 0!=constructorArgs.length){
			for(Object argValue:constructorArgs) beanDefinitionBuilder.addConstructorArgValue(argValue);
		}
		
		//设置Bean属性值
		if(null!=properties)properties.forEach((key,value)->beanDefinitionBuilder.addPropertyValue(key, value));
		
		//设置Bean的名字(BeanId)
		if(null==beanName) beanName=CommonUtil.lowerFirstChar(beanType.getSimpleName());
		
		//移除已经包含此Bean的定义
		if(removeNonUnique==BeanIdMode.BEAN_NAME) {
			removeBean(beanName);
		}else{
			removeBean(beanType);
		}
		
		//注册Bean到容器
		beanFactory.registerBeanDefinition(beanName,beanDefinitionBuilder.getRawBeanDefinition());
	}
	
	/**
	 * 包装实体对象(递归处理对象图结构)
	 * @param key 复合类型Key
	 * @param value 最终设定的简单类型值
	 */
	public static <R> void recursionInjection(String key,Object value,R r){
		String curAttrName=key;
		String nextAttrName=null;
		int index=key.indexOf(".");
		if(-1!=index){
			curAttrName=key.substring(0, index);
			nextAttrName=key.substring(index+1);
		}
		
		String fieldName=CommonUtil.dashToHump(curAttrName);
		Class<? extends Object> returnType=r.getClass();
		try {
			Field field=CommonUtil.findField(returnType, fieldName);
			if(null==field)return;
			Class<?> attrType=field.getType();
			Boolean isSimpleType=CommonUtil.isSimpleType(attrType);
			if(null!=nextAttrName && (isSimpleType || attrType.isArray())) return;
			
			//简单类型设值
			if(isSimpleType){
				field.set(r, CommonUtil.transferType(value,field.getType()));
				return;
			}
			
			//字符数组类型赋值
			if(char[].class==attrType || Character[].class==attrType){
				field.set(r, CommonUtil.transferArray(value.toString().toCharArray(),attrType.getComponentType()));
				return;
			}
			
			//数组类型设值
			if(attrType.isArray()){
				Class<?> eleType=attrType.getComponentType();
				String[] array=CommonUtil.COMMA.split(value.toString());
				Object arrValue=Array.newInstance(eleType, array.length);
				for(int i=0;i<array.length;i++){
					Object transferValue=CommonUtil.transferType(array[i],eleType);
					Array.set(arrValue, i, transferValue);
				}
				field.set(r, arrValue);
				return;
			}
			
			//集合类型设值(集合泛型为简单类型)
			if(Collection.class.isAssignableFrom(attrType)){
				Collection cols=null;
				if(attrType.isInterface()){
	        		cols=List.class.isAssignableFrom(attrType)?new ArrayList():new HashSet();
	        	}else{
	        		try {
	    				cols = (Collection)attrType.newInstance();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	        	}
				
				Class<?>[] genericParams=CommonUtil.getGenericClass(field);
				String[] array=CommonUtil.COMMA.split(value.toString());
				if(null==genericParams||0==genericParams.length){
					cols.add(Arrays.asList(array));
				}else{
					Object newArray=CommonUtil.transferArray(array, genericParams[0]);
					cols.addAll(CommonUtil.arrayToList(newArray));
				}
				field.set(r, cols);
				return;
			}
			
			//字典类型设值(字典泛型为简单类型)
			if(Map.class.isAssignableFrom(attrType)){
				Map map=null;
				if(attrType.isInterface()) {
					map=new HashMap();
				}else{
					try {
						map = (Map)attrType.newInstance();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
				}
				
				Map readMap=CommonUtil.jsonStrToJava(value.toString(), Map.class);
				Class<?>[] genericParams=CommonUtil.getGenericClass(field);
				if(null==genericParams||2!=genericParams.length){
					map.putAll(readMap);
				}else{
					for(Map.Entry entry:(Set<Map.Entry>)readMap.entrySet()) {
						Object attrName=CommonUtil.transferType(entry.getKey(), genericParams[0]);
						Object attrValue=CommonUtil.transferType(entry.getValue(), genericParams[1]);
						map.put(attrName, attrValue);
					}
				}
				field.set(r, map);
				return;
			}
			
			//复合类型设值
			recursionInjection(nextAttrName,value,attrType.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
