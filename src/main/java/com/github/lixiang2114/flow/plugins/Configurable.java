package com.github.lixiang2114.flow.plugins;

/**
 * @author Lixiang
 * @description 可配置接口
 */
public interface Configurable {
	/**
	 * 配置操作
	 * @param params 配置参数表
	 * @return 对象
	 */
	public abstract Object config(Object... params) throws Exception;
}
