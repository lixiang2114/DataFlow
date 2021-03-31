package com.github.lixiang2114.flow.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.github.lixiang2114.flow.util.ApplicationUtil;
import com.github.lixiang2114.flow.util.PropertiesReader;

/**
 * @author Lixiang
 * @description SpringBoot生命周期监听器
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ApplicationLifecycleListener extends AbstractApplicationListener{
	/**
	 * 生命周期定义对象
	 */
	private static Object lifecycleDefinition;
	
	/**
	 * 生命周期实现类配置键
	 */
	private static final String SPRING_BOOT_LIFECYCLE="spring.boot.lifecycle";
	
	/**
	 * 生命周期实现类方法字典
	 */
	private static Map<String,Method> lifecycleMethodDict=new HashMap<String,Method>();
	
	/**
	 * 设置应用参数
	 * @param fieldName 字段名
	 * @param fieldValue 字段值
	 */
	private static void setApplicationData(String fieldName,Object fieldValue){
		Class<ApplicationUtil> type=ApplicationUtil.class;
		try {
			Field field=type.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(type, fieldValue);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 记录Spring应用上下文
	 * @param contextType 上下文类型
	 * @param context 上下文对象
	 */
	private static void putApplicationContext(Class<?> contextType,ConfigurableApplicationContext context){
		Class<ApplicationUtil> type=ApplicationUtil.class;
		try {
			Field field=type.getDeclaredField("CONTEXT_DICT");
			field.setAccessible(true);
			LinkedHashMap contextDict=(LinkedHashMap)field.get(type);
			contextDict.put(contextType, context);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化生命周期对象数据
	 * @param lifecycleStrs 生命周期
	 */
	private static void initLifecycleData(String... lifecycleTypeNames){
		String lifecycleTypeName=null;
		if(null==lifecycleTypeNames || 0==lifecycleTypeNames.length){
			lifecycleTypeName=PropertiesReader.getProperty(SPRING_BOOT_LIFECYCLE);
			lifecycleTypeName=null==lifecycleTypeName?"":lifecycleTypeName.trim();
		}else{
			lifecycleTypeName=lifecycleTypeNames[0].trim();
		}
		
		if(lifecycleTypeName.isEmpty()) return;
		
		try {
			Class<?> type = Class.forName(lifecycleTypeName);
			lifecycleDefinition=type.newInstance();
			Method[] methods=type.getDeclaredMethods();
			for(Method method:methods){
				method.setAccessible(true);
				lifecycleMethodDict.put(method.getName(), method);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 回调应用层接口
	 * @param methodName 回调方法名
	 * @param throwable 回调异常对象
	 * @param lifecycleDefinitionStrs 声明周期定义键
	 */
	private static void callBackAppFace(String methodName,Throwable throwable){
		try {
			Method targetMethod=lifecycleMethodDict.get(methodName);
			if(null==targetMethod) return;
			
			int paramCount=targetMethod.getParameterCount();
			if(1==paramCount){
				Class<?> argType=targetMethod.getParameterTypes()[0];
				if(argType.isAssignableFrom(StandardEnvironment.class)){
					targetMethod.invoke(lifecycleDefinition,ApplicationUtil.getEnvironment());
				}else if(argType.isAssignableFrom(GenericWebApplicationContext.class)){
					targetMethod.invoke(lifecycleDefinition,ApplicationUtil.getApplicationContext());
				}
			}else if(2==paramCount){
				Class<?> firstArgType=targetMethod.getParameterTypes()[0];
				Class<?> secondArgType=targetMethod.getParameterTypes()[1];
				if(firstArgType.isAssignableFrom(SpringApplication.class) && secondArgType.isAssignableFrom(String[].class)){
					targetMethod.invoke(lifecycleDefinition,ApplicationUtil.getSpringApplication(),ApplicationUtil.getCmdLineArgs());
				}else if(firstArgType.isAssignableFrom(GenericWebApplicationContext.class) && secondArgType.isAssignableFrom(Throwable.class)){
					targetMethod.invoke(lifecycleDefinition,ApplicationUtil.getApplicationContext(),throwable);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 必须的构造方法(SpringBoot回调时传递SpringApplication上下文和命令行参数表)
	 * @param application SpringApplication上下文
	 * @param args 命令行参数表
	 */
	public ApplicationLifecycleListener(SpringApplication application,String[] args){
		try{
			afterInstanceInit(application,args);
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 引导初始化
	 * @param application SpringBoot应用
	 * @param args 命令行启动参数
	 */
	public void afterInstanceInit(SpringApplication application,String[] args){
		setApplicationData("cmdLineArgs",args);
		setApplicationData("springApplication",application);
		setApplicationData("lifecycleDict",new ConcurrentHashMap<String,Object>());
		initLifecycleData();
		//如果某些Bean需要在所有Bean装载之前完成注册则可以在AAA监听器中完成回调处理
		ApplicationUtil.getSpringApplication().addPrimarySources(Arrays.asList(AAABootConfigListener.class));
		callBackAppFace("afterLifecycleInstance",null);
	}
	
	/**
	 * 在Spring容器初始化开始前(在创建Environment对象之前)调用,用于早期版本的Spring监听器
	 * */
	@Override
	public void starting() {
		callBackAppFace("afterStartingBeforeEnv",null);
	}

	/**
	 * 在Environment对象初始化完成后(在创建Spring上下文容器对象之前)调用一次
	 * */
	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		setApplicationData("environment",environment);
		callBackAppFace("afterEnvBeforeContext",null);
	}

	/**
	 * 在Spring上下文容器对象创建完成后(在实例化和装载Bean到容器之前)调用一次
	 * */
	@Override
	public void contextPrepared(ConfigurableApplicationContext applicationContext) {
		putApplicationContext(applicationContext.getClass(),applicationContext);
		callBackAppFace("afterContextBeforeBean",null);
	}

	/**
	 * 在所有Bean类(包括注解和XML配置)实例化并装载(实例化、解析并进行自动装配)到Spring上下文容器后
	 * (在refreshContext调用之前)调用一次,如果某些Bean需要在所有Bean装载完成后才注册,则可以在此方法中进行
	 * */
	@Override
	public void contextLoaded(ConfigurableApplicationContext applicationContext) {
		putApplicationContext(applicationContext.getClass(),applicationContext);
		callBackAppFace("afterBeanBeforeStart",null);
	}

	/**
	 * 在Spring上下文容器全部初始化完成后(在refreshContext方法调用之后,在ApplicationRunner和CommandLineRunner调用之前)调用一次
	 * */
	@Override
	public void started(ConfigurableApplicationContext applicationContext) {
		callBackAppFace("afterStartBeforeRunner",null);
	}

	/**
	 * 在回调IOC容器中所有的ApplicationRunner和CommandLineRunner之后调用一次
	 * */
	@Override
	public void running(ConfigurableApplicationContext applicationContext) {
		callBackAppFace("afterInitInvokeRunning",null);
	}

	/**
	 * SpringApplicationRunListener类中的其它任何方法调用失败后都会回调此方法
	 * */
	@Override
	public void failed(ConfigurableApplicationContext applicationContext, Throwable throwable) {
		System.out.println("current application context is:");
		System.out.println(applicationContext);
		throwable.printStackTrace();
		callBackAppFace("afterFailured",throwable);
	}
}
