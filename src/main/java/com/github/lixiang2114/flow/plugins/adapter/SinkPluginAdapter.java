package com.github.lixiang2114.flow.plugins.adapter;

import java.io.File;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.plugins.face.AbstractPlugin;
import com.github.lixiang2114.flow.plugins.face.SinkPlugin;

/**
 * @author Lixiang
 * @description 发送器插件适配器
 */
public abstract class SinkPluginAdapter extends AbstractPlugin implements SinkPlugin<Flow,String>{
	/**
	 * 插件运行时路径
	 */
	protected File pluginPath;

	@Override
	public Boolean config() throws Exception {
		this.pluginPath=flow.sinkPath;
		return init();
	}
	
	public Boolean init() throws Exception{
		return true;
	}
	
	@Override
	public Object stop(Object params) throws Exception {
		flow.sinkStart=false;
		return true;
	}
}
