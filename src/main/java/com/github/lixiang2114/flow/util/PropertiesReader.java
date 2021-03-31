package com.github.lixiang2114.flow.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * @author Lixiang
 * @description 配置配置文件
 */
public class PropertiesReader {
	/**
	 * 路径解析器
	 * 配置文件(*.yml|*.properties)
	 */
	private static ResourcePatternResolver pathResolver=new PathMatchingResourcePatternResolver();
	
	/**
	 * 字母序列
	 */
	private static final String LETTERS="abcdefghijklmnopqrstuvwxyz"+"abcdefghijklmnopqrstuvwxyz".toUpperCase();
	
	/**
	 * 读取属性配置文件
	 * @param absolutePath 配置文件绝对路径
	 * @return 属性字典
	 */
	public static Properties getPropertiesByRealPath(String absolutePath){
		return getProperties(new File(absolutePath));
	}
	
	/**
	 * 读取属性配置文件
	 * @param fileUrl 配置文件路径
	 * @return 属性字典
	 */
	public static Properties getProperties(URL fileUrl){
		if(!ResourceUtils.isFileURL(fileUrl)) return null;
		return getProperties(new FileUrlResource(fileUrl));
	}
	
	/**
	 * 读取属性配置文件
	 * @param fileUri 配置文件路径
	 * @return 属性字典
	 */
	public static Properties getProperties(URI fileUri){
		URL url=null;
		try {
			url = fileUri.toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		if(!ResourceUtils.isFileURL(url)) return null;
		return getProperties(new FileUrlResource(url));
	}
	
	/**
	 * 读取属性配置文件
	 * @param confFile 配置文件
	 * @return 属性字典
	 */
	public static Properties getProperties(File confFile){
		if(!confFile.exists() || confFile.isDirectory()) return null;
		
		URL url=null;
		try {
			url = confFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		if(!ResourceUtils.isFileURL(url)) return null;
		return getProperties(new FileUrlResource(url));
	}
	
	/**
	 * 判断是否为绝对路径
	 * @param resourcePath 资源路径
	 * @return 是否为绝对路径
	 */
	public static boolean isAbsolutePath(String resourcePath){
		if(null==resourcePath || resourcePath.trim().isEmpty()) return false;
		char firstChar=resourcePath.charAt(0);
		if(firstChar=='/' || firstChar=='\\' || firstChar==File.separatorChar) return true;
		int count=LETTERS.length();
		for(int i=0;i<count;i++) if(resourcePath.startsWith(LETTERS.charAt(i)+":")) return true;
		return false;
	}
	
	/**
	 * 根据可选的资源路径获取资源描述
	 * @param resourceURI 资源路径
	 * @return 资源描述
	 */
	public static Resource getResource(String... resourceURI){
		Resource resource=null;
		if(null==resourceURI || 0==resourceURI.length || null==resourceURI[0] || resourceURI[0].trim().isEmpty()) {
			resource = pathResolver.getResource("classpath:application.yml");
			if(null==resource || !resource.exists()) resource = pathResolver.getResource("classpath:application.yaml");
			if(null==resource || !resource.exists()) resource = pathResolver.getResource("classpath:application.properties");
		} else {
			String resourcePath=resourceURI[0].trim();
			if(resourcePath.startsWith("classpath:") || resourcePath.startsWith("classpath*:") || resourcePath.startsWith("file:")){
				resource = pathResolver.getResource(resourcePath);
			}else if(isAbsolutePath(resourcePath)){
				resource = pathResolver.getResource("file:"+resourcePath);
			}else{
				resource = pathResolver.getResource(resourcePath);
			}
		}
		if(null==resource || !resource.exists()) return null;
		return resource;
	}
	
	/**
	 * 从资源配置中获取属性字典表
	 * @param resourceURI 资源路径
	 * @return 属性字典
	 */
	public static Properties getProperties(String... resourceURI){
		Resource resource=getResource(resourceURI);
		if(null==resource) return null;
		return getProperties(resource);
	}
	
	/**
	 * 从资源配置中获取属性字典表
	 * @param resource 资源描述
	 * @return 属性字典
	 */
	public static Properties getProperties(Resource resource){
		String fileName = resource.getFilename();
		if(null==fileName || fileName.trim().isEmpty()) return null;
		Properties properties = null;
		if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
			YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties=yamlFactory.getObject();
		}else{
			try {
				properties=PropertiesLoaderUtils.loadProperties(resource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	/**
	 * 从资源路径指向的配置文件中获取指定键的值
	 * @param key 键
	 * @param defaultValue 默认值
	 * @param resourceURI 资源路径
	 * @return 值
	 */
	public static String getProperty(String key,String defaultValue,String... resourceURI){
		Resource resource=getResource(resourceURI);
		if(null==resource) return null;
		Properties properties=getProperties(resource);
		if(null==properties) return null;
		String value = properties.getProperty(key);
        return null==value?defaultValue:value;
    }
	
	/**
	 * 从资源路径指向的配置文件中获取指定键的值
	 * @param key 键
	 * @param resourceURI 资源路径
	 * @return 值
	 */
	public static String getProperty(String key,String... resourceURI){
		return getProperty(key,null,resourceURI);
	}
}
