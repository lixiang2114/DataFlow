package com.github.lixiang2114.flow.handler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;

/**
 * @author Lixiang
 * @description 流程Source插件操作器
 */
public class SourceHandler implements Callable<Object>{
	/**
	 * 流程对象
	 */
	private Flow flow;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(SourceHandler.class);
	
	public SourceHandler(Flow flow){
		this.flow=flow;
	}

	@Override
	public Object call() throws Exception {
		if(null==flow.source) return null;
		try {
			Boolean flag=(Boolean)flow.source.callFace("start",flow);
			if(null==flag || !flag) {
				log.warn("source plugin: {} invoke start method failure!!!",flow.source.compName);
				return flag;
			}
			
			if(!flow.realTime) {
				log.info("call manual plugin face from: {}...",flow.source.compName);
				return flow.source.callFace("handle",flow.sourceToFilterChannel);
			}else{
				log.info("call realtime plugin face from: {}...",flow.source.compName);
				return flow.source.callFace("handle",flow.transferToSourceChannel,flow.sourceToFilterChannel);
			}
		} catch (Exception e) {
			log.error("call source plugin process occur error: ",e);
			return null;
		}
	}
}
