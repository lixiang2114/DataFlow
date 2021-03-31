package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.comps.Channel;

/**
 * @author Lixiang
 * @description 离线收集器(Source)插件接口
 * 用于处理离线数据(手动数据)
 * @param <F> 流程对象
 * @param <I> 读入类型
 * @param <O> 写出类型
 */
public interface ManualPlugin<F,I,O> extends SourcePlugin<F>{
	/**
	 * 处理离线数据
	 * @param sourceToFilterChannel ETL通道
	 * @description ETL源数据只能从本地预置文件读取
	 * @return 对象
	 */
	public abstract Object handle(Channel<O> sourceToFilterChannel) throws Exception;
}
