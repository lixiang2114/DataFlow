package com.github.lixiang2114.flow.context;

/**
 * @author Lixiang
 * @description 提供默认常量值
 */
public class Default {
	/**
	 *  ETL流程模式(true:实时,false:离线)
	 */
	public static final String ETL_MODE="true";
	
	/**
	 *  是否清除流程实例中缓存的插件实例配置信息
	 */
	public static final String CLEAR_CACHE="false";
	
	/**
	 * 流程初始化模式(true:启动时初始化,false:运行时初始化)
	 */
	public static final String INIT_ON_START="true";
	
	/**
	 * 默认Sink插件
	 */
	public static final String SINK="fileSink";
	
	/**
	 * 默认Filter过滤器
	 */
	public static final String Filter="defaultFilter";
	
	/**
	 * 默认Manual插件
	 */
	public static final String MANUAL="fileManual";
	
	/**
	 * 默认Realtime插件
	 */
	public static final String REALTIME="fileRealtime";
	
	/**
	 * 默认Transfer插件
	 */
	public static final String TRANSFER="fileTransfer";
	
	/**
	 * 默认的ETL流程列表(多个流程名称间使用英文逗号分隔)
	 */
	public static final String FLOWS="fileToFile";

	/**
	 *  启动流程调度器(true:启动,false:停止)
	 */
	public static final String FLOW_SCHEDULER="true";
	
	/**
	 *  是否需要检查插件实现接口
	 */
	public static final String CHECK_PLUGIN_FACE="false";
	
	/**
	 * 流程通道最大尺寸
	 */
	public static final String CHANNEL_MAX_SIZE="20000";
	
	/**
	 *  ETL流程调度间隔时间(单位:毫秒)
	 */
	public static final String ETL_SCHEDULER_INTERVAL="2000";
	
	/**
	 *  转存流程调度间隔时间(单位:毫秒)
	 */
	public static final String TRA_SCHEDULER_INTERVAL="1000";
}
