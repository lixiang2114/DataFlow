package com.github.lixiang2114.flow.comps;

import java.io.File;

import com.github.lixiang2114.flow.context.Context;

/**
 * @author Lixiang
 * @description 组件总接口
 */
public abstract class Comp {
	/**
	 * 组件实例位置
	 */
	public File compPath;
	
	/**
	 * 组件实例名称
	 */
	public String compName;
	
	/**
	 * 组件实例类型
	 */
	public CompType compType;
	
	public Comp(){}
	
	public Comp(CompType compType,String compName){
		this.compType=compType;
		this.compName=compName;
		this.compPath=new File(Context.projectFile,compType+"/"+compName);
	}
}
