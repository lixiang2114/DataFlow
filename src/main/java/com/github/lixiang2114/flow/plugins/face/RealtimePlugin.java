package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.comps.Channel;

/**
 * @author Lixiang
 * @description 实时收集器(Source)插件接口
 * 用于处理实时数据(在线数据)
 * @param <F> 流程对象
 * @param <I> 读入类型
 * @param <O> 写出类型
 */
public interface RealtimePlugin<F,I,O> extends SourcePlugin<F>{
	/**
	 * 处理实时数据
	 * @param transferToETLChannel 转存通道
	 * @param sourceToFilterChannel ETL通道
	 * @description ETL源数据可以从本地转存文件或参数转存通道读取
	 * @return 对象
	 * @throws Exception
	 */
	public abstract Object handle(Channel<I> transferToETLChannel,Channel<O> sourceToFilterChannel) throws Exception;
}
