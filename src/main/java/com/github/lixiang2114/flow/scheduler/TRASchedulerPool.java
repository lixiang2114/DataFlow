package com.github.lixiang2114.flow.scheduler;

import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;

import com.github.lixiang2114.flow.caller.TRACaller;
import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.comps.TRAFuture;
import com.github.lixiang2114.flow.context.Context;

/**
 * @author Lixiang
 * @description 转存流程控制调度器
 */
public class TRASchedulerPool extends SchedulerPool{
	/**
	 * 转存流程调度器控制句柄
	 */
	private static ListenableFuture<Object> traScheduerFuture;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(TRASchedulerPool.class);
	
	/**
	 * 转存流程句柄字典
	 */
	private static ConcurrentHashMap<String,TRAFuture> traFutureDict=new ConcurrentHashMap<String,TRAFuture>();
	
	/**
	 * 销毁流程控制句柄池
	 */
	public static final void destoryTRAPool(){
		traFutureDict.clear();
		traFutureDict=null;
	}
	
	/**
	 * 获取所有启动的流程名称
	 * @return 流程名称列表
	 */
	public static final Enumeration<String> getAllTRANames(){
		return traFutureDict.keys();
	}
	
	/**
	 * 获取所有启动的流程控制句柄
	 * @return 流程控制句柄列表
	 */
	public static final Collection<TRAFuture> getAllTRAFutures(){
		return traFutureDict.values();
	}
	
	/**
	 * 获取调度池中指定参数名的流程控制句柄
	 * @return 流程控制句柄
	 */
	public static final TRAFuture getTRAFuture(String flowName){
		return traFutureDict.get(flowName);
	}
	
	/**
	 * 移除调度池中指定参数名的流程控制句柄
	 * @param flowFuture 流程控制句柄
	 */
	public static final TRAFuture removeTRAFuture(String flowName){
		return traFutureDict.remove(flowName);
	}
	
	/**
	 * 参数TRA流程是否处于运行状态
	 * @return 流程控制句柄
	 */
	public static final boolean isRunning(String flowName){
		return traFutureDict.containsKey(flowName);
	}
	
	/**
	 * 获取应用流程控制句柄字典
	 * @return 流程控制句柄字典
	 */
	public static final ConcurrentHashMap<String,TRAFuture> getTRAFutureDict(){
		return traFutureDict;
	}
	
	/**
	 * 添加流程控制句柄
	 * @param flowFuture 流程控制句柄
	 */
	public static final void addTRAFuture(String flowName,TRAFuture flowFuture){
		traFutureDict.put(flowName,flowFuture);
	}
	
	/**
	 * 平滑停止指定流程
	 * @param flowName 流程名称
	 * @return 取消结果信息
	 */
	public static final String gracefulStopTRA(String flowName) {
		TRAFuture traFuture=traFutureDict.remove(flowName);
		if(null==traFuture) {
			log.warn("specify flow: {} is not exists...",flowName);
			return "specify flow: "+flowName+" is not exists...";
		}
		
		Flow flow=traFuture.flow;
		flow.transferStart=false;
		
		try{
			if(null!=traFuture.transferFuture) traFuture.transferFuture.cancel(true);
		}catch(Exception e){
			log.error("cancel transferFuture occur error:",e);
		}
		
		try {
			Object result=flow.transfer.callFace("stop", new Object[]{null});
			log.info("stop transfer process from:{} result(false:Success,true:Failure): {}",flowName,result);
		} catch (Exception e) {
			log.error("stop transfer process from: "+flowName+" occur error...",e);
		}
		
		traFuture.isStarted=false;
		log.info("stop flow {} complete...",flowName);
		return "stop flow "+flowName+" complete...";
	}
	
	/**
	 * 平滑停止所有流程
	 */
	public static final String gracefulStopAllTRAs() {
		Flow flow=null;
		TRAFuture traFuture=null;
		Enumeration<String> keys=traFutureDict.keys();
		while(keys.hasMoreElements()) {
			traFuture=traFutureDict.remove(keys.nextElement());
			if(null==traFuture) continue;
			
			flow=traFuture.flow;
			flow.transferStart=false;
			
			try{
				if(null!=traFuture.transferFuture) traFuture.transferFuture.cancel(true);
			}catch(Exception e) {
				log.error("cancel transferFuture occur error:",e);
			}
			
			try {
				Object result=flow.transfer.callFace("stop", new Object[]{null});
				log.info("stop transfer process from: {} result(false:Success,true:Failure): {}",flow.compName,result);
			} catch (Exception e) {
				log.error("stop transfer process from: "+flow.compName+" occur error...",e);
			}
			
			traFuture.isStarted=false;
		}
		
		Context.traSchedulerStart=false;
		try{
			if(null!=traScheduerFuture) traScheduerFuture.cancel(true);
		}catch(Exception e) {
			log.error("cancel traScheduerFuture occur error:",e);
		}
		
		log.info("stop tra flows complete...");
		return "stop tra flows complete...";
	}
	
	/**
	 * 启动TRA流程调度器
	 * 当某个流程中的某个插件遇到异常或错误时将终止整个流程
	 */
	public static final Boolean startTRAScheduler(){
		if(null!=Context.traSchedulerStart && Context.traSchedulerStart) {
			log.warn("TRA scheduler is already started...");
			return true;
		}
		
		if(0==traFutureDict.size()) return false;
		
		Context.traSchedulerStart=true;
		traScheduerFuture=getTaskExecutor().submitListenable(new TRACaller(traFutureDict.values()));
		log.info("flow scheduler start complete...");
		return true;
	}
	
	/**
	 * 停止TRA流程调度器
	 */
	public static final Boolean stopTRAScheduler() {
		Context.traSchedulerStart=false;
		try{
			if(null!=traScheduerFuture) traScheduerFuture.cancel(true);
			return true;
		}catch(Exception e) {
			log.error("cancel traScheduerFuture occur error:",e);
			return false;
		}
	}
}
