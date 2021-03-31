package com.github.lixiang2114.flow.comps;

import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author Lixiang
 * @description ETL流程控制句柄
 */
public class ETLFuture {
	/**
	 * 流程对象
	 */
	public Flow flow;
	
	/**
	 * 流程是否已经启动
	 */
	public Boolean isStarted=true;
	
	/**
	 * Sink插件线程控制句柄
	 */
	public ListenableFuture<?> sinkFuture;
	
	/**
	 * Filter插件线程控制句柄
	 */
	public ListenableFuture<?> filterFuture;
	
	/**
	 * Source插件线程控制句柄
	 */
	public ListenableFuture<?> sourceFuture;
	
	public ETLFuture(){}
	
	public ETLFuture(Flow flow){
		this.flow=flow;
	}
	
	/**
	 * 是否为实时流程
	 * @return 是否为实时流程
	 */
	public boolean isRealTime(){
		return flow.realTime;
	}
	
	/**
	 * 流程是否停运
	 * @return 是否停运
	 */
	public boolean isDone(){
		if(null==sinkFuture || null==filterFuture || null==sourceFuture) return true;
		return (sinkFuture.isDone() || filterFuture.isDone() || sourceFuture.isDone());
	}
}
