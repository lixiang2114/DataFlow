package com.github.lixiang2114.flow.scheduler;

import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;

import com.github.lixiang2114.flow.caller.ETLCaller;
import com.github.lixiang2114.flow.comps.ETLFuture;
import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.context.Context;

/**
 * @author Lixiang
 * @description ETL流程控制调度器
 */
public class ETLSchedulerPool extends SchedulerPool{
	/**
	 * ETL流程调度器控制句柄
	 */
	private static ListenableFuture<Object> etlScheduerFuture;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(ETLSchedulerPool.class);
	
	/**
	 * ETL流程句柄字典
	 */
	private static ConcurrentHashMap<String,ETLFuture> etlFutureDict=new ConcurrentHashMap<String,ETLFuture>();
	
	/**
	 * 销毁流程控制句柄池
	 */
	public static final void destoryETLPool(){
		etlFutureDict.clear();
		etlFutureDict=null;
	}
	
	/**
	 * 获取所有启动的流程名称
	 * @return 流程名称列表
	 */
	public static final Enumeration<String> getAllETLNames(){
		return etlFutureDict.keys();
	}
	
	/**
	 * 获取所有启动的流程控制句柄
	 * @return 流程控制句柄列表
	 */
	public static final Collection<ETLFuture> getAllETLFutures(){
		return etlFutureDict.values();
	}
	
	/**
	 * 获取调度池中指定参数名的流程控制句柄
	 * @return 流程控制句柄
	 */
	public static final ETLFuture getETLFuture(String flowName){
		return etlFutureDict.get(flowName);
	}
	
	/**
	 * 移除调度池中指定参数名的流程控制句柄
	 * @param flowFuture 流程控制句柄
	 */
	public static final ETLFuture removeETLFuture(String flowName){
		return etlFutureDict.remove(flowName);
	}
	
	/**
	 * 参数ETL流程是否处于运行状态
	 * @return 流程控制句柄
	 */
	public static final boolean isRunning(String flowName){
		return etlFutureDict.containsKey(flowName);
	}
	
	/**
	 * 获取应用流程控制句柄字典
	 * @return 流程控制句柄字典
	 */
	public static final ConcurrentHashMap<String,ETLFuture> getETLFutureDict(){
		return etlFutureDict;
	}
	
	/**
	 * 添加流程控制句柄
	 * @param flowFuture 流程控制句柄
	 */
	public static final void addETLFuture(String flowName,ETLFuture etlFuture){
		etlFutureDict.put(flowName,etlFuture);
	}
	
	/**
	 * 平滑停止指定的流程
	 * @param flowName 流程名称
	 * @return 取消结果信息
	 */
	public static final String gracefulStopETL(String flowName){
		ETLFuture etlFuture=etlFutureDict.remove(flowName);
		if(null==etlFuture) {
			log.warn("specify flow: {} is not exists...",flowName);
			return "specify flow: "+flowName+" is not exists...";
		}
		
		Flow flow=etlFuture.flow;
		flow.sourceStart=false;
		flow.filterStart=false;
		flow.sinkStart=false;
		
		try{
			if(null!=etlFuture.sourceFuture)etlFuture.sourceFuture.cancel(true);
		}catch(Exception e){
			log.error("cancel sourceFuture occur error:",e);
		}
		
		try{
			if(null!=etlFuture.filterFuture) etlFuture.filterFuture.cancel(true);
		}catch(Exception e){
			log.error("cancel filterFuture occur error:",e);
		}
		
		try{
			if(null!=etlFuture.sinkFuture) etlFuture.sinkFuture.cancel(true);
		}catch(Exception e){
			log.error("cancel sinkFuture occur error:",e);
		}
		
		etlFuture.isStarted=false;
		log.info("stop flow {} complete...",flowName);
		return "stop flow "+flowName+" complete...";
	}
	
	/**
	 * 平滑停止所有流程
	 */
	public static final String gracefulStopAllETLs(){
		Flow flow=null;
		ETLFuture etlFuture=null;
		Enumeration<String> keys=etlFutureDict.keys();
		while(keys.hasMoreElements()) {
			etlFuture=etlFutureDict.remove(keys.nextElement());
			if(null==etlFuture) continue;
			
			flow=etlFuture.flow;
			
			flow.sourceStart=false;
			flow.filterStart=false;
			flow.sinkStart=false;
			
			try{
				if(null!=etlFuture.sourceFuture)etlFuture.sourceFuture.cancel(true);
			}catch(Exception e){
				log.error("cancel sourceFuture occur error:",e);
			}
			
			try{
				if(null!=etlFuture.filterFuture) etlFuture.filterFuture.cancel(true);
			}catch(Exception e){
				log.error("cancel filterFuture occur error:",e);
			}
			
			try{
				if(null!=etlFuture.sinkFuture) etlFuture.sinkFuture.cancel(true);
			}catch(Exception e){
				log.error("cancel sinkFuture occur error:",e);
			}
			
			etlFuture.isStarted=false;
		}
		
		Context.etlSchedulerStart=false;
		try{
			if(null!=etlScheduerFuture) etlScheduerFuture.cancel(true);
		}catch(Exception e) {
			log.error("cancel etlScheduerFuture occur error:",e);
		}
		
		log.info("stop etl flows complete...");
		return "stop etl flows complete...";
	}
	
	/**
	 * 启动流程调度器
	 * 当某个流程中的某个插件遇到异常或错误时将终止整个流程
	 */
	public static final Boolean startETLScheduler(){
		if(null!=Context.etlSchedulerStart && Context.etlSchedulerStart) {
			log.warn("ETL scheduler is already started...");
			return true;
		}
		
		if(0==etlFutureDict.size()) return false;
		
		Context.etlSchedulerStart=true;
		etlScheduerFuture=getTaskExecutor().submitListenable(new ETLCaller(etlFutureDict.values()));
		log.info("flow scheduler start complete...");
		return true;
	}
	
	/**
	 * 停止流程调度器
	 */
	public static final Boolean stopETLScheduler(){
		Context.etlSchedulerStart=false;
		try{
			if(null!=etlScheduerFuture) etlScheduerFuture.cancel(true);
			return true;
		}catch(Exception e){
			log.error("cancel etlScheduerFuture occur error:",e);
			return false;
		}
	}
}
