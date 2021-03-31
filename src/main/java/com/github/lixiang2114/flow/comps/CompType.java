package com.github.lixiang2114.flow.comps;

/**
 * @author Lixiang
 * @description 组件类型
 */
public enum CompType {
	/**
	 * Flow组件类型
	 */
	flows("flows"),
	
	/**
	 * Plugins组件类型
	 */
	plugins("plugins");
	
	/**
	 * 插件类型名称
	 */
	public String name;
	
	private CompType(String compName){
		this.name=compName;
	}
}
