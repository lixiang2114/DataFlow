package com.github.lixiang2114.flow.context;

/**
 * @author Lixiang
 * @description 计算机字节容量换算单位
 */
public enum SizeUnit {
	B("B",1L),
	
	K("KB",1024L),
	
	M("MB",1024*1024L),
	
	G("GB",1024*1024*1024L),
	
	T("TB",1024*1024*1024*1024L),
	
	P("PB",1024*1024*1024*1024*1024L),
	
	E("EB",1024*1024*1024*1024*1024*1024L),
	
	Z("ZB",1024*1024*1024*1024*1024*1024*1024L),
	;
	
	/**
	 * 进制
	 */
	public Long radix;
	
	/**
	 * 名称
	 */
	public String name;
	
	private SizeUnit(String name,Long radix){
		this.radix=radix;
		this.name=name;
	}
	
	public static Long getBytes(Long value,String name){
		if(null==value) return null;
		if(0==value) return 0L;
		Long radix=getBytes(name);
		if(null==radix) return null;
		return value*radix;
	}
	
	public static Long getBytes(String name){
		SizeUnit target=getUnit(name);
		if(null==target) return null;
		return target.radix;
	}
	
	public static SizeUnit getUnit(String name){
		for(SizeUnit unit:SizeUnit.values()) if(unit.name.equalsIgnoreCase(name) || unit.name().equalsIgnoreCase(name)) return unit;
		return null;
	}
}
