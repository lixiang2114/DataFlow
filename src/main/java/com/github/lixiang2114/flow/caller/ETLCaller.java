package com.github.lixiang2114.flow.caller;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.ETLFuture;
import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.scheduler.ETLSchedulerPool;

/**
 * @author Lixiang
 * @description 流程调度器
 */
public class ETLCaller implements Callable<Object>{
	/**
	 * FlowFuture集合
	 */
	private static Collection<ETLFuture> etlFutures;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(ETLCaller.class);
	
	public ETLCaller(Collection<ETLFuture> etlFutures){
		ETLCaller.etlFutures=etlFutures;
	}
	
	@Override
	public Object call() throws Exception {
		Flow flow=null;
		while(Context.etlSchedulerStart) {
			Thread.sleep(Context.etlSchedulerInterval);
			for(ETLFuture etlFuture:etlFutures) {
				if(null==etlFuture || !etlFuture.isStarted || !etlFuture.isDone()) continue;
				
				flow=etlFuture.flow;
				log.warn("flow: {} is not running yet,it will be killing...",flow.compName);
				
				flow.sourceStart=false;
				flow.filterStart=false;
				flow.sinkStart=false;
				
				if(null!=etlFuture.sourceFuture) etlFuture.sourceFuture.cancel(true);
				if(null!=etlFuture.filterFuture) etlFuture.filterFuture.cancel(true);
				if(null!=etlFuture.sinkFuture) etlFuture.sinkFuture.cancel(true);
				
				try {
					if(null!=flow.source) flow.source.callFace("checkPoint",new Object[]{null});
				} catch (Exception e) {
					log.error("flow: "+flow.compName+" plugin: "+flow.source.compName+" refresh checkpoint error...",e);
				}
				
				etlFuture.isStarted=false;
				ETLSchedulerPool.removeETLFuture(flow.compName);
			}
		}
		return false;
	}
}
