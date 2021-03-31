package com.github.lixiang2114.flow.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTool;

/**
 * @author Lixiang
 * @description 通用类装载器工具
 */
public class ClassLoaderUtil{
	/**
	 * 添加文件到类路径方法
	 */
	private static Method addClassPathMethod;
	
	static{
		try {
			addClassPathMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addClassPathMethod.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 校正类路径
	 * @param classPath 类路径
	 * @return 类路径
	 */
	public static String fixClassPath(String classPath){
		if(null==classPath || classPath.trim().isEmpty()) return null;
		String classPathFile=classPath.trim();
		if(classPathFile.startsWith("\\")){
			classPathFile="/"+classPathFile.substring(1);
		}else if(classPathFile.startsWith("./")||classPathFile.startsWith(".\\")){
			classPathFile="/"+classPathFile.substring(2);
		}else if(!classPathFile.startsWith("/")){
			classPathFile="/"+classPathFile;
		}
		return classPathFile;
	}
	
	/**
	 * 获取指定绝对路径文件的输入流
	 * @param absolutePathfile 绝对路径文件
	 * @return 指向绝对路径文件的输入流
	 */
	public static InputStream getAbsolutePathFileStream(String absolutePathfile){
		File file=new File(absolutePathfile);
		if(!file.exists()||file.isDirectory()) return null;
		try {
			return file.toURI().toURL().openStream();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 获取类文件对应的真实文件
	 * @param classPath 类路径文件
	 * @return 指向绝对路径的真实文件
	 */
	public static File getRealFile(String classPath){
		String fileFullPath=getRealPath(classPath);
		if(null==fileFullPath) return null;
		return new File(fileFullPath);
	}
	
	/**
	 * 获取类文件对应的真实路径
	 * @param classPath 类路径文件
	 * @return 指向绝对路径的真实文件
	 */
	public static String getRealPath(String classPath){
		String fileClassPath=fixClassPath(classPath);
		if(null==fileClassPath) return null;
		Class<?> caller=getCallerClass();
		if(null==caller) return null;
		return caller.getResource(fileClassPath).getPath();
	}
	
	/**
	 * 获取指定类文件的输入流
	 * @param classPathfile 类路径文件
	 * @return 指向类路径文件的输入流
	 * @description 按类路径的先后顺序从左到右依次查找文件名,一旦找到则停止搜索类路径
	 */
	public static InputStream getClassPathFileStream(String classPathfile){
		String fileClassPath=fixClassPath(classPathfile);
		if(null==fileClassPath) return null;
		Class<?> caller=getCallerClass();
		if(null==caller) return null;
		return caller.getResourceAsStream(fileClassPath);
	}
	
	/**
	 * 获取当前类的类装载器(该方法通常在本类的外部调用)
	 * @return 类装载器
	 */
	public static ClassLoader getCurrentClassLoader(){
		return getCallerClassLoader();
	}
	
	/**
	 * 获取当前类的调用类的类装载器(该方法通常在本类的外部调用)
	 * @return 类装载器
	 */
	public static ClassLoader getCurrentCallerClassLoader(){
		Class<?> currentClass=getCallerClass();
		return getCallerClassLoader(currentClass);
	}
	
	/**
	 * 获取可选参数类的调用类
	 * @param currentClass 可选的基准参数类
	 * @return 类装载器
	 */
	public static ClassLoader getCallerClassLoader(Class<?>... currentClass){
		Class<?> caller=getCallerClass(currentClass);
		return null==caller?null:caller.getClassLoader();
	}
	
	/**
	 * 获取可选参数类(默认为GenericClassLoader)的调用类
	 * @param currentClass 参数类(不能是Thread类)
	 * @return 调用者类(不能是Thread类和GenericClassLoader类)
	 */
	public static Class<?> getCallerClass(Class<?>... currentClass){
		Class<?> type=(null==currentClass||0==currentClass.length)?ClassLoaderUtil.class:currentClass[0];
		StackTraceElement[] elements=Thread.currentThread().getStackTrace();
		String currentClassName=type.getName();
		
		int startIndex=-1;
		for(int i=0;i<elements.length;i++){
			if(!currentClassName.equals(elements[i].getClassName())) continue;
			startIndex=i;
			break;
		}
		
		if(-1==startIndex) return null;
		
		String callerClassName=null;
		for(int i=startIndex+1;i<elements.length;i++){
			String iteClassName=elements[i].getClassName();
			if(currentClassName.equals(iteClassName)) continue;
			callerClassName=iteClassName;
			break;
		}
		
		if(null==callerClassName) return null;
		
		try {
			return Class.forName(callerClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * 获取当前类的类装载器的classpath中的路径集合
	 * @param type 参考类
	 * @return URL路径集合
	 * @throws IOException IO异常
	 * @description 按类路径先后顺序从左到右加载并包装成URL数组返回
	 */
	public static URL[] getCurrentClassPath(Class<?>... type) throws IOException{
		ClassLoader classLoader=getCallerClassLoader(type);
		if(null==classLoader) return null;
		return ((URLClassLoader)classLoader).getURLs();
	}
	
	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param fullPath 目录或jar文件的绝对路径
	 * @param type 类装载器的参考类
	 */
	public static void addFileToCurrentClassPath(String fullPath,Class<?>... type) {
		addCurrentClassPath(new File(fullPath),false,type);
	}
	
	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param url 目录或jar文件的URL路径
	 * @param type 类装载器的参考类
	 */
	public static void addFileToCurrentClassPath(URL url,Class<?>... type) {
		File file=null;
		try {
			file=new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if(null==file) return;
		addCurrentClassPath(file,false,type);
	}
	
	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param uri 目录或jar文件的URI路径
	 * @param type 类装载器的参考类
	 */
	public static void addFileToCurrentClassPath(URI uri,Class<?>... type) {
		addCurrentClassPath(new File(uri),false,type);
	}
	
	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param file 目录或jar文件对象
	 * @param type 类装载器的参考类
	 */
	public static void addFileToCurrentClassPath(File file,Class<?>... type) {
		addCurrentClassPath(file,false,type);
	}
	
	/**
	 * 递归添加参数目录(目录或jar文件)到当前类路径
	 * @param fullPath 目录或jar文件的绝对路径
	 * @param type 类装载器的参考类
	 * @description 
	 * 装载指定的参数jar文件或从装载参数目录开始递归装载所有子目录及其jar文件
	 * 注意装载到类路径的都是目录而不是文件,而jar文件则被视为一种特殊的目录被装载到类路径,所以除jar文件以外的普通文件是不能被装载到类路径的
	 */
	public static void addCycleFileToCurrentClassPath(String fullPath,Class<?>... type) {
		addCurrentClassPath(new File(fullPath),true,type);
	}
	
	/**
	 * 递归添加参数目录(目录或jar文件)到当前类路径
	 * @param url 目录或jar文件的URL路径
	 * @param type 类装载器的参考类
	 * @description 
	 * 装载指定的参数jar文件或从装载参数目录开始递归装载所有子目录及其jar文件
	 * 注意装载到类路径的都是目录而不是文件,而jar文件则被视为一种特殊的目录被装载到类路径,所以除jar文件以外的普通文件是不能被装载到类路径的
	 */
	public static void addCycleFileToCurrentClassPath(URL url,Class<?>... type) {
		File file=null;
		try {
			file=new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if(null==file) return;
		addCurrentClassPath(file,true,type);
	}
	
	/**
	 * 递归添加参数目录(目录或jar文件)到当前类路径
	 * @param uri 目录或jar文件的URI路径
	 * @param type 类装载器的参考类
	 * @description 
	 * 装载指定的参数jar文件或从装载参数目录开始递归装载所有子目录及其jar文件
	 * 注意装载到类路径的都是目录而不是文件,而jar文件则被视为一种特殊的目录被装载到类路径,所以除jar文件以外的普通文件是不能被装载到类路径的
	 */
	public static void addCycleFileToCurrentClassPath(URI uri,Class<?>... type) {
		addCurrentClassPath(new File(uri),true,type);
	}
	
	/**
	 * 递归添加参数目录(目录或jar文件)到当前类路径
	 * @param file 目录或jar文件对象
	 * @param type 类装载器的参考类
	 */
	public static void addCycleFileToCurrentClassPath(File file,Class<?>... type) {
		addCurrentClassPath(file,true,type);
	}
	
	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param file 目录或jar文件对象
	 * @param circle 是否递归装载子目录及其文件到类路径
	 * @param type 类装载器的参考类
	 * @description 
	 * 装载指定的参数jar文件或从装载参数目录开始递归装载所有子目录及其jar文件
	 * 注意装载到类路径的都是目录而不是文件,而jar文件则被视为一种特殊的目录被装载到类路径,所以除jar文件以外的普通文件是不能被装载到类路径的
	 */
	public static void addCurrentClassPath(File file,boolean circle,Class<?>... type) {
		if(!file.exists()) throw new RuntimeException(file.getAbsolutePath()+" is not exists...");
		
		ClassLoader classLoader=getCallerClassLoader(type);
		if(null==classLoader) throw new RuntimeException("can not get caller classloader...");
		
		addCurrentClassPath(file,circle,classLoader);
	}

	/**
	 * 添加参数目录(目录或jar文件)到当前类路径
	 * @param file 目录文件或jar文件
	 * @param classLoader 类装载器
	 * @param circle 是否递归装载子目录及其文件到类路径
	 * @description 装载指定的参数jar文件或从装载参数目录开始递归装载所有子目录及其jar文件
	 * 注意装载到类路径的都是目录而不是文件,而jar文件则被视为一种特殊的目录被装载到类路径,所以除jar文件以外的普通文件是不能被装载到类路径的
	 */
	public static void addCurrentClassPath(File file,boolean circle,ClassLoader classLoader){
		if(file.isFile() && !file.getName().endsWith(".jar")) return;
		
		try {
			addClassPathMethod.invoke(classLoader, file.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!circle || file.isFile()) return;
		
		File[] fileArray = file.listFiles();
		for(File subFile:fileArray) addCurrentClassPath(subFile,circle,classLoader);
	}
	
	/**
	 * 获取独立文件的类装载器
	 * @param fileFullPath 文件全路径
	 * @return 文件类装载器
	 * @throws IOException IO异常
	 */
	public static ClassLoader getFileClassLoader(String fileFullPath) throws IOException{
		File file=new File(fileFullPath);
		if(!file.exists()){
			throw new FileNotFoundException(fileFullPath+" is not exists...");
		}
		if(file.isDirectory()){
			throw new IOException(fileFullPath+" is directory...");
		}
		return new URLClassLoader(new URL[]{file.toURI().toURL()});
	}
	
	/**
	 * 获取独立目录的类装载器
	 * @param path 路径目录
	 * @return 目录类装载器
	 * @throws IOException IO异常
	 */
	public static ClassLoader getPathClassLoader(String path) throws IOException{
		File fileDir=new File(path);
		if(!fileDir.exists()){
			throw new FileNotFoundException(path+" is not exists...");
		}
		if(fileDir.isFile()){
			throw new IOException(path+" is file...");
		}
		
		File[] jarFiles = fileDir.listFiles(new FilenameFilter() {  
			public boolean accept(File dir, String fileName) {  
				return fileName.endsWith(".jar");  
			}  
		});
		
		URL[] urls=new URL[jarFiles.length];
		for(int i=0;i<jarFiles.length;urls[i]=jarFiles[i].toURI().toURL(),i++);
		return new URLClassLoader(urls);
	}
	
	/**
	 * 判断指定的类是否存在于类路径中
	 * @param classFullName 类的全限定名
	 * @param type 类装载器参考类
	 * @return 是否存在该类
	 */
	public static boolean existClass(String classFullName,Class<?>... type){
		return null==loadType(classFullName,getCallerClassLoader(type))?false:true;
	}
	
	
	/**
	 * 判断指定的类是否存在于类路径中
	 * @param classFullName 类的全限定名
	 * @param classLoader 类装载器
	 * @return 是否存在该类
	 */
	public static boolean existClass(String classFullName,ClassLoader classLoader){
		return null==loadType(classFullName,classLoader)?false:true;
	}
	
	/**
	 * 装载类(未初始化)到方法区
	 * @param classFullName 类的全限定名
	 * @param type 类装载器参考类
	 * @return 类
	 */
	public static Class<?> loadType(String classFullName,Class<?>... type){
		try {
			return loadClass(classFullName,getCallerClassLoader(type));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * 装载类(未初始化)到方法区
	 * @param classFullName 类的全限定名
	 * @param classLoader 类装载器
	 * @return 类
	 */
	public static Class<?> loadType(String classFullName,ClassLoader classLoader){
		try {
			return loadClass(classFullName,classLoader);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * 装载类(未初始化)到方法区
	 * @param classFullName 类的全限定名
	 * @param type 类装载器参考类
	 * @return 类
	 * @throws ClassNotFoundException 找不到类的异常
	 */
	public static Class<?> loadClass(String classFullName,Class<?>... type) throws ClassNotFoundException{
		return loadClass(classFullName,getCallerClassLoader(type));
	}
	
	/**
	 * 装载类(未初始化)到方法区
	 * @param classFullName 类的全限定名
	 * @param classLoader 类装载器
	 * @return 类
	 * @throws ClassNotFoundException 找不到类的异常
	 */
	public static Class<?> loadClass(String classFullName,ClassLoader classLoader) throws ClassNotFoundException{
		if(null==classLoader) return null;
		return classLoader.loadClass(classFullName);
	}
	
	/**
	 * 实例化类的对象到堆区
	 * @param classFullName 类的全限定名
	 * @param type 类装载器参考类
	 * @return 对象类型
	 * @throws Exception 异常
	 */
	public static Object instanceClass(String classFullName,Class<?>... type) throws Exception{
		return instanceClass(classFullName,Object.class,getCallerClassLoader(type));
	}
	
	/**
	 * 实例化类的对象到堆区
	 * @param classFullName 类的全限定名
	 * @param returnType 返回类型
	 * @param type 类装载器参考类
	 * @return 泛化类型
	 * @throws Exception 异常
	 */
	public static <R> R instanceClass(String classFullName,Class<R> returnType,Class<?>... type) throws Exception{
		return instanceClass(classFullName,returnType,getCallerClassLoader(type));
	}
	
	/**
	 * 实例化类的对象到堆区
	 * @param classFullName 类的全限定名
	 * @param returnType 返回类型
	 * @param classLoader 类装载器
	 * @return 泛化类型
	 * @throws Exception 异常
	 */
	public static <R> R instanceClass(String classFullName,Class<R> returnType,ClassLoader classLoader) throws Exception{
		if(null==classLoader) return null;
		Class<?> type=Class.forName(classFullName, true, classLoader);
		return returnType.cast(type.newInstance());
	}
	
	/**
	 * 获取指定类路径中类的实例
	 * @param absoluteFile 绝对路径的文件名或目录名
	 * @param className 类的全限定名
	 * @return 对象类型
	 * @throws Exception 异常
	 */
	public static Object getInstance(String absoluteFile,String className) throws Exception{
		return getInstance(absoluteFile,className,Object.class);
	}
	
	/**
	 * 获取指定类路径中类的实例
	 * @param absoluteFile 绝对路径的文件名或目录名
	 * @param className 类的全限定名
	 * @param returnType 返回类型
	 * @return 泛化类型
	 * @throws Exception 异常
	 */
	public static <R> R getInstance(String absoluteFile,String className,Class<R> returnType) throws Exception{
		Class<?> cla=getClass(absoluteFile,className);
		if(null==cla) return null;
		return returnType.cast(cla.newInstance());
	}
	
	/**
	 * 获取指定类路径中的类
	 * @param file 绝对路径的文件名或目录名
	 * @param className 类的全限定名
	 * @return 类
	 * @throws Exception 异常
	 */
	public static Class<?> getClass(String absoluteFile,String className) throws Exception{
		if(null==absoluteFile||null==className||absoluteFile.trim().isEmpty()||className.trim().isEmpty()) return null;
		File file=new File(absoluteFile.trim());
		if(!file.exists()) return null;
		
		ClassLoader classLoader=null;
		if(file.isFile()){
			classLoader=getFileClassLoader(absoluteFile);
		}else{
			classLoader=getPathClassLoader(absoluteFile);
		}
		
		if(null==classLoader) return null;
		return classLoader.loadClass(className.trim());
	}
	
	/**
	 * 编译Java源文件
	 * @param srcPath 源文件根目录
	 * @param classPaths 引用的第三方类路径
	 * @return 是否编译成功,若编译失败则标准输出错误信息
	 */
	public static final boolean compileJavaSource(File srcPath,File... classPaths) {
		return compileJavaSource(srcPath,null,null,null,classPaths);
	}
	
	/**
	 * 编译Java源文件
	 * @param srcPath 源文件根目录
	 * @param binPath 类文件根目录
	 * @param classPaths 引用的第三方类路径
	 * @return 是否编译成功,若编译失败则标准输出错误信息
	 */
	public static final boolean compileJavaSource(File srcPath,File binPath,File... classPaths) {
		return compileJavaSource(srcPath,binPath,null,null,classPaths);
	}
	
	/**
	 * 编译Java源文件
	 * @param srcPath 源文件根目录
	 * @param binPath 类文件根目录
	 * @param locale 编译系统操作语言
	 * @param charset 编译源文件字符编码
	 * @param classPaths 引用的第三方类路径
	 * @return 是否编译成功,若编译失败则标准输出错误信息
	 * @description 调用本方法前必须将JDK环境下的tools.jar加入类路径
	 */
	public static final boolean compileJavaSource(File srcPath,File binPath,Locale locale,String charset,File... classPaths) {
		String[] srcPathList=getJavaSourceFiles(srcPath);
		if(null==srcPathList) return false;
		
		if(null==binPath) binPath=new File(srcPath.getParentFile(),"bin");
		if(!binPath.exists()) binPath.mkdirs();
		
		if(null==locale) locale=Locale.CHINA;
		if(null==charset || 0==charset.trim().length()) charset="UTF-8";
		
		String classPath=".";
		if(null!=classPaths) {
			StringBuilder builder=new StringBuilder("");
			char pathSep=CommonUtil.getOSType().toLowerCase().startsWith("win")?';':':';
			for(int i=0;i<classPaths.length;builder.append(getJarPath(classPaths[i++],pathSep)));
			if(0!=builder.length()) {
				builder.deleteCharAt(builder.length()-1);
				classPath=classPath+pathSep+builder.toString();
			}
		}
		
		JavacTool javac=JavacTool.create();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = javac.getStandardFileManager(diagnostics, locale, Charset.forName(charset));
		Iterable<String> options = Arrays.asList("-encoding", charset, "-classpath",classPath, "-d", binPath.getAbsolutePath(), "-sourcepath", srcPath.getAbsolutePath());
		
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(srcPathList);
		JavacTask javacTask = javac.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
		if(javacTask.call()) return true;
		
		for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
			String level=diagnostic.getKind().toString();
			Long lineNum=diagnostic.getLineNumber();
			String message=diagnostic.getMessage(locale);
			Long columnNum=diagnostic.getColumnNumber();
			System.out.println("level: "+level+",lineNum: "+lineNum+",columnNum: "+columnNum+",message: "+message);
         }
		return false;
	}
	
	/**
	 * 获取JAR路径列表
	 * @param jarPath JAR路径
	 * @param pathSep 类路径分隔符
	 * @return 源文件列表(以pathSep结尾)
	 */
	private static final String getJarPath(File jarPath,char pathSep) {
		if(null==jarPath || jarPath.isFile()) return null;
		
		File[] subFiles=jarPath.listFiles();
		StringBuilder pathBuilder=new StringBuilder("");
		
		for(File subFile:subFiles) {
			if(subFile.isDirectory()) {
				pathBuilder.append(getJarPath(subFile,pathSep));
				continue;
			}
			String fileFullPath=subFile.getAbsolutePath();
			if(fileFullPath.endsWith(".jar")) pathBuilder.append(fileFullPath).append(pathSep);
		}
		
		return pathBuilder.toString();
	}
	
	/**
	 * 获取JAVA源文件列表
	 * @param src 源文件目录
	 * @return 源文件列表
	 */
	public static final String[] getJavaSourceFiles(File src) {
		if(null==src || src.isFile()) return null;
		
		File[] subFiles=src.listFiles();
		ArrayList<String> pathList=new ArrayList<String>();
		
		for(File subFile:subFiles) {
			if(subFile.isDirectory()) {
				pathList.addAll(Arrays.asList(getJavaSourceFiles(subFile)));
				continue;
			}
			String fileFullPath=subFile.getAbsolutePath();
			if(fileFullPath.endsWith(".java")) pathList.add(fileFullPath);
		}
		
		return pathList.toArray(new String[pathList.size()]);
	}
}
