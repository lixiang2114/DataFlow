package com.github.lixiang2114.flow.handler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;

/**
 * @author Lixiang
 * @description 流程Filter插件操作器
 */
public class FilterHandler implements Callable<Object>{
	/**
	 * 流程对象
	 */
	private Flow flow;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(FilterHandler.class);
	
	public FilterHandler(Flow flow){
		this.flow=flow;
	}

	@Override
	public Object call() throws Exception {
		if(null==flow.filter) return null;
		try {
			Boolean flag=(Boolean)flow.filter.callFace("start",flow);
			if(null==flag || !flag) {
				log.warn("filter plugin: {} invoke start method failure!!!",flow.filter.compName);
				return flag;
			}
			return flow.filter.callFace("filter",flow.sourceToFilterChannel,flow.filterToSinkChannel);
		} catch (Exception e) {
			log.error("call filter plugin occur error: ",e);
			return null;
		}
	}
}
