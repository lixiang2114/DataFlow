package com.github.lixiang2114.flow.handler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lixiang2114.flow.comps.Flow;

/**
 * @author Lixiang
 * @description 流程Transfer插件操作器
 */
public class TransferHandler implements Callable<Object>{
	/**
	 * 流程对象
	 */
	private Flow flow;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(TransferHandler.class);
	
	public TransferHandler(Flow flow){
		this.flow=flow;
	}

	@Override
	public Object call() throws Exception {
		if(null==flow.transfer) return null;
		try {
			Boolean flag=(Boolean)flow.transfer.callFace("start",flow);
			if(null==flag || !flag) {
				log.warn("transfer plugin: {} invoke start method failure!!!",flow.transfer.compName);
				return flag;
			}
			return flow.transfer.callFace("transfer",flow.transferToSourceChannel);
		} catch (Exception e) {
			log.error("call transfer plugin process occur error: ",e);
			return null;
		}
	}
}
