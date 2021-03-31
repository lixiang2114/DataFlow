package com.github.lixiang2114.flow.comps;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lixiang
 * @description 流程通道
 * @param <R>
 */
@SuppressWarnings("serial")
public class Channel<R> extends LinkedBlockingQueue<R>{
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(Channel.class);
	
	/**
	 * 构造方法
	 */
	public Channel(){}
	
	/**
	 * 构造方法
	 * @param capacity 队列尺寸
	 */
	public Channel(Integer capacity){
		super(capacity);
	}
	
	/**
	 * 返回队列中元素数量
	 * @return 元素数量
	 */
	public int length(){
		return super.size();
	}
	
	/**
	 * 阻塞直到队列中有数据时返回并移除队首元素
	 * @return 泛化类型
	 */
	public R get(){
		try {
			return super.take();
		} catch (InterruptedException e) {
			log.warn("channel interrupted error: {}",e.getMessage());
		}
		return null;
	}
	
	/**
	 * 从队首开始返回并移除队列中给定数量的元素,若队列中没有指定数量的元素则返回队列中所有元素,若队列为空则返回NULL
	 * @param maxNumber 需要获取的元素数量(可选),若未给定batchSize参数则返回并移除队列中所有元素
	 * @return 元素集合,本方法快速返回、无阻塞
	 */
	public ArrayList<R> batchGet(Integer... batchSize){
		ArrayList<R> resultList=new ArrayList<R>();
		if(null==batchSize || 0==batchSize.length) {
			super.drainTo(resultList);
		}else{
			super.drainTo(resultList,batchSize[0]);
		}
		return 0==resultList.size()?null:resultList;
	}
	
	/**
	 * 等待给定的最大时间内返回并移除队列中队首元素,如果超时则返回NULL,
	 * 若未给定timeout参数则快速无阻塞返回队首元素(若队列为空则返回NULL)
	 * @param timeout 超时时间(单位:毫秒),即等待队列中有元素到来前的最大等待时间
	 * @return 泛化类型
	 */
	public R get(Long... timeout){
		try {
			if(null==timeout || 0==timeout.length) return super.poll();
			return super.poll(timeout[0], TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.warn("channel interrupted error: {}",e.getMessage());
		}
		return null;
	}
}
