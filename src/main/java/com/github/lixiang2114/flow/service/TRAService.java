package com.github.lixiang2114.flow.service;

/**
 * @author Lixiang
 * @description TRA流程服务接口
 */
public interface TRAService {
	/**
	 * 停止所有TRA流程
	 * @return 对象
	 */
	public String stopAllTRAProcess() throws Exception;
	
	/**
	 * 启动所有TRA流程
	 * @return 对象
	 */
	public String startAllTRAProcess() throws Exception;
	
	/**
	 * 刷新所有TRA流程检查点
	 * @return 对象
	 */
	public String refleshAllTRACheckpoint() throws Exception;
	
	/**
	 * 停止TRA流程
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public String stopTRAProcess(String flowName) throws Exception;
	
	/**
	 * 启动TRA流程
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public String startTRAProcess(String flowName) throws Exception;
	
	/**
	 * 刷新指定TRA流程检查点
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public Object refleshTRACheckpoint(String flowName) throws Exception;
}
