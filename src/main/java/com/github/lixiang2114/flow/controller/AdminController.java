package com.github.lixiang2114.flow.controller;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.lixiang2114.flow.comps.PluginType;
import com.github.lixiang2114.flow.context.Context;
import com.github.lixiang2114.flow.scheduler.SchedulerPool;
import com.github.lixiang2114.flow.service.ETLService;
import com.github.lixiang2114.flow.service.TRAService;
import com.github.lixiang2114.flow.service.impl.BaseService;

/**
 * @author Lixiang
 * @description 运维管理控制台
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
	/**
	 * TRA服务接口
	 */
	@Autowired
	private TRAService traService;
	
	/**
	 * ETL服务接口
	 */
	@Autowired
	private ETLService etlService;
	
	/**
	 * 日志工具
	 */
	private static final Logger log=LoggerFactory.getLogger(AdminController.class);
	
	@PostConstruct
	public void init() throws Exception {
		Context.loadAppContext();
		if(Context.initOnStart) {
			initAllFlows();
		}else{
			log.info("initOnStart is: "+Context.initOnStart+",all flows will not be initialization...");
		}
	}
	
	@RequestMapping(path="/initallflow")
    public void initAllFlows() throws Exception {
		log.info("initialization flow context start...");
		if(Context.flowsLoaded) {
			log.info("all flows is already loaded...");
			return;
		}
		
		//装载所有流程
		loadAllFlows();
		
		//启动所有流程
		startAllFlows();
		
		//设置初始化开关
		Context.flowsLoaded=true;
		log.info("initialization flow context complete...");
	}
	
	/**
	 * 装载所有流程实例
	 * @throws Exception
	 */
	@RequestMapping(path="/loadallflow")
    public void loadAllFlows() throws Exception {
		Context.loadFlowContext("load flow context start...","load flow context complete...");
		log.info("load all flow instance start...");
		for(String flowName:Context.flowList) Context.loadFlow(flowName);
		log.info("load all flow instance complete...");
		log.info("Current Thread Pool Arguments Are: {}",SchedulerPool.getPoolParams());
	}
	
	/**
	 * 关闭系统进程
	 * @throws Exception
	 */
	@RequestMapping(path="/shutdown")
    public void shudown() throws Exception {
		log.info("Shutdown DataFlow Server...");
		Runtime.getRuntime().exit(0);
	}
	
	/**
	 * dump出系统线程池参数
	 * @return 线程池参数字典表
	 * @throws Exception
	 */
	@RequestMapping(path="/dumpPoolArgs")
    public Map<String,Object> dumpPoolParams() throws Exception {
		return SchedulerPool.getPoolParams();
	}
	
	/**
	 * 启动指定的完整流程
	 * @param flowname 流程名称
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/startflow/{flowname}")
    public String startFlow(@PathVariable("flowname") String flowname) throws Exception {
		if(!BaseService.ensureFlowExists(flowname)) return "specify flow: "+flowname+" is not exists!!!";
		StringBuilder retBuilder=new StringBuilder(startETL(flowname)).append("\n");
		if(Context.getFlow(flowname).hasTransfer) retBuilder.append(startTRA(flowname)).append("\n");
		return retBuilder.toString();
	}
	
	/**
	 * 停止指定的完整流程
	 * @param flowname 流程名称
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stopflow/{flowname}")
    public String stopFlow(@PathVariable("flowname") String flowname) throws Exception {
		if(!BaseService.ensureFlowExists(flowname)) return "specify flow: "+flowname+" is not exists!!!";
		StringBuilder retBuilder=new StringBuilder(stopETL(flowname)).append("\n");
		if(Context.getFlow(flowname).hasTransfer) retBuilder.append(stopTRA(flowname)).append("\n");
		return retBuilder.toString();
	}
	
	/**
	 * 启动所有的完整流程
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/startflowall")
    public String startAllFlows() throws Exception {
		StringBuilder retBuilder=new StringBuilder(startAllETLs()).append("\n");
		retBuilder.append(startAllTRAs()).append("\n");
		return retBuilder.toString();
	}
	
	/**
	 * 停止所有的完整流程
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stopflowall")
    public String stopAllFlows() throws Exception {
		StringBuilder retBuilder=new StringBuilder(stopAllTRAs()).append("\n");
		retBuilder.append(stopAllETLs()).append("\n");
		return retBuilder.toString();
	}
	
	/**
	 * 启动指定流程的ETL进程组
	 * @param flowname 流程名称
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/startetl/{flowname}")
    public String startETL(@PathVariable("flowname") String flowname) throws Exception {
		if(BaseService.ensureFlowExists(flowname)) return etlService.startETLProcess(flowname);
		log.error("unable to load this flow: {}, may be not defined...",flowname);
		return "unable to load this flow instance: "+flowname+", may be not defined...";
	}
	
	/**
	 * 停止指定流程的ETL进程组
	 * @param flowname 流程名称
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stopetl/{flowname}")
    public String stopETL(@PathVariable("flowname") String flowname) throws Exception {
		return etlService.stopETLProcess(flowname);
	}
	
	/**
	 * 启动指定流程的TRA进程组
	 * @param flowname 流程名称
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/starttra/{flowname}")
    public String startTRA(@PathVariable("flowname") String flowname) throws Exception {
		if(BaseService.ensureFlowExists(flowname)) return traService.startTRAProcess(flowname);
		log.error("unable to load this flow: {}, may be not defined...",flowname);
		return "unable to load this flow instance: "+flowname+", may be not defined...";
	}
	
	/**
	 * 停止指定流程的TRA进程组
	 * @param flowname 流程名称
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stoptra/{flowname}")
    public String stopTRA(@PathVariable("flowname") String flowname) throws Exception {
		return traService.stopTRAProcess(flowname);
	}
	
	/**
	 * 启动所有流程的ETL进程组
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/startetlall")
    public String startAllETLs() throws Exception {
		log.info("loop start each etl flow...");
		return etlService.startAllETLProcess();
	}
	
	/**
	 * 停止所有流程的ETL进程组
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stopetlall")
    public String stopAllETLs() throws Exception {
		return etlService.stopAllETLProcess();
	}
	
	/**
	 * 启动所有流程的TRA进程组
	 * @return 启动信息
	 * @throws Exception
	 */
	@RequestMapping(path="/starttraall")
    public String startAllTRAs() throws Exception {
		log.info("loop start each tra flow...");
		return traService.startAllTRAProcess();
	}
	
	/**
	 * 停止所有流程的TRA进程组
	 * @return 停止信息
	 * @throws Exception
	 */
	@RequestMapping(path="/stoptraall")
    public String stopAllTRAs() throws Exception {
		return traService.stopAllTRAProcess();
	}
	
	/**
	 * 执行指定流程的ETL检查点
	 * @param flowName 流程名称
	 * @return 检查点提示信息
	 * @throws Exception
	 */
	@RequestMapping(path="/etlcheckpoint")
    public Object etlCheckpoint(String flowName) throws Exception {
		return etlService.refleshETLCheckpoint(flowName);
	}
	
	/**
	 * 执行所有流程的ETL检查点
	 * @return 检查点提示信息
	 * @throws Exception
	 */
	@RequestMapping(path="/etlcheckpointall")
    public Object etlCheckpointAll() throws Exception {
		return etlService.refleshAllETLCheckpoint();
	}
	
	/**
	 * 执行指定流程的TRA检查点
	 * @param flowName 流程名称
	 * @return 检查点提示信息
	 * @throws Exception
	 */
	@RequestMapping(path="/tracheckpoint")
    public Object traCheckpoint(String flowName) throws Exception {
		return traService.refleshTRACheckpoint(flowName);
	}
	
	/**
	 * 执行所有流程的TRA检查点
	 * @return 检查点提示信息
	 * @throws Exception
	 */
	@RequestMapping(path="/tracheckpointall")
    public Object traCheckpointAll() throws Exception {
		return traService.refleshAllTRACheckpoint();
	}
	
	/**
	 * 显示指定插件的参数状态信息
	 * @param flowname 流程名称
	 * @param plugintype 插件类型
	 * @param name 参数名
	 * @param value 参数值
	 * @return 参数状态信息
	 * @throws Exception
	 */
	@RequestMapping(path="/{flowname}/{plugintype}")
    public Object status(@PathVariable("flowname") String flowname,@PathVariable("plugintype") String plugintype,String name,String value) throws Exception {
		if(null==name && null==value) return BaseService.status(flowname, PluginType.valueOf(plugintype));
		if(null!=name && null==value) return BaseService.status(flowname, PluginType.valueOf(plugintype),name.trim());
		if(null!=name && null!=value) return BaseService.status(flowname, PluginType.valueOf(plugintype),name.trim(),value.trim());
		return null;
	}
	
	/**
	 * 通用透传服务接口
	 * @param flowname 流程名称
	 * @param plugintype 插件类型名
	 * @param facename 插件接口方法名
	 * @param params 参数表(使用英文逗号分隔参数值)
	 * @return 插件调用结果信息
	 */
	@RequestMapping(path="/{flowname}/{plugintype}/{facename}")
    public String through(@PathVariable("flowname") String flowname,@PathVariable("plugintype") String plugintype,@PathVariable("facename") String facename,String params) {
		return BaseService.throughService(flowname,PluginType.valueOf(plugintype),facename,params).toString();
	}
}
