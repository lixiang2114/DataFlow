package com.github.lixiang2114.flow.comps;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.plugins.face.ManualPlugin;
import com.github.lixiang2114.flow.plugins.face.RealtimePlugin;
import com.github.lixiang2114.flow.util.FileUtil;

/**
 * @author Lixiang
 * @description ETL流程配置
 */
public class Flow extends Comp{
	/**
	 * Sink插件
	 */
	public Plugin sink;
	
	/**
	 * Filter插件
	 */
	public Plugin filter;
	
	/**
	 * Source插件
	 */
	public Plugin source;
	
	/**
	 * Transfer插件
	 */
	public Plugin transfer;
	
	/**
	 * Sink插件实例运行时路径
	 */
	public File sinkPath;
	
	/**
	 * Filter插件实例运行时路径
	 */
	public File filterPath;
	
	/**
	 * Source插件实例运行时路径
	 */
	public File sourcePath;
	
	/**
	 * Transfer插件实例运行时路径
	 */
	public File transferPath;
	
	/**
	 * 流程实例插件共享路径
	 */
	public File sharePath;
	
	/**
	 * 是否为实时流程
	 */
	public boolean realTime;
	
	/**
	 * Sink插件是否已经启动
	 */
	public boolean sinkStart;
	
	/**
	 * Filter插件是否已经启动
	 */
	public boolean filterStart;
	
	/**
	 * Source插件是否已经启动
	 */
	public boolean sourceStart;
	
	/**
	 * Transfer插件是否已经启动
	 */
	public boolean transferStart;
	
	/**
	 * 是否有转存流程
	 */
	public boolean hasTransfer;
	
	/**
	 * 流程通道最大尺寸
	 */
	public Integer channelMaxSize;
	
	/**
	 * Filter插件到Sink插件的通道
	 */
	public Channel<Object> filterToSinkChannel;
	
	/**
	 * Source插件到Filter插件的通道
	 */
	public Channel<Object> sourceToFilterChannel;
	
	/**
	 * Transfer插件到Source插件的通道
	 */
	public Channel<Object> transferToSourceChannel;
	
	/**
	 * 日志工具
	 */
	public static final Logger log=LoggerFactory.getLogger(Flow.class);
	
	/**
	 * 上次发送失败的数据表
	 */
	public KeySetView<Object,Boolean> preFailSinkSet=ConcurrentHashMap.newKeySet();
	
	/**
	 * 流程插件字典
	 */
	public ConcurrentHashMap<PluginType, Plugin> pluginDict=new ConcurrentHashMap<PluginType, Plugin>();
	
	public Flow(String flowName,Plugin sink,Plugin filter,Plugin source,Plugin transfer,boolean clearCache){
		super(CompType.flows,flowName);
		if(RealtimePlugin.class.isAssignableFrom(source.bootClass)){
			this.realTime=true;
			source.bindPluginType(PluginType.realtime);
		}else if(ManualPlugin.class.isAssignableFrom(source.bootClass)){
			this.realTime=false;
			source.bindPluginType(PluginType.manual);
		}else if(PluginType.realtime==source.pluginType){
			this.realTime=true;
		}else {
			this.realTime=false;
		}
		
		pluginDict.put(PluginType.sink, this.sink=sink);
		pluginDict.put(PluginType.filter, this.filter=filter);
		pluginDict.put(PluginType.source, this.source=source);
		
		sinkPath=new File(super.compPath,sink.compName);
		filterPath=new File(super.compPath,filter.compName);
		sourcePath=new File(super.compPath,source.compName);
		
		if(clearCache){
			log.info("clear ETL plugin cache for flow instance: {}...",flowName);
			FileUtil.dropFile(new File(super.compPath,"share"));
			FileUtil.dropFile(new File(super.compPath,sink.compName));
			FileUtil.dropFile(new File(super.compPath,filter.compName));
			FileUtil.dropFile(new File(super.compPath,source.compName));
		}
		
		try {
			log.info("create ETL instance for flow: {}...",flowName);
			FileUtil.copyFile(sink.compPath, super.compPath);
			FileUtil.copyFile(filter.compPath, super.compPath);
			FileUtil.copyFile(source.compPath, super.compPath);
			(sharePath=new File(super.compPath,"share")).mkdirs();
			new File(sharePath,"buffer.log.0").createNewFile();
		} catch (IOException e) {
			log.error("copy ETL plugin instance directory to flow instance directory failure...",e);
		}
		
		if(null==transfer){
			hasTransfer=false;
		}else{
			hasTransfer=true;
			pluginDict.put(PluginType.transfer, this.transfer=transfer);
			transferPath=new File(super.compPath,transfer.compName);
			
			if(clearCache) {
				log.info("clear transfer plugin cache for flow instance: {}...",flowName);
				FileUtil.dropFile(new File(super.compPath,transfer.compName));
			}
			
			try {
				log.info("create transfer instance for flow: {}...",flowName);
				FileUtil.copyFile(transfer.compPath, super.compPath);
			} catch (IOException e) {
				log.error("copy transfer plugin instance directory to flow instance directory failure...",e);
			}
		}
		
		log.info("flowName:="+flowName+";sink="+sink+";filter="+filter+";source="+source+";transfer="+transfer+";realTime="+realTime);
	}
	
	/**
	 * 根据插件类型获取插件
	 * @param pluginType 插件类型
	 * @return 插件
	 */
	public Plugin getPlugin(PluginType pluginType) {
		return pluginDict.get(pluginType);
	}
}
