package com.github.lixiang2114.flow.comps;

import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author Lixiang
 * @description 转存流程控制句柄
 */
public class TRAFuture {
	/**
	 * 流程对象
	 */
	public Flow flow;
	
	/**
	 * 流程是否已经启动
	 */
	public Boolean isStarted=true;
	
	/**
	 * Transfer插件线程控制句柄
	 */
	public ListenableFuture<?> transferFuture;
	
	public TRAFuture(){}
	
	public TRAFuture(Flow flow){
		this.flow=flow;
	}
	
	/**
	 * 流程是否停运
	 * @return 是否停运
	 */
	public boolean isDone(){
		if(null==transferFuture) return true;
		return transferFuture.isDone();
	}
}
