package com.github.lixiang2114.flow.plugins.adapter;

import java.io.File;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.plugins.face.AbstractPlugin;
import com.github.lixiang2114.flow.plugins.face.TransferPlugin;

/**
 * @author Lixiang
 * @description 转换器插件适配器
 */
public abstract class TransferPluginAdapter extends AbstractPlugin implements TransferPlugin<Flow,String>{
	/**
	 * 插件运行时路径
	 */
	protected File pluginPath;

	@Override
	public Boolean config() throws Exception {
		this.pluginPath=flow.transferPath;
		return init();
	}
	
	public Boolean init() throws Exception{
		return true;
	}
	
	@Override
	public Object stop(Object params) throws Exception {
		flow.transferStart=false;
		return true;
	}
}
