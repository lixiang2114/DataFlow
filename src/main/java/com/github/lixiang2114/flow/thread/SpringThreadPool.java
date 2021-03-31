package com.github.lixiang2114.flow.thread;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.lixiang2114.flow.util.ApplicationUtil;

/**
 * @author Lixiang
 * @description Spring线程池
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SpringThreadPool {
	/**
	 * 核心线程池尺寸
	 */
	private static final Integer CORE_POOL_SIZE=5;
	
	/**
	 * 最大线程池尺寸
	 */
	private static final Integer MAX_POOL_SIZE=20;
	
	/**
	 * 任务缓冲队列尺寸
	 */
	private static final Integer QUEUE_CAPACITY=100;
	
	/**
	 * 线程最大空闲时间(单位:秒)
	 */
	private static final Integer KEEP_ALIVE_SECONDS=300;
	
	/**
	 * 新创建线程的名称前缀
	 */
	private static final String THREAD_NAME_PREFIX="Spring-ThreadPool-";
	
	/**
	 * 允许核心线程超时后也关闭
	 */
	private static final Boolean ALLOW_CORETHREAD_TIMEOUT=false;
	
	/**
	 * 丢弃新任务且抛出RejectedExecutionException异常(默认值)
	 */
	public static final Class<? extends RejectedExecutionHandler> DROP_THROWS=ThreadPoolExecutor.AbortPolicy.class;
	
	/**
	 * 使用调用者线程处理队列溢出任务
	 */
	public static final Class<? extends RejectedExecutionHandler> CALLER_RUNS=ThreadPoolExecutor.CallerRunsPolicy.class;
	
	/**
	 * 丢弃新任务且不抛出异常
	 */
	public static final Class<? extends RejectedExecutionHandler> DROP_NOTHROWS=ThreadPoolExecutor.DiscardPolicy.class;
	
	/**
	 * 丢弃队列中老任务(最早,最前面)且不抛出异常
	 */
	public static final Class<? extends RejectedExecutionHandler> DROP_OLDS_NOTHROWS=ThreadPoolExecutor.DiscardOldestPolicy.class;
	
	/**
	 * 队列中任务满之后采取的策略
	 */
	public static final HashMap<String,Class<? extends RejectedExecutionHandler>> REJECTED_POLICY=new HashMap<String,Class<? extends RejectedExecutionHandler>>();
	
	/**
	 * 获取异步调用异常处理器
	 * @return AsyncUncaughtExceptionHandler
	 */
	public static AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
	
	/**
	 * 获取Sleuth中的LazyTraceExecutor包装器以支持自定义线程池中创建异步Span
	 * Sleuth默认的线程池包装器是org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor
	 * @param executor 线程池执行器
	 * @return 包装后的线程池执行器
	 */
	public static Executor getLazyTraceExecutor(Executor executor){
		try {
			Class executorType=Class.forName("org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor");
			Constructor<Executor> cons=executorType.getDeclaredConstructor(BeanFactory.class,Executor.class);
			return cons.newInstance(ApplicationUtil.getApplicationContext(),executor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取Sleuth中的LazyTraceThreadPoolTaskExecutor包装器以支持自定义线程池中创建异步Span
	 * Sleuth默认的线程池包装器是org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor
	 * @param executor 线程池执行器
	 * @return 包装后的线程池执行器
	 */
	public static ThreadPoolTaskExecutor getLazyTraceExecutor(ThreadPoolTaskExecutor executor){
		try {
			Class executorType=Class.forName("org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor");
			Constructor<ThreadPoolTaskExecutor> cons=executorType.getDeclaredConstructor(BeanFactory.class,ThreadPoolTaskExecutor.class);
			return cons.newInstance(ApplicationUtil.getApplicationContext(),executor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取固定容量大小且无缓冲队列的线程池,池中无空闲线程后使用调用方线程处理任务
	 * @param poolSize 池中固定线程数量
	 * @param queueSize 任务队列尺寸
	 * @return 线程池
	 */
	public static ThreadPoolTaskExecutor getFixedTaskExecutor(Integer poolSize){
		return getTaskExecutor(poolSize,poolSize,0,0,THREAD_NAME_PREFIX,ALLOW_CORETHREAD_TIMEOUT,CALLER_RUNS);
	}
	
	/**
	 * 获取固定容量大小且具有缓冲队列的线程池,池中无空闲线程后转入缓冲队列,队列溢出后使用调用方线程处理任务
	 * @param poolSize 池中固定线程数量
	 * @param queueSize 任务队列尺寸
	 * @return 线程池
	 */
	public static ThreadPoolTaskExecutor getFixedTaskExecutor(Integer poolSize,Integer queueSize){
		return getTaskExecutor(poolSize,poolSize,queueSize,0,THREAD_NAME_PREFIX,ALLOW_CORETHREAD_TIMEOUT,CALLER_RUNS);
	}
	
	/**
	 * 获取默认线程池实现(任务队列溢出后使用调用方线程处理任务)
	 * @return ThreadPoolTaskExecutor
	 */
	public static ThreadPoolTaskExecutor getDefaultExecutor(){
        return getTaskExecutor(CORE_POOL_SIZE,MAX_POOL_SIZE,QUEUE_CAPACITY,KEEP_ALIVE_SECONDS,THREAD_NAME_PREFIX,ALLOW_CORETHREAD_TIMEOUT,CALLER_RUNS);
	}
	
	/**
	 * 获取自定义线程池
	 * @param coreSize 池中核心线程数量
	 * @param maxSize 池中最大线程数量
	 * @param queueSize 任务队列尺寸
	 * @param idleTime 线程空闲时间
	 * @param threadNamePrefix 创建新线程的名称前缀
	 * @param allowCoreTimeout 允许核心数量的线程超时关闭
	 * @param rejectedPolicy 任务队列溢出处理策略
	 * @return
	 */
	public static ThreadPoolTaskExecutor getTaskExecutor(Integer coreSize,Integer maxSize,Integer queueSize,Integer idleTime,String threadNamePrefix,Boolean allowCoreTimeout,Class<? extends RejectedExecutionHandler> rejectedPolicy){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueSize);
        executor.setKeepAliveSeconds(idleTime);
        executor.setThreadNamePrefix(threadNamePrefix); 
        executor.setAllowCoreThreadTimeOut(allowCoreTimeout);
        try {
			executor.setRejectedExecutionHandler(rejectedPolicy.newInstance());
			return executor;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取Spring默认线程池
	 * 扩展为无缓冲队列线程池
	 * @return 线程池
	 */
	public static ThreadPoolTaskExecutor getSpringThreadPool(Integer... poolSizes) {
		ThreadPoolTaskExecutor taskExecutor=getSpringContextThreadPool(ThreadPoolTaskExecutor.class);
		taskExecutor.setQueueCapacity(0);
		if(null!=poolSizes) {
			if(0<poolSizes.length && null!=poolSizes[0]) taskExecutor.setCorePoolSize(poolSizes[0]);
			if(1<poolSizes.length && null!=poolSizes[1]) taskExecutor.setMaxPoolSize(poolSizes[1]);
		}
		taskExecutor.initialize();
		return taskExecutor;
	}
	
	/**
	 * 获取Spring上下文中第一个兼容的线程池
	 * @param threadPoolType 兼容的线程池类型
	 * @return 线程池
	 */
	public static <R extends Executor> R getSpringContextThreadPool(Class<R>... threadPoolType){
		Class<R> poolType=null==threadPoolType||0==threadPoolType.length?(Class<R>)ThreadPoolTaskExecutor.class:threadPoolType[0];
		Map<String, R> executors=ApplicationUtil.getBeansOfType(poolType);
		if(null==executors||0==executors.size()) return null;
		return executors.entrySet().iterator().next().getValue();
	}
}
