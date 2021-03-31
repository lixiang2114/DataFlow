package com.github.lixiang2114.flow.plugins;

/**
 * @author Lixiang
 * @description 检查点接口
 */
public interface CheckPointable {
	/**
	 * 刷新检查点
	 * @param params 对象
	 * @return 对象
	 */
	public Object checkPoint(Object params) throws Exception;
}
