package com.github.lixiang2114.flow.handler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;

/**
 * @author Lixiang
 * @description 流程Sink插件操作器
 */
public class SinkHandler implements Callable<Object>{
	/**
	 * 流程对象
	 */
	private Flow flow;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(SinkHandler.class);
	
	public SinkHandler(){}
	
	public SinkHandler(Flow flow){
		this.flow=flow;
	}

	@Override
	public Object call() throws Exception {
		if(null==flow.sink) return null;
		try {
			Boolean flag=(Boolean)flow.sink.callFace("start",flow);
			if(null==flag || !flag) {
				log.warn("sink plugin: {} invoke start method failure!!!",flow.sink.compName);
				return flag;
			}
			return flow.sink.callFace("send",flow.filterToSinkChannel);
		} catch (Exception e) {
			log.error("call sink plugin occur error: ",e);
			return null;
		}
	}
}
