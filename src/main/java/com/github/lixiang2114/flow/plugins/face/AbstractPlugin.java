package com.github.lixiang2114.flow.plugins.face;

import java.io.File;

import com.github.lixiang2114.flow.comps.Flow;
import com.github.lixiang2114.flow.plugins.CheckPointable;
import com.github.lixiang2114.flow.plugins.Configurable;

/**
 * @author Lixiang
 * @description 抽象插件
 */
public abstract class AbstractPlugin implements CheckPointable,Configurable,Procedure<Flow>{
	/**
	 * 流程实例对象
	 */
	protected Flow flow;
	
	/**
	 * 流程实例插件共享路径
	 */
	protected File sharePath;
	
	@Override
	public Boolean start(Flow flow) throws Exception {
		this.flow=flow;
		sharePath=flow.sharePath;
		return config();
	}
	
	@Override
	public Object stop(Object params) throws Exception {
		return null;
	}

	@Override
	public Object config(Object... params) throws Exception {
		return null;
	}
	
	@Override
	public Object checkPoint(Object params) throws Exception {
		return null;
	}
	
	public abstract Boolean config() throws Exception;
}
