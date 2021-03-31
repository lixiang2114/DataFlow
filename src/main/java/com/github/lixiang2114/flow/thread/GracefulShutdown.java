package com.github.lixiang2114.flow.thread;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.lixiang2114.flow.scheduler.ETLSchedulerPool;
import com.github.lixiang2114.flow.scheduler.SchedulerPool;
import com.github.lixiang2114.flow.scheduler.TRASchedulerPool;

/**
 * @author Lixiang
 * @description 平滑关闭DataFlow例程
 */
public class GracefulShutdown extends Thread{	
	
	@Override
	public void run() {
		try{
			TRASchedulerPool.gracefulStopAllTRAs();
			TRASchedulerPool.stopTRAScheduler();
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			ETLSchedulerPool.gracefulStopAllETLs();
			ETLSchedulerPool.stopETLScheduler();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		try{
			TRASchedulerPool.destoryTRAPool();
			ETLSchedulerPool.destoryETLPool();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		ThreadPoolTaskExecutor taskExecutor=SchedulerPool.getTaskExecutor();
		if(null!=taskExecutor) taskExecutor.destroy();
	}
}
