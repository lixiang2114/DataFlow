package com.github.lixiang2114.flow.plugins.adapter;

import java.io.File;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.plugins.face.AbstractPlugin;
import com.github.lixiang2114.flow.plugins.face.RealtimePlugin;

/**
 * @author Lixiang
 * @description 实时收集器插件适配器
 */
public abstract class RealtimePluginAdapter extends AbstractPlugin implements RealtimePlugin<Flow,String,String>{
	/**
	 * 插件运行时路径
	 */
	protected File pluginPath;

	@Override
	public Boolean config() throws Exception {
		this.pluginPath=flow.sourcePath;
		return init();
	}
	
	public Boolean init() throws Exception{
		return true;
	}
	
	@Override
	public Object stop(Object params) throws Exception {
		flow.sourceStart=false;
		return true;
	}
}
