package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.comps.Channel;
import com.github.lixiang2114.flow.plugins.ETLFlow;

/**
 * @author Lixiang
 * @description 发送器(Sink)插件接口
 * @param <F> 流程对象
 * @param <D> 写出数据
 */
public interface SinkPlugin<F,D> extends ETLFlow<F> {
	/**
	 * 处理通道数据
	 * @param filterToSinkChannel ETL数据通道
	 * @return 对象
	 */
	public abstract Object send(Channel<D> filterToSinkChannel) throws Exception;
}
