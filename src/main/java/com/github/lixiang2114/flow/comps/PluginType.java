package com.github.lixiang2114.flow.comps;

import com.github.lixiang2114.flow.context.Context;

/**
 * @author Lixiang
 * @description 插件类型
 */
public enum PluginType {
	/**
	 * Sink插件类型
	 */
	sink("sink"),
	
	/**
	 * Filter插件类型
	 */
	filter("filter"),
	
	/**
	 * Source插件类型
	 */
	source("source"),
	
	/**
	 * Transfer插件类型
	 */
	transfer("transfer"),
	
	/**
	 * Manual插件类型
	 */
	manual("manual","source"),
	
	/**
	 * Realtime插件类型
	 */
	realtime("realtime","source");
	
	/**
	 * 插件类型名称
	 */
	public String name;
	
	/**
	 * 插件超类型名称
	 */
	public String supName;
	
	/**
	 * 插件接口类型
	 */
	public Class<?> faceType;
	
	/**
	 * 插件超接口类型
	 */
	public Class<?> supFaceType;
	
	/**
	 * 插件超类型
	 * @return 插件超类型
	 */
	public PluginType superType(){
		if(null==supName) return null;
		return PluginType.valueOf(supName);
	}
	
	/**
	 * 参数类型是否为当前类型的超类型
	 * @param superType 超类型
	 * @return 是否为当前类型的超类型
	 */
	public Boolean typeFrom(PluginType superType){
		if(null==superType) return false;
		return superType==superType();
	}
	
	private PluginType(String typeName){
		this.name=typeName;
		this.faceType=Context.PLUG_TYPE_TO_FACE.get(typeName);
	}
	
	private PluginType(String typeName,String supTypeName){
		this.name=typeName;
		this.supName=supTypeName;
		this.faceType=Context.PLUG_TYPE_TO_FACE.get(typeName);
		this.supFaceType=Context.PLUG_TYPE_TO_FACE.get(supTypeName);
	}
}
