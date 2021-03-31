package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.comps.Channel;
import com.github.lixiang2114.flow.plugins.ETLFlow;

/**
 * @author Lixiang
 * @description 过滤器(Filter)插件接口
 * @param <T> 流程对象
 * @param <I> 读入类型
 * @param <O> 写出类型
 */
public interface FilterPlugin<F,I,O> extends ETLFlow<F> {
	/**
	 * 处理过滤数据
	 * @param sourceToFilterChannel ETL通道
	 * @param filterToSinkChannel Sink通道
	 * @return 对象
	 * @throws Exception
	 */
	public abstract Object filter(Channel<I> sourceToFilterChannel,Channel<O> filterToSinkChannel) throws Exception;
}
