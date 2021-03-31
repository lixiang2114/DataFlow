package com.github.lixiang2114.flow.plugins.face;

/**
 * @author Lixiang
 * @description 过程接口
 * 用于描述流程的各个阶段
 * @param <F> 流程对象
 */
public interface Procedure<F> {
	/**
	 * 启动操作
	 * @param flow 流程对象
	 * @return 对象
	 */
	public abstract Boolean start(F flow) throws Exception;
	
	/**
	 * 停止操作
	 * @param params 对象
	 * @return 对象
	 */
	public abstract Object stop(Object params) throws Exception;
}
