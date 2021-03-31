package com.github.lixiang2114.flow.caller;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.comps.TRAFuture;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.scheduler.TRASchedulerPool;

/**
 * @author Lixiang
 * @description 流程调度器
 */
public class TRACaller implements Callable<Object>{
	/**
	 * FlowFuture集合
	 */
	private static Collection<TRAFuture> traFutures;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(TRACaller.class);
	
	public TRACaller(Collection<TRAFuture> traFutures){
		TRACaller.traFutures=traFutures;
	}
	
	@Override
	public Object call() throws Exception {
		Flow flow=null;
		while(Context.traSchedulerStart) {
			Thread.sleep(Context.traSchedulerInterval);
			for(TRAFuture traFuture:traFutures){
				if(null==traFuture || !traFuture.isStarted || !traFuture.isDone()) continue;
				
				flow=traFuture.flow;
				log.warn("flow: {} is not running yet,it will be killing...",flow.compName);
				
				flow.transferStart=false;
				
				try{
					if(null!=traFuture.transferFuture) traFuture.transferFuture.cancel(true);
				}catch(Exception e) {
					log.warn("cancel transferFuture occue error:",e);
				}
				
				try {
					if(null!=flow.transfer) flow.transfer.callFace("checkPoint",new Object[]{null});
				} catch (Exception e) {
					log.error("flow: "+flow.compName+" plugin: "+flow.transfer.compName+" refresh checkpoint error...",e);
				}
				
				traFuture.isStarted=false;
				TRASchedulerPool.removeTRAFuture(flow.compName);
			}
		}
		return false;
	}
}
