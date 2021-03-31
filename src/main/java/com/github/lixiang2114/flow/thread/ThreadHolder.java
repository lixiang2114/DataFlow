package com.github.lixiang2114.flow.thread;

import java.util.HashMap;

/**
 * @author Louis
 * @description 线程操作工具类
 */
@SuppressWarnings("unchecked")
public class ThreadHolder {
	/**
	 * 线程句柄
	 */
	private static final InheritableThreadLocal<HashMap<String,Object>> CONTEXT_HOLDER = new InheritableThreadLocal<HashMap<String,Object>>();
	
	/**
	 * 获取线程中指定键映射的值
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 对象值
	 */
	public static Object peek(String key,Object... defaultValue) {
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		Object value=threadDict.get(key);
		if(null!=value) return value;
		return null==defaultValue || 0==defaultValue.length? null:defaultValue[0];
	}
	
	/**
	 * 获取线程中指定键映射的值
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R peek(String key,Class<R> returnType,R... defaultValue) {
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		Object value=threadDict.get(key);
		if(null!=value) return returnType.cast(value);
		return null==defaultValue || 0==defaultValue.length? null:defaultValue[0];
	}
	
	/**
	 * 将参数键值对设置到线程中去
	 * @param key 键
	 * @param value 值
	 */
	public static void push(String key,Object value) {
		if(null==key || null==value) return;
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		threadDict.put(key,value);
	}
	
	/**
	 * 获取并移除线程中指定键的映射
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 对象值
	 */
	public static Object poll(String key,Object... defaultValue) {
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		Object value=threadDict.remove(key);
		if(null!=value) return value;
		return null==defaultValue || 0==defaultValue.length? null:defaultValue[0];
	}
	
	/**
	 * 获取并移除线程中指定键的映射
	 * @param key 键
	 * @param returnType 返回类型
	 * @param defaultValue 默认值
	 * @return 泛化类型
	 */
	public static <R> R poll(String key,Class<R> returnType,R... defaultValue) {
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		Object value=threadDict.remove(key);
		if(null!=value) return returnType.cast(value);
		return null==defaultValue || 0==defaultValue.length? null:defaultValue[0];
	}
	
	/**
	 * 清空线程中所有的键值对
	 */
	public static void clear() {
		HashMap<String,Object> threadDict=CONTEXT_HOLDER.get();
		if(null==threadDict)CONTEXT_HOLDER.set(threadDict=new HashMap<String,Object>());
		threadDict.clear();
	}
}
