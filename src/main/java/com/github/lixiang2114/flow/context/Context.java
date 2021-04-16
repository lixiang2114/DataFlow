package com.github.lixiang2114.flow.context;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Channel;
import com.github.lixiang2114.flow.comps.CompType;
import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.comps.Plugin;
import com.github.lixiang2114.flow.comps.PluginType;
import com.github.lixiang2114.flow.plugins.face.FilterPlugin;
import com.github.lixiang2114.flow.plugins.face.ManualPlugin;
import com.github.lixiang2114.flow.plugins.face.RealtimePlugin;
import com.github.lixiang2114.flow.plugins.face.SinkPlugin;
import com.github.lixiang2114.flow.plugins.face.SourcePlugin;
import com.github.lixiang2114.flow.plugins.face.TransferPlugin;
import com.github.lixiang2114.flow.thread.GracefulShutdown;
import com.github.lixiang2114.flow.util.ApplicationUtil;
import com.github.lixiang2114.flow.util.ClassLoaderUtil;
import com.github.lixiang2114.flow.util.FileUtil;
import com.github.lixiang2114.flow.util.PropertiesReader;

/**
 * @author Lixiang
 * @description 系统上下文
 */
public class Context {
	/**
	 * 系统Home目录
	 */
	public static File projectFile;
	
	/**
	 * 流程名称列表
	 */
	public static String[] flowList;
	
	/**
	 * 核心线程数量
	 */
	public static Integer coreThreads;
	
	/**
	 * 当服务启动时是否初始化流程
	 */
	public static boolean initOnStart;
	
	/**
	 * 是否已经装载过所有流程
	 */
	public static boolean flowsLoaded;
	
	/**
	 * 类装载器
	 */
	public static ClassLoader classLoader;
	
	/**
	 * 是否需要检查插件实现相应接口
	 */
	public static Boolean checkPluginFace;
	
	/**
	 * 是否启动ETL进程组调度器
	 */
	public static Boolean etlSchedulerStart;
	
	/**
	 * 是否启动TRA进程组调度器
	 */
	public static Boolean traSchedulerStart;
	
	/**
	 * ETL进程组调度时间间隔(单位:毫秒)
	 */
	public static Long etlSchedulerInterval;
	
	/**
	 * TRA进程组调度时间间隔(单位:毫秒)
	 */
	public static Long traSchedulerInterval;
	
	/**
	 * 英文逗号正则式
	 */
	private static final Pattern COMMA_REGEX=Pattern.compile(",");
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(Context.class);
	
	/**
	 * 流程字典
	 */
	private final static ConcurrentHashMap<String,Flow> FLOW_DICT=new ConcurrentHashMap<String,Flow>();
	
	/**
	 * 插件类型到插件接口映射字典
	 */
	public static final ConcurrentHashMap<String,Class<?>> PLUG_TYPE_TO_FACE=new ConcurrentHashMap<String,Class<?>>();
	
	static{
		PLUG_TYPE_TO_FACE.put("sink", SinkPlugin.class);
		PLUG_TYPE_TO_FACE.put("filter", FilterPlugin.class);
		PLUG_TYPE_TO_FACE.put("source", SourcePlugin.class);
		PLUG_TYPE_TO_FACE.put("manual", ManualPlugin.class);
		PLUG_TYPE_TO_FACE.put("transfer", TransferPlugin.class);
		PLUG_TYPE_TO_FACE.put("realtime", RealtimePlugin.class);
	}
	
	/**
	 * 是否已经装载指定名称的流程
	 * @param pluginName 流程名称
	 * @return 流程信息
	 */
	public final static boolean hasFlow(String flowName) {
		return FLOW_DICT.containsKey(flowName);
	}
	
	/**
	 * 根据ETL流程名称获取流程
	 * @param pluginName 流程名称
	 * @return 流程信息
	 */
	public final static Flow getFlow(String flowName) {
		return FLOW_DICT.get(flowName);
	}
	
	/**
	 * 获取流程字典
	 * @return 流程字典
	 */
	public final static ConcurrentHashMap<String,Flow> getFlowDict(){
		return FLOW_DICT;
	}
	
	/**
	 * 收集应用上下文配置参数
	 * @return 应用上下文配置参数
	 */
	public final static String collectContextParams() {
		log.info("dump applcation context params...");
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("projectFile", projectFile);
		map.put("initOnStart", initOnStart);
		map.put("flowList", Arrays.toString(flowList));
		return map.toString();
	}
	
	/**
	 * 装载插件
	 * @param pluginName 插件名称
	 * @param pluginType 插件类型
	 * @param classLoader 类装载器
	 * @return 插件
	 * @throws Exception
	 */
	private final static Plugin loadPlugin(File pluginRuntimePath,PluginType pluginType,ClassLoader classLoader) throws Exception {
		String pluginConfName=pluginRuntimePath.getName()+".properties";
		File pluginConf=new File(pluginRuntimePath,pluginConfName);
		if(!pluginConf.exists()){
			log.error("plugin config is not found for: {}",pluginConfName);
			throw new RuntimeException("plugin config is not found for: "+pluginConfName);
		}
		
		Properties pluginConfig=PropertiesReader.getProperties(pluginConf);
		String classPath=pluginConfig.getProperty("classPath","").trim();
		String bootType=pluginConfig.getProperty("bootType","").trim();
		if(classPath.isEmpty() || bootType.isEmpty()) {
			log.error("attributes for classPath and bootType must be specified for the plugin config: {}",pluginConfName);
			throw new RuntimeException("attributes for classPath and bootType must be specified for the plugin config: "+pluginConfName);
		}
		
		File pluginClassFile=null;
		String[] plugincClassPathList=COMMA_REGEX.split(classPath);
		for(String plugincClassPath:plugincClassPathList){
			if((plugincClassPath=plugincClassPath.trim()).isEmpty()) continue;
			pluginClassFile=new File(pluginRuntimePath,plugincClassPath);
			if(!pluginClassFile.exists()) {
				log.warn("specify classPath parts is not exists for: {}",pluginClassFile.getAbsolutePath());
				continue;
			}
			ClassLoaderUtil.addCurrentClassPath(pluginClassFile,true,classLoader);
		}
		
		return new Plugin(pluginRuntimePath.getName(),bootType,classLoader).bindPluginType(pluginType);
	}
	
	/**
	 * 装载流程
	 * @param flowName 流程名称
	 * @param classLoader 类装载器
	 * @throws Exception
	 */
	public final static void loadFlow(String flowName) throws Exception {
		log.info("load flow context: {} starting...",flowName);
		if(FLOW_DICT.containsKey(flowName)) {
			log.info("flow instance already defined for: {}...",flowName);
			return;
		}
		
		File flowFile=new File(projectFile,"flows/"+flowName+"/"+flowName+".properties");
		if(!flowFile.exists()){
			log.error("flow instance not defined for: {}...",flowName);
			throw new RuntimeException("flow instance not defined for: "+flowName);
		}
		
		Properties flowConfig=PropertiesReader.getProperties(flowFile);
		
		String realTimeStr=flowConfig.getProperty("realTime","").trim();
		boolean realTime=Boolean.parseBoolean(realTimeStr.isEmpty()?Default.ETL_MODE:realTimeStr);
		
		String clearCacheStr=flowConfig.getProperty("clearCache","").trim();
		boolean clearCache=Boolean.parseBoolean(clearCacheStr.isEmpty()?Default.CLEAR_CACHE:clearCacheStr);
		
		String sinkNameStr=flowConfig.getProperty(PluginType.sink.name,"").trim();
		String sinkName=sinkNameStr.isEmpty()?Default.SINK:sinkNameStr;
		
		String filterNameStr=flowConfig.getProperty(PluginType.filter.name,"").trim();
		String filterName=filterNameStr.isEmpty()?Default.Filter:filterNameStr;
		
		String sourceNameStr=flowConfig.getProperty(PluginType.source.name,"").trim();
		String sourceName=sourceNameStr.isEmpty()?(realTime?Default.REALTIME:Default.MANUAL):sourceNameStr;
		
		String transferName=flowConfig.getProperty(PluginType.transfer.name, "").trim();
		
		File flowPath=new File(Context.projectFile,CompType.flows+"/"+flowName);
		File sinkPath=new File(Context.projectFile,CompType.plugins+"/"+sinkName);
		File filterPath=new File(Context.projectFile,CompType.plugins+"/"+filterName);
		File sourcePath=new File(Context.projectFile,CompType.plugins+"/"+sourceName);
		File transferPath=transferName.isEmpty()?null:new File(Context.projectFile,CompType.plugins+"/"+transferName);
		
		if(!flowPath.exists()) throw new RuntimeException("flow is not exists: "+flowName);
		if(!sinkPath.exists()) throw new RuntimeException("sink plugin is not exists: "+sinkName);
		if(!filterPath.exists()) throw new RuntimeException("filter plugin is not exists: "+filterName);
		if(!sourcePath.exists()) throw new RuntimeException("source plugin is not exists: "+sourceName);
		if(null!=transferPath && !transferPath.exists()) throw new RuntimeException("transfer plugin is not exists: "+transferName);
		
		File shareRuntimePath=new File(flowPath,"share");
		File sinkRuntimePath=new File(flowPath,sinkName);
		File filterRuntimePath=new File(flowPath,filterName);
		File sourceRuntimePath=new File(flowPath,sourceName);
		File transferRuntimePath=null==transferPath?null:new File(flowPath,transferName);
		
		if(clearCache){
			log.info("drop ETL plugin cache for flow instance: {}...",flowName);
			FileUtil.dropFile(shareRuntimePath);
			FileUtil.dropFile(sinkRuntimePath);
			FileUtil.dropFile(filterRuntimePath);
			FileUtil.dropFile(sourceRuntimePath);
			if(null!=transferRuntimePath) FileUtil.dropFile(transferRuntimePath);
		}
		
		File sharePath=null;
		try {
			log.info("create ETL plugin cache for flow instance: {}...",flowName);
			FileUtil.copyFile(sinkPath, flowPath);
			FileUtil.copyFile(filterPath, flowPath);
			FileUtil.copyFile(sourcePath, flowPath);
			if(null!=transferPath) FileUtil.copyFile(transferPath, flowPath);
			(sharePath=new File(flowPath,"share")).mkdirs();
			new File(sharePath,"buffer.log.0").createNewFile();
		} catch (IOException e) {
			log.error("copy ETL plugin instance directory to flow instance directory failure...",e);
		}
		
		Plugin sink=loadPlugin(sinkRuntimePath,PluginType.sink,classLoader);
		Plugin filter=loadPlugin(filterRuntimePath,PluginType.filter,classLoader);
		Plugin source=loadPlugin(sourceRuntimePath,(realTime?PluginType.realtime:PluginType.manual),classLoader);
		Plugin transfer=null==transferRuntimePath?null:loadPlugin(transferRuntimePath,PluginType.transfer,classLoader);
		
		if(null==sink || null==filter || null==source) {
			log.error("flow plugin is not well defined, can not instantiate the flow for: {}...",flowName);
			throw new RuntimeException("flow plugin is not well defined, can not instantiate the flow for: "+flowName);
		}
		
		Flow flow=new Flow(flowName,sink,filter,source,transfer);
		String channelMaxSizeStr=flowConfig.getProperty("channelMaxSize","").trim();
		flow.channelMaxSize=Integer.valueOf(channelMaxSizeStr.isEmpty()?Default.CHANNEL_MAX_SIZE:channelMaxSizeStr);
		
		flow.sharePath=sharePath;
		flow.filterToSinkChannel=new Channel<Object>(flow.channelMaxSize);
		flow.sourceToFilterChannel=new Channel<Object>(flow.channelMaxSize);
		flow.transferToSourceChannel=new Channel<Object>(flow.channelMaxSize);
		
		FLOW_DICT.put(flowName, flow);
		
		log.info("load flow context: {} complete...",flowName);
	}
	
	/**
	 * 装载应用上下文实例
	 * @throws Exception 
	 */
	public static final void loadAppContext() throws Exception {
		log.info("load application context start...");
		
		projectFile=ApplicationUtil.getProjectPath();
		if(null==projectFile) return;
		
		Runtime.getRuntime().addShutdownHook(new GracefulShutdown());
		classLoader=Thread.currentThread().getContextClassLoader();
		generateAppPidFile();
		
		log.info("load application context complete...");
	}
	
	/**
	 * 装载流程上下文实例
	 * @throws Exception
	 */
	public static final void loadFlowContext(String startTip,String endTip) throws Exception {
		log.info(startTip);
		
		flowsLoaded=false;
		Properties contextConfig=PropertiesReader.getProperties("context.properties");
		
		String flowListStr=contextConfig.getProperty("flowList", Default.FLOWS).trim();
		flowList=COMMA_REGEX.split(flowListStr.isEmpty()?Default.FLOWS:flowListStr);
		for(int i=0;i<flowList.length;flowList[i]=flowList[i++].trim());
		
		String coreThreadStr=contextConfig.getProperty("coreThreads","").trim();
		coreThreads=coreThreadStr.isEmpty()?3*flowList.length:Integer.parseInt(coreThreadStr);
		
		String initOnStartStr=contextConfig.getProperty("initOnStart", Default.INIT_ON_START).trim();
		initOnStart=Boolean.parseBoolean(initOnStartStr.isEmpty()?Default.INIT_ON_START:initOnStartStr);
		
		String checkPluginFaceStr=contextConfig.getProperty("checkPluginFace", Default.CHECK_PLUGIN_FACE).trim();
		checkPluginFace=Boolean.parseBoolean(checkPluginFaceStr.isEmpty()?Default.CHECK_PLUGIN_FACE:checkPluginFaceStr);
		
		String etlSchedulerIntervalStr=contextConfig.getProperty("etlSchedulerInterval", Default.ETL_SCHEDULER_INTERVAL).trim();
		etlSchedulerInterval=Long.parseLong(etlSchedulerIntervalStr.isEmpty()?Default.ETL_SCHEDULER_INTERVAL:etlSchedulerIntervalStr);
		
		String traSchedulerIntervalStr=contextConfig.getProperty("traSchedulerInterval", Default.TRA_SCHEDULER_INTERVAL).trim();
		traSchedulerInterval=Long.parseLong(traSchedulerIntervalStr.isEmpty()?Default.TRA_SCHEDULER_INTERVAL:traSchedulerIntervalStr);
		
		log.info(endTip);
	}
	
	/**
	 * 输出JVM进程ID文件
	 * @throws Exception 
	 */
	private static final void generateAppPidFile() throws Exception {
		Integer processID=ApplicationUtil.getJvmProcessID();
		if(null==processID) return;
		File pidFile=new File(projectFile,"logs/pid");
		FileUtil.overrideWriteFile(pidFile, processID.toString());
		pidFile.deleteOnExit();
	}
}
