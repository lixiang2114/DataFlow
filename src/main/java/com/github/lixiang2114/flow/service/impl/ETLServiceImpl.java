package com.github.lixiang2114.flow.service.impl;

import java.util.Collection;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.github.lixiang2114.flow.comps.ETLFuture;
import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.handler.FilterHandler;
import com.github.lixiang2114.flow.handler.SinkHandler;
import com.github.lixiang2114.flow.handler.SourceHandler;
import com.github.lixiang2114.flow.scheduler.ETLSchedulerPool;
import com.github.lixiang2114.flow.scheduler.SchedulerPool;
import com.github.lixiang2114.flow.service.ETLService;

/**
 * @author Lixiang
 * @description 日志ETL服务实现
 */
@Service("etlService")
public class ETLServiceImpl extends BaseService implements ETLService{
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(ETLServiceImpl.class);
	
	@Override
	public String startETLProcess(String flowName) throws Exception {
		if(ETLSchedulerPool.isRunning(flowName)) {
			log.info("ETL flow: {} is running,no need to start...",flowName);
			return "ETL flow: "+flowName+" is running,no need to start...";
		}
		
		ETLFuture etlFuture=new ETLFuture(Context.getFlow(flowName));
		ThreadPoolTaskExecutor taskExecutor=SchedulerPool.getTaskExecutor();
		
		startSink(etlFuture,taskExecutor);
		startFilter(etlFuture,taskExecutor);
		startSource(etlFuture,taskExecutor);
		
		etlFuture.isStarted=true;
		ETLSchedulerPool.addETLFuture(flowName, etlFuture);
		ETLSchedulerPool.startETLScheduler();
		
		log.info("ETL flow: {} is already started...",flowName);
		return "ETL flow: "+flowName+" is already started...";
	}
	
	@Override
	public String stopETLProcess(String flowName) throws Exception {
		return ETLSchedulerPool.gracefulStopETL(flowName);
	}
	
	@Override
	public String startAllETLProcess() throws Exception {
		ThreadPoolTaskExecutor taskExecutor=SchedulerPool.getTaskExecutor();
		Collection<Flow> flows=Context.getFlowDict().values();
		ETLFuture etlFuture=null;
		String flowName=null;
		
		for(Flow flow:flows) {
			if(ETLSchedulerPool.isRunning(flowName=flow.compName)) continue;
			
			etlFuture=new ETLFuture(flow);
			
			startSink(etlFuture,taskExecutor);
			startFilter(etlFuture,taskExecutor);
			startSource(etlFuture,taskExecutor);
			
			etlFuture.isStarted=true;
			ETLSchedulerPool.addETLFuture(flowName, etlFuture);
			
			log.info("ETL flow: {} is already started...",flowName);
		}
		
		ETLSchedulerPool.startETLScheduler();
		log.info("start all ETL flows complete...");
		return "start all ETL flows complete...";
	}
	
	@Override
	public String stopAllETLProcess() throws Exception {
		return ETLSchedulerPool.gracefulStopAllETLs();
	}
	
	@Override
	public Object refleshETLCheckpoint(String flowName) throws Exception {
		return Context.getFlow(flowName.trim()).source.callFace("checkPoint",new Object[]{null});
	}

	@Override
	public String refleshAllETLCheckpoint() throws Exception {
		Enumeration<String> allETLNames=ETLSchedulerPool.getAllETLNames();
		while(allETLNames.hasMoreElements()){
			String flowName=allETLNames.nextElement();
			if(null==flowName || flowName.trim().isEmpty()) continue;
			try{
				Context.getFlow(flowName.trim()).source.callFace("checkPoint",new Object[]{null});
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return "refresh all ETL flows checkpoint complete...";
	}
	
	/**
	 * 从指定参数流程中启动Sink插件
	 * @param etlFuture ETL流程控制句柄
	 * @param taskExecutor Spring线程池
	 * @return 信息提示
	 * @throws Exception
	 */
	private String startSink(ETLFuture etlFuture,ThreadPoolTaskExecutor taskExecutor) throws Exception {
		log.info("start sink plugin from flow: {}...",etlFuture.flow.compName);
		etlFuture.sinkFuture=taskExecutor.submitListenable(new SinkHandler(etlFuture.flow));
		log.info("sink plugin from flow: {} is already startted...",etlFuture.flow.compName);
		return "sink plugin from flow: "+etlFuture.flow.compName+" is already startted...";
	}
	
	/**
	 * 从指定参数流程中启动Filter插件
	 * @param etlFuture ETL流程控制句柄
	 * @param taskExecutor Spring线程池
	 * @return 信息提示
	 * @throws Exception
	 */
	private String startFilter(ETLFuture etlFuture,ThreadPoolTaskExecutor taskExecutor) throws Exception {
		log.info("start filter plugin from flow: {}...",etlFuture.flow.compName);
		etlFuture.filterFuture=taskExecutor.submitListenable(new FilterHandler(etlFuture.flow));
		log.info("filter plugin from flow: {} is already startted...",etlFuture.flow.compName);
		return "filter plugin from flow: "+etlFuture.flow.compName+" is already startted...";
	}
	
	/**
	 * 从指定参数流程中启动Source插件
	 * @param etlFuture ETL流程控制句柄
	 * @param taskExecutor Spring线程池
	 * @return 信息提示
	 * @throws Exception
	 */
	private String startSource(ETLFuture etlFuture,ThreadPoolTaskExecutor taskExecutor) throws Exception {
		log.info("start source plugin from flow: {}...",etlFuture.flow.compName);
		etlFuture.sourceFuture=taskExecutor.submitListenable(new SourceHandler(etlFuture.flow));
		log.info("source plugin from flow: {} is already startted...",etlFuture.flow.compName);
		return "source plugin from flow: "+etlFuture.flow.compName+" is already startted...";
	}
}
