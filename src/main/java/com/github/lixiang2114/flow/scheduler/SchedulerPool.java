package com.github.lixiang2114.flow.scheduler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.thread.SpringThreadPool;

/**
 * @author Lixiang
 * @description 流程控制调度器
 */
public abstract class SchedulerPool {
	/**
	 * Spring线程池执行器
	 */
	private static ThreadPoolTaskExecutor taskExecutor;
	
	/**
	 * 获取Spring线程池执行器
	 * @return 线程池执行器
	 */
	public synchronized static final ThreadPoolTaskExecutor getTaskExecutor(){
		if(null!=taskExecutor) return taskExecutor;
		return taskExecutor=SpringThreadPool.getSpringThreadPool(Context.coreThreads);
	}
	
	/**
	 * 获取池化参数状态
	 * @return 池化参数字典表
	 */
	public synchronized static final HashMap<String,Object> getPoolParams() {
		ThreadPoolTaskExecutor taskExecutor=getTaskExecutor();
		ThreadPoolExecutor executor=taskExecutor.getThreadPoolExecutor();
		BlockingQueue<Runnable> queue=executor.getQueue();
		
		LinkedHashMap<String,Object> runtimeArgs=new LinkedHashMap<String,Object>();
		runtimeArgs.put("threadNum", executor.getPoolSize());
		runtimeArgs.put("activeThreadNum", executor.getActiveCount());
		runtimeArgs.put("totalTaskNum", executor.getTaskCount());
		runtimeArgs.put("completedTaskNum", executor.getCompletedTaskCount());
		runtimeArgs.put("historyMaxPoolSize", executor.getLargestPoolSize());
		runtimeArgs.put("remainingCapacity", queue.remainingCapacity());
		runtimeArgs.put("queueSize", queue.size());
		
		
		LinkedHashMap<String,Object> staticSetArgs=new LinkedHashMap<String,Object>();
		staticSetArgs.put("corePoolSize", taskExecutor.getCorePoolSize());
		staticSetArgs.put("maxPoolSize", taskExecutor.getMaxPoolSize());
		staticSetArgs.put("isDaemon", taskExecutor.isDaemon());
		staticSetArgs.put("priority", taskExecutor.getThreadPriority());
		staticSetArgs.put("keepAliveSeconds", taskExecutor.getKeepAliveSeconds());
		
		HashMap<String,Object> poolParams=new HashMap<String,Object>();
		poolParams.put("staticSetArgs", staticSetArgs);
		poolParams.put("runtimeArgs", runtimeArgs);
		return poolParams;
	}
}
