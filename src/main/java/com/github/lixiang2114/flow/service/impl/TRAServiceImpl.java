package com.github.lixiang2114.flow.service.impl;

import java.util.Collection;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.comps.TRAFuture;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.handler.TransferHandler;
import com.github.lixiang2114.flow.scheduler.SchedulerPool;
import com.github.lixiang2114.flow.scheduler.TRASchedulerPool;
import com.github.lixiang2114.flow.service.TRAService;

/**
 * @author Lixiang
 * @description 日志TRA服务实现
 */
@Service("traService")
public class TRAServiceImpl extends BaseService implements TRAService{
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(TRAServiceImpl.class);
	
	@Override
	public String startTRAProcess(String flowName) throws Exception {
		Flow flow=Context.getFlow(flowName);
		if(!flow.hasTransfer){
			log.error("TRA process is not exists for: {}...",flowName);
			return "TRA process is not exists for: "+flowName;
		}
		
		if(TRASchedulerPool.isRunning(flowName)) {
			log.info("TRA flow: {} is running,no need to start...",flowName);
			return "TRA flow: "+flowName+" is running,no need to start...";
		}
		
		TRAFuture traFuture=new TRAFuture(flow);
		ThreadPoolTaskExecutor taskExecutor=SchedulerPool.getTaskExecutor();
		
		startTransfer(traFuture,taskExecutor);
		
		traFuture.isStarted=true;
		TRASchedulerPool.addTRAFuture(flowName, traFuture);
		TRASchedulerPool.startTRAScheduler();
		
		log.warn("TRA flow: {} is already started...",flowName);
		return "TRA flow: "+flowName+" is already started...";
	}

	@Override
	public String stopTRAProcess(String flowName) throws Exception {
		return TRASchedulerPool.gracefulStopTRA(flowName);
	}
	
	@Override
	public String startAllTRAProcess() throws Exception {
		ThreadPoolTaskExecutor taskExecutor=SchedulerPool.getTaskExecutor();
		Collection<Flow> flows=Context.getFlowDict().values();
		TRAFuture traFuture=null;
		String flowName=null;
		
		for(Flow flow:flows){
			if(!flow.hasTransfer) continue;
			if(TRASchedulerPool.isRunning(flowName=flow.compName)) continue;
			
			traFuture=new TRAFuture(flow);
			startTransfer(traFuture,taskExecutor);
			
			traFuture.isStarted=true;
			TRASchedulerPool.addTRAFuture(flowName, traFuture);
			
			log.info("TRA flow: {} is already started...",flowName);
		}
		
		TRASchedulerPool.startTRAScheduler();
		log.info("start all TRA flows complete...");
		return "start all TRA flows  complete...";
	}
	
	@Override
	public String stopAllTRAProcess() throws Exception {
		return TRASchedulerPool.gracefulStopAllTRAs();
	}
	
	@Override
	public Object refleshTRACheckpoint(String flowName) throws Exception {
		return Context.getFlow(flowName).transfer.callFace("checkPoint",new Object[0]);
	}

	@Override
	public String refleshAllTRACheckpoint() throws Exception {
		Enumeration<String> allTRANames=TRASchedulerPool.getAllTRANames();
		while(allTRANames.hasMoreElements()){
			String flowName=allTRANames.nextElement();
			if(null==flowName || flowName.trim().isEmpty()) continue;
			try{
				Context.getFlow(flowName.trim()).transfer.callFace("checkPoint",new Object[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return "refresh all TRA flows checkpoint complete...";
	}
	
	/**
	 * 从指定参数流程中启动Transfer插件
	 * @param traFuture 转存流程控制句柄
	 * @param taskExecutor Spring线程池
	 * @return 信息提示
	 * @throws Exception
	 */
	private String startTransfer(TRAFuture traFuture,ThreadPoolTaskExecutor taskExecutor) throws Exception {
		log.info("start tansfer plugin from flow: {}...",traFuture.flow.compName);
		traFuture.transferFuture=taskExecutor.submitListenable(new TransferHandler(traFuture.flow));
		log.info("tansfer plugin from flow: {} is already startted...",traFuture.flow.compName);
		return "tansfer plugin from flow: "+traFuture.flow.compName+" is already startted...";
	}
}
