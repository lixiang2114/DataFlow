package com.github.lixiang2114.flow.plugins.face;

import com.github.lixiang2114.flow.comps.Channel;
import com.github.lixiang2114.flow.plugins.CheckPointable;
import com.github.lixiang2114.flow.plugins.TRAFlow;

/**
 * @author Lixiang
 * @description 转换器(Transfer)插件接口
 * @param <F> 流程对象
 * @param <I> 读入类型
 * @param <O> 写出类型
 */
public interface TransferPlugin<F,O> extends CheckPointable,TRAFlow<F> {
	/**
	 * 处理转存数据
	 * @param transferToSourceChannel 转存通道
	 * @description 
	 * 转存读入数据可以来自于本地文件或网络Socket
	 * 转存写出数据可以写入自定义转存文件或参数转存通道
	 * @return 对象
	 */
	public abstract Object transfer(Channel<O> transferToSourceChannel) throws Exception;
}
