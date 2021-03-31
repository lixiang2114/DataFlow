package com.github.lixiang2114.flow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Lixiang
 * @description 文件工具
 * 适用于普通配置文件的解析或读取操作,切勿用于大数据集读取
 * 更多关于文件的通用操作可以参考Java7中的Files类和Paths类
 */
public class FileUtil {
	/**
	 * 默认字符编码
	 */
	private static final String DEFAULT_CHARSET="UTF-8";
	
	/**
	 * @author Louis
	 * @description 行记录查找模式
	 */
	private static enum SubType{PREFIX,SUFFIX,CONTAINS}
	
	/**
	 * 获取文件中所有数据
	 * @param classPathfile 文件类路径
	 * @return 字串内容
	 */
	public static String getContentStringByClasspath(String classPathfile){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		return getContentAsString(inputStream);
	}
	
	/**
	 * 获取文件中所有数据
	 * @param fileFullPath 文件全路径
	 * @return 字串内容
	 */
	public static String getContentStringByFullpath(String fileFullPath){
		try {
			InputStream inputStream = new FileInputStream(fileFullPath);
			return getContentAsString(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取文件中所有数据
	 * @param inputStream 文件输入流
	 * @return 字串内容
	 */
	public static String getContentAsString(InputStream inputStream){
		byte[] b=null;
		try {
			b = new byte[inputStream.available()];
			if(CommonUtil.isEmpty(b)) return null;
			inputStream.read(b);
			return new String(b,DEFAULT_CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				if(null!=inputStream) inputStream.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取文件中所有数据
	 * @param classPathfile 文件类路径
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentListByClasspath(String classPathfile){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		return getContentAsList(inputStream);
	}
	
	/**
	 * 获取文件中所有数据
	 * @param fileFullPath 文件全路径
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentListByFullpath(String fileFullPath){
		try {
			InputStream inputStream = new FileInputStream(fileFullPath);
			return getContentAsList(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取文件中所有数据
	 * @param inputStream 文件输入流
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentAsList(InputStream inputStream){
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		ArrayList<String> lines=new ArrayList<String>();
		try {
			String line=null;
			while(null!=(line=reader.readLine())) lines.add(line);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				if(null!=reader) reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取文件中前N行数据
	 * @param classPathfile 文件类路径
	 * @param headN 文件前N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentHeadByClasspath(String classPathfile,Integer headN){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentByHead(inputStream,headN);
	}
	
	/**
	 * 获取文件中前N行数据
	 * @param fileFullPath 文件全路径
	 * @param headN 文件前N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentHeadByFullpath(String fileFullPath,Integer headN){
		try {
			InputStream inputStream = new FileInputStream(fileFullPath);
			return getContentByHead(inputStream,headN);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取文件中前N行数据
	 * @param inputStream 文件输入流
	 * @param headN 文件前N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentByHead(InputStream inputStream,Integer headN){
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		ArrayList<String> lines=new ArrayList<String>();
		try {
			String line=null;
			for(int i=0;i<headN && null!=(line=reader.readLine());lines.add(line),i++);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				if(null!=reader) reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取文件中后N行数据
	 * @param fileFullPath 文件全路径
	 * @param tailN 文件最后N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentByTail(String fileFullPath,Integer tailN){
		RandomAccessFile fp=null;
		try {
			fp = new RandomAccessFile(fileFullPath,"r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			long i=fp.length()-2;
			for(int j=0;i>1&&j<tailN;i--){
				fp.seek(i);
				if(10==fp.read()) j++;
			}
			
			ArrayList<String> list=new ArrayList<String>();
			for(String line=null;null!=(line=fp.readLine());list.add(line.trim()));
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				fp.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取文件中后N行数据
	 * @param file 文件对象
	 * @param tailN 文件最后N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentByTail(File file,Integer tailN){
		return getContentByTail(file.getAbsolutePath(),tailN);
	}
	
	/**
	 * 获取文件中后N行数据
	 * @param fileFullPath 文件全路径
	 * @param tailN 文件最后N行
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentTailByClasspath(String classPathfile,Integer tailN){
		String fileFullPath=ClassLoaderUtil.getRealPath(classPathfile);
		if(null==fileFullPath) return null;
		return getContentByTail(fileFullPath,tailN);
	}
	
	/**
	 * 获取文件中指定的若干行记录
	 * @param inputStream 文件输入流
	 * @param lineNums 获取行的行号列表(行号从1开始计数)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentList(InputStream inputStream,Integer... lineNums){
		if(CommonUtil.isEmpty(lineNums)) return null;
		
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		Arrays.sort(lineNums);
		
		int counter=0;
		ArrayList<String> list=new ArrayList<String>();
		try{
			for(int lineIndex:lineNums) {
				String line=null;
				while(null!=(line=reader.readLine())){
					counter++;
					if(counter==lineIndex) {
						list.add(line);
						break;
					}
				}
				if(null==line) break;
			}
			return list;
		} catch(IOException e){
			e.printStackTrace();
			return null;
		} finally{
			try{
				if(null!=reader) reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取文件中指定的若干行记录
	 * @param fileFullPath 文件全名
	 * @param lineNums 获取行的行号列表(行号从1开始计数)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentList(String fileFullPath,Integer... lineNums){
		FileInputStream fis=null;
		try {
			fis=new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getContentList(fis,lineNums);
	}
	
	/**
	 * 获取文件中指定的若干行记录
	 * @param classPathfile 类路径文件
	 * @param lineNums 获取行的行号列表(行号从1开始计数)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentListByClasspath(String classPathfile,Integer... lineNums){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentList(inputStream,lineNums);
	}
	
	/**
	 * 获取文件中指定的行记录
	 * @param fileFullPath 文件全名
	 * @param lineNum 获取行的行号(行号从1开始计数)
	 * @return 字符串
	 */
	public static String getContentLine(String fileFullPath,Integer lineNum){
		List<String> lines=getContentList(fileFullPath,lineNum);
		return CommonUtil.isEmpty(lines)?null:lines.get(0);
	}
	
	/**
	 * 获取文件中指定的行记录
	 * @param classPathfile 类路径文件
	 * @param lineNum 获取行的行号(行号从1开始计数)
	 * @return 字符串
	 */
	public static String getContentLineByClasspath(String classPathfile,Integer lineNum){
		List<String> lines=getContentListByClasspath(classPathfile,lineNum);
		return CommonUtil.isEmpty(lines)?null:lines.get(0);
	}
	
	/**
	 * 获取文件中指定行号范围的若干行记录
	 * @param inputStream 文件输入流
	 * @param startRow 起始行(行号从1开始计数)
	 * @param endRow 结束行(结束行号必须大于等于起始行号)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentList(InputStream inputStream,Integer startRow,Integer endRow){
		if(null==startRow||null==endRow||0==startRow||0==endRow||startRow>endRow) return null;
		
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		int lineNum=endRow-startRow+1;
		ArrayList<String> list=new ArrayList<String>();
		try{
			String line=null;
			for(int i=0;i<startRow-1&&null!=(line=reader.readLine());i++);
			for(int i=0;i<lineNum&&null!=(line=reader.readLine());list.add(line),i++);
			return list.isEmpty()?null:list;
		} catch(IOException e){
			e.printStackTrace();
			return null;
		} finally{
			try{
				if(null!=reader) reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取文件中指定行号范围的若干行记录
	 * @param fileFullPath 文件全名
	 * @param startRow 起始行(行号从1开始计数)
	 * @param endRow 结束行(结束行号必须大于等于起始行号)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentList(String fileFullPath,Integer startRow,Integer endRow){
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getContentList(inputStream,startRow,endRow);
	}
	
	/**
	 * 获取文件中指定行号范围的若干行记录
	 * @param classPathfile 类路径文件
	 * @param startRow 起始行(行号从1开始计数)
	 * @param endRow 结束行(结束行号必须大于等于起始行号)
	 * @return 字串列表内容
	 */
	public static ArrayList<String> getContentListByClasspath(String classPathfile,Integer startRow,Integer endRow){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentList(inputStream,startRow,endRow);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param inputStream 文件输入流
	 * @param subStr 子串
	 * @param subType 过滤模式
	 * @return 字串列表
	 */
	public static ArrayList<String> getContentList(InputStream inputStream,String subStr,SubType subType){
		if(null==subType||CommonUtil.isEmpty(subStr)) return null;
		
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		ArrayList<String> list=new ArrayList<String>();
		try{
			String line=null;
			switch(subType){
				case PREFIX:
					while(null!=(line=reader.readLine())) {
						if(line.startsWith(subStr)) list.add(line);
					}
					break;
				case SUFFIX:
					while(null!=(line=reader.readLine())) {
						if(line.endsWith(subStr)) list.add(line);
					}
					break;
				default:
					while(null!=(line=reader.readLine())) {
						if(line.contains(subStr)) list.add(line);
					}
			}
			return list;
		} catch(IOException e){
			e.printStackTrace();
			return null;
		} finally{
			try{
				if(null!=reader) reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param fileFullPath 文件全名
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getPrefixLines(String fileFullPath,String subStr){
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getContentList(inputStream,subStr,SubType.PREFIX);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param fileFullPath 文件全名
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getSuffixLines(String fileFullPath,String subStr){
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getContentList(inputStream,subStr,SubType.SUFFIX);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param fileFullPath 文件全名
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getContainsLines(String fileFullPath,String subStr){
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getContentList(inputStream,subStr,SubType.CONTAINS);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param classPathfile 类路径文件
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getPrefixLinesByClasspath(String classPathfile,String subStr){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentList(inputStream,subStr,SubType.PREFIX);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param classPathfile 类路径文件
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getSuffixLinesByClasspath(String classPathfile,String subStr){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentList(inputStream,subStr,SubType.SUFFIX);
	}
	
	/**
	 * 获取文件中包含指定子串的若干行记录
	 * @param classPathfile 类路径文件
	 * @param subStr 子串
	 * @return 字串列表
	 */
	public static ArrayList<String> getContainsLinesByClasspath(String classPathfile,String subStr){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getContentList(inputStream,subStr,SubType.CONTAINS);
	}
	
	/**
	 * 获取文件中包含指定正则式匹配的若干行记录
	 * @param inputStream 文件输入流
	 * @param regex 正则式字串
	 * @return 字串列表
	 */
	public static ArrayList<String> getRegexLines(InputStream inputStream,String regex){
		if(CommonUtil.isEmpty(regex)) return null;
		
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		
		String line=null;
		ArrayList<String> list=new ArrayList<String>();
		Pattern subRegexPattern=Pattern.compile(regex);
		try{
			while(null!=(line=reader.readLine())) if(subRegexPattern.matcher(line).find()) list.add(line);
			return list;
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try{
				if(null!=reader) reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取文件中包含指定正则式匹配的若干行记录
	 * @param fileFullPath 文件全名
	 * @param regex 正则式字串
	 * @return 字串列表
	 */
	public static ArrayList<String> getRegexLines(String fileFullPath,String regex){
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(fileFullPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return getRegexLines(inputStream,regex);
	}
	
	/**
	 * 获取文件中包含指定正则式匹配的若干行记录
	 * @param classPathfile 类路径文件
	 * @param regex 正则式字串
	 * @return 字串列表
	 */
	public static ArrayList<String> getRegexLinesByClasspath(String classPathfile,String regex){
		InputStream inputStream=ClassLoaderUtil.getClassPathFileStream(classPathfile);
		if(null==inputStream) return null;
		return getRegexLines(inputStream,regex);
	}
	
	/**
	 * 监视指定的文件或目录变化
	 * @param filePath 被监视的文件
	 * @param callable 文件变化回调
	 * @throws Exception
	 */
	public static void watchFile(String file,FileCallback callable) throws Exception{
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Paths.get(file).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE);
		 while(!Thread.currentThread().isInterrupted()) callable.call(watchService.take());
	}
	
	/**
	 * @author Louis
	 * 文件事件回调接口
	 */
	public static interface FileCallback{
		default void call(WatchKey key){};
	}
	
	/**
	 * 拷贝(复制)文件(文件或目录)到指定的目标目录下,若目标文件存在则不发生拷贝
	 * @param srcFile 源文件(可以是文件或目录)
	 * @param dstPath 目标路径目录(必须是目录)
	 * @description 
	 * 本方法会递归拷贝文件/目录及其子文件和子目录,若目录树中间有存在的目标子目录,
	 * 则该目标子目录及其该目标子目录下的所有子文件和子目录都将不发生拷贝
	 * @throws IOException 
	 */
	public static final void copyFile(File srcFile,File dstDir) throws IOException{
		if(null==srcFile || null==dstDir || !srcFile.exists()) return;
		
		if(dstDir.exists()){
			if(dstDir.isFile()) return;
		}else{
			dstDir.mkdirs();
		}
		
		File dstPath=new File(dstDir,srcFile.getName());
		if(dstPath.exists()) return;
		
		Files.copy(srcFile.toPath(), dstPath.toPath());
		if(srcFile.isFile()) return;
		
		File[] subFiles=srcFile.listFiles();
		for(File file:subFiles) copyFile(file,dstPath);
	}
	
	/**
	 * 移动(剪切)文件(文件或目录)到指定的目标目录下,若目标文件存在则不发生移动
	 * @param srcFile 源文件(可以是文件或目录)
	 * @param dstPath 目标路径目录(必须是目录)
	 * @description 
	 * 本方法会递归移动文件/目录及其子文件和子目录,若目录树中间有存在的目标子目录,
	 * 则该目标子目录及其该目标子目录下的所有子文件和子目录都将不发生移动
	 * @throws IOException 
	 */
	public static final void moveFile(File srcFile,File dstDir) throws IOException{
		if(null==srcFile || null==dstDir || !srcFile.exists()) return;
		
		if(dstDir.exists()){
			if(dstDir.isFile()) return;
		}else{
			dstDir.mkdirs();
		}
		
		File dstPath=new File(dstDir,srcFile.getName());
		if(dstPath.exists()) return;
		
		Files.move(srcFile.toPath(), dstPath.toPath());
	}
	
	/**
	 * 删除(移除)文件或目录
	 * @param srcFile 待删除的文件或目录
	 * @description 本方法会递归删除文件/目录及其子文件和子目录
	 * @throws IOException
	 */
	public static final void dropFile(File srcFile) {
		if(null==srcFile || !srcFile.exists()) return;
		
		File[] subFiles=null;
		if(srcFile.isFile() || 0==(subFiles=srcFile.listFiles()).length) {
			srcFile.delete();
			return;
		}
		
		for(File subFile:subFiles) dropFile(subFile);
		srcFile.delete();
	}
	
	/**
	 * 将指定的参数字串以指定的写出方式写入参数指定的文件中,文件不存在则自动创建
	 * @param outFile 写出的文件
	 * @param conent 被写出的字符串
	 */
	public static final void overrideWriteFile(File outFile,String conent) {
		setFileContent(outFile,conent);
	}
	
	/**
	 * 将指定的参数字串以指定的写出方式写入参数指定的文件中,文件不存在则自动创建
	 * @param outFile 写出的文件
	 * @param conent 被写出的字符串
	 */
	public static final void appendWriteFile(File outFile,String conent) {
		setFileContent(outFile,conent,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
	}
	
	/**
	 * 将指定的参数字串以指定的写出方式写入参数指定的文件中,文件不存在则自动创建
	 * @param outFile 写出的文件
	 * @param conent 被写出的字符串列表
	 */
	public static final void overrideWriteFiles(File outFile,List<String> conents) {
		setFileContents(outFile,conents);
	}
	
	/**
	 * 将指定的参数字串以指定的写出方式写入参数指定的文件中,文件不存在则自动创建
	 * @param outFile 写出的文件
	 * @param conent 被写出的字符串列表
	 */
	public static final void appendWriteFiles(File outFile,List<String> conents) {
		setFileContents(outFile,conents,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
	}
	
	/**
	 * 将指定的参数字串以指定的写出方式写入参数指定的文件中
	 * @param outFile 写出的文件
	 * @param conent 被写出的字符串
	 * @param options 写出的模式(默认为覆盖写[先清空源文件再写入])
	 * 写出模式是StandardOpenOption枚举类的一个实例:
	 * 追加写出:StandardOpenOption.CREATE+StandardOpenOption.APPEND
	 * 覆盖写出:StandardOpenOption.CREATE+StandardOpenOption.TRUNCATE_EXISTING
	 */
	public static final void setFileContent(File outFile,String conent,OpenOption... options) {
		if(null==conent) return;
		conent=conent.trim();
		if(0==conent.length()) return;
		try {
			Files.write(outFile.toPath(), conent.getBytes(), options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将指定的参数列表以指定的写出方式写入参数指定的文件中
	 * @param outFile 输出到指定的文件
	 * @param lines 被写出的参数字串列表
	 * @param options 写出的模式(默认为覆盖写[先清空源文件再写入])
	 * 写出模式是StandardOpenOption枚举类的一个实例
	 */
	public static final void setFileContents(File outFile,Iterable<String> lines,OpenOption... options) {
		if(null==lines) return;
		try {
			Files.write(outFile.toPath(), lines, StandardCharsets.UTF_8, options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将指定的参数列表以指定的写出方式写入参数指定的文件中
	 * @param outFile 输出到指定的文件
	 * @param lines 被写出的参数字串列表
	 * @param charset 写出的字符编码
	 * @param options 写出的模式(默认为覆盖写[先清空源文件再写入])
	 * 写出模式是StandardOpenOption枚举类的一个实例
	 */
	public static final void setFileContents(File outFile,Iterable<? extends CharSequence> lines,Charset charset,OpenOption... options) {
		if(null==lines) return;
		if(null==charset) charset=StandardCharsets.UTF_8;
		try {
			Files.write(outFile.toPath(), lines, charset, options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
