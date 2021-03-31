package com.github.lixiang2114.flow.service;

/**
 * @author Lixiang
 * @description ETL流程服务接口
 */
public interface ETLService {
	/**
	 * 停止所有ETL流程
	 * @return 对象
	 */
	public String stopAllETLProcess() throws Exception;
	
	/**
	 * 启动所有ETL流程
	 * @return 对象
	 */
	public String startAllETLProcess() throws Exception;
	
	/**
	 * 刷新所有ETL流程检查点
	 * @return 对象
	 */
	public String refleshAllETLCheckpoint() throws Exception;
	
	/**
	 * 停止ETL流程
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public String stopETLProcess(String flowName) throws Exception;
	
	/**
	 * 启动ETL流程
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public String startETLProcess(String flowName) throws Exception;
	
	/**
	 * 刷新指定ETL流程检查点
	 * @param flowName 流程名称
	 * @return 对象
	 */
	public Object refleshETLCheckpoint(String flowName) throws Exception;
}
