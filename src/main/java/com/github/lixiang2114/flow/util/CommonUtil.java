package com.github.lixiang2114.flow.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Lixiang
 * 通用工具集
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CommonUtil {
	/**
	 * 类对象类型(类类型、接口类型、所有类型)
	 */
	public static enum ClassType{CLASS,FACE,ALL}
	
	/**
     * 逗号正则式
     */
	public static final Pattern COMMA=Pattern.compile(",");
	
	/**
	 * 空白分隔符
	 */
	public static final Pattern BLANK=Pattern.compile("\\s+");
	
	/**
     * 短横线正则式
     */
	public static final Pattern DASH_HUMP=Pattern.compile("-|_");
	
	/**
     * 点号正则式
     */
	public static final Pattern DOT_REGEX = Pattern.compile("\\.");
	
	/**
	 * 基本类型到包装类型映射字典
	 */
	public static final HashMap<Class<?>,Class<?>> BASE_TO_WRAP;
    
    /**
     * 键值分隔符正则式
     */
	public static final Pattern KEYVAL_SEPARATOR=Pattern.compile(":|=");
    
    /**
     * 元素分隔符正则式
     */
	public static final Pattern ELEMENT_SEPARATOR=Pattern.compile(",|;");
    
    /**
     * 单双引号集合
     */
	public static final List<Character> QUOTATIONS=Arrays.asList('\'','"');
	
	 /**
     * 整数正则式
     */
	public static final Pattern INTEGER_CHARACTER=Pattern.compile("[0-9]+");
    
    /**
     * 数字正则式
     */
	public static final Pattern NUMBER_CHARACTER=Pattern.compile("[0-9.]+");
    
    /**
     * 字母正则式
     */
	public static final Pattern LETTER_CHARACTER=Pattern.compile("[a-zA-Z]+");
    
    /**
     * 单词正则式
     */
	public static final Pattern WORD_CHARACTER=Pattern.compile("[a-zA-Z0-9_]+");
    
    /**
     * 中文(汉字)正则式
     */
	public static final Pattern CHINESE_CHARACTER=Pattern.compile("[\u4e00-\u9fa5]+");
    
	/**
     * JSON工具类
     */
	public static final String JSON_UTIL_CLASS="com.fasterxml.jackson.databind.ObjectMapper";
    
    /**
     * 方法签名正则式
     */
	public static final Pattern METHOD_SIGNATURE=Pattern.compile("([a-z_$A-Z]+[0-9]*)[\\s]*\\((.*)\\)");
	
	static{
		BASE_TO_WRAP=new HashMap<Class<?>,Class<?>>();
		BASE_TO_WRAP.put(byte.class, Byte.class);
		BASE_TO_WRAP.put(short.class, Short.class);
		BASE_TO_WRAP.put(int.class, Integer.class);
		BASE_TO_WRAP.put(long.class, Long.class);
		BASE_TO_WRAP.put(float.class, Float.class);
		BASE_TO_WRAP.put(double.class, Double.class);
		BASE_TO_WRAP.put(boolean.class, Boolean.class);
		BASE_TO_WRAP.put(char.class, Character.class);
		BASE_TO_WRAP.put(void.class, Void.class);
	}
	
	/**
     * 指定类型是否为基本类型
     * @param type 类型
     * @return 是否为8种基本类型
     */
    public static boolean isBaseType(Class<?> type){
    	return type.isPrimitive();
    }
    
    /**
     * 指定类型是否为包装类型
     * @param type 类型
     * @return 是否为8种包装类型
     */
    public static boolean isWrapType(Class<?> type){
    	return BASE_TO_WRAP.containsValue(type);
    }
    
    /**
     * 指定类型是否为日期类型
     * @param type 类型
     * @return 是否为日期类型
     */
    public static boolean isDateType(Class<?> type){
    	return Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type);
    }
    
    /**
     * 指定类型是否为简单类型
     * @param type 类型
     * @return 是否为三类简单类型
     * @description
     * 简单类型包括8中基本类型、8种包装类型、字符串类型和日期类型
     */
    public static boolean isSimpleType(Class<?> type){
    	if(isBaseType(type) || isWrapType(type) || String.class.isAssignableFrom(type)) return true;
    	if(Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) return true;
    	return false;
    }
    
    /**
     * 指定类型是否为集合类型
     * @param type 类型
     * @return 是否为集合类型
     */
    public static boolean isCollectionType(Class<?> type){
    	if(Collection.class.isAssignableFrom(type)) return true;
    	if(Map.class.isAssignableFrom(type)) return true;
    	return false;
    }
    
    /**
     * 指定类型是否为数字类型
     * @param type 类型
     * @return 是否为数字类型
     */
    public static boolean isNumber(Class<?> type){
    	if(Number.class.isAssignableFrom(type)) return true;
    	if(boolean.class==type || char.class==type || void.class==type) return false;
    	return BASE_TO_WRAP.containsKey(type);
    }
    
    /**
     * 指定字串是否为整数串
     * @param type 类型
     * @return 是否为整数串
     */
    public static boolean isInteger(String string){
    	if(isEmpty(string)) return false;
    	return INTEGER_CHARACTER.matcher(string).matches();
    }
    
    /**
     * 指定字串是否为纯数字串
     * @param type 类型
     * @return 是否为纯数字串
     */
    public static boolean isNumber(String string){
    	if(isEmpty(string)) return false;
    	return NUMBER_CHARACTER.matcher(string).matches();
    }
    
    /**
     * 指定字串是否为纯字母串
     * @param type 类型
     * @return 是否为纯字母串
     */
    public static boolean isLetter(String string){
    	if(isEmpty(string)) return false;
    	return LETTER_CHARACTER.matcher(string).matches();
    }
    
    /**
	 * 指定字串是否为纯汉字串
	 * @param source 字符串
	 * @return 是否为纯汉字串
	 */
	public static boolean isUnicode(String string){
		if(isEmpty(string)) return false;
		return CHINESE_CHARACTER.matcher(string).matches();
	}
	
	/**
	 * 指定字串是否为纯单词串
	 * @param source 字符串
	 * @return 是否为纯单词串
	 */
	public static boolean isWord(String string){
		if(isEmpty(string)) return false;
		return WORD_CHARACTER.matcher(string).matches();
	}
	
		/**
		 * 判断给定的字串是否包含数字
		 * @param utfString 源串
		 * @return 是否包含数字
		 */
		public static boolean containsNumber(String utfString){
			return NUMBER_CHARACTER.matcher(utfString).find();
		}
		
		/**
		 * 判断给定的字串是否包含字母
		 * @param utfString 源串
		 * @return 是否包含字母
		 */
		public static boolean containsLetter(String utfString){
			return LETTER_CHARACTER.matcher(utfString).find();
		}
		
		/**
		 * 判断给定的字串是否包含汉字
		 * @param utfString 源串
		 * @return 是否包含汉字
		 */
		public static boolean containsCNChar(String utfString){
			return CHINESE_CHARACTER.matcher(utfString).find();
		}
		
		 /**
		 * 判定给定的字符串是否包含Unicode字符
		 * @param utfString 源串
		 * @return 是否包含Unicode字符
		 */
		public static boolean containsUnicode(String utfString){
			return -1==utfString.indexOf("\\u")?false:true;
		}
		
		/**
		 * 判断指定的参数类型是否为泛型参数
		 * @param type 参数类型
		 * @return 是否为泛型参数
		 */
		public static boolean isGeneric(Type type){
			return (null==type||Class.class.isInstance(type))?false:true;
		}
		
		/**
		 * 获取类型可能对应的包装类型
		 * @param type 参考类型
		 * @return 包装类型
		 */
		public static Class<?> getWrapType(Class<?> type){
			if(null==type) return null;
			if(!type.isPrimitive()) return type;
			return BASE_TO_WRAP.get(type);
		}
		
		/**
		 * 获取操作系统类型
		 * @return 系统类型
		 */
		public static String getOSType(){
			String osType=System.getProperty("os.name");
			if(null==osType || 0==osType.trim().length()) return null;
			String[] osInfo=BLANK.split(osType.trim());
			if(0==osInfo.length) return null;
			return upperFirstChar(osInfo[0]);
		}
		
		/**
		 * 获取操作系统版本
		 * @return 系统版本
		 */
		public static String getOSVersion(){
			return System.getProperty("os.version");
		}
		
		/**
		 * 获取处理器架构
		 * @return 系统架构
		 */
		public static String getCPUArch(){
			return System.getProperty("os.arch");
		}
		
		/**
		 * 获取类中成员是否携带泛型
		 * @param member 类的成员
		 * @return 成员是否携带泛型
		 * @description 泛型集合成员返回true,数组和其它成员返回false
		 */
		public static boolean hasGeneric(Member member){
			return isGeneric(getGeneric(member));
		}
		
		/**
		 * 获取类中成员的泛型类型
		 * @param member 成员字段(Field)或成员方法(Method)
		 * @return 泛型类型
		 * @description 若成员携带泛型参数,则返回成员参数类型,否则返回成员自身类型
		 */
		public static Type getGeneric(Member member){
			if(null==member) return null;
			if(Field.class.isInstance(member)) return ((Field)member).getGenericType();
			if(Method.class.isInstance(member)) return ((Method)member).getGenericReturnType();
			return null;
		}
		
		/**
		 * 获取类中成员(字段(Field)或方法(Method))携带的泛型参数类型
		 * @param member 类中成员
		 * @return 泛型类型
		 * @description 字段泛化参数类型或方法返回泛化参数类型
		 */
		public static Class<?>[] getGenericClass(Member member){
			Type type=getGeneric(member);
			if(!isGeneric(type)) return null;
			Type[] classParams=((ParameterizedType)type).getActualTypeArguments();
			if(null==classParams || 0==classParams.length) return null;
			Object newArray=Array.newInstance(Class.class, classParams.length);
			System.arraycopy(classParams, 0, newArray, 0, classParams.length);
			return (Class<?>[])newArray;
		}
		
		/**
		 * 获取成员方法的泛型参数类型
		 * @param method 成员方法
		 * @return 泛型参数类型表
		 * @description 返回方法中每个参数类型的泛化类型列表
		 * 返回列表中的每一个数组代表对应参数项的泛型类型表(每一个参数可以携带多个泛型类型)
		 */
		public static ArrayList<Class<?>[]> getGenericParamClass(Method method){
			if(null==method) return null;
			Type[] types=method.getGenericParameterTypes();
			ArrayList<Class<?>[]> paramGenericTypes=new ArrayList<Class<?>[]>();
			
			for(int i=0;i<types.length;i++){
				if(!isGeneric(types[i])) {
					paramGenericTypes.add(null);
					continue;
				}
				
				Type[] classParams=((ParameterizedType)types[i]).getActualTypeArguments();
				if(null==classParams || 0==classParams.length) {
					paramGenericTypes.add(null);
					continue;
				}
				
				Object newArray=Array.newInstance(Class.class, classParams.length);
				System.arraycopy(classParams, 0, newArray, 0, classParams.length);
				paramGenericTypes.add((Class<?>[])newArray);
			}
			
			return paramGenericTypes;
		}
		
		/**
		 * 读取输入流中的一行记录并返回读取的行记录
		 * @param inputStream 输入流
		 * @param bufferSize 缓冲尺寸
		 * @return 实际读取数据行
		 * @throws IOException
		 */
		public static String readLine(InputStream inputStream,Integer... bufferSize) throws IOException{
			byte[] b=readBytes(inputStream,bufferSize);
			if(null==b) return null;
			return new String(b).trim();
		}
		
		/**
		 * 读取输入流中的一行记录并返回读取的字节数组
		 * @param inputStream 输入流
		 * @param bufferSize 缓冲尺寸
		 * @return 实际读取的字节数组(含换行符和回车符)
		 * @throws IOException
		 */
		public static byte[] readBytes(InputStream inputStream,Integer... bufferSize) throws IOException{
			int size=null==bufferSize || 0==bufferSize.length?32768:null==bufferSize[0]?32768:bufferSize[0];
			int k=inputStream.read();
			if(-1==k) return null;
			
			int i=0;
			byte[] b=new byte[size];
			b[i++]=(byte)k;
			
			for(k=inputStream.read();k!=-1;k=inputStream.read()){
				byte kk=(byte)k;
				b[i++]=kk;
				if(10==kk || 13==kk) break;
			}
			
			byte[] retByte=new byte[i];
			System.arraycopy(b, 0, retByte, 0, i);
			return retByte;
		}
		
		/**
		 * 转换数组的元素类型
		 * @param array 原数组
		 * @param componentType 新数组的组件类型
		 * @return 新组件类型的数组
		 * @description 
		 * 与asArray的区别在于srcArray如果是非数组类型的情况下,本方法直接返回原值srcArray,
		 * 而asArray则将srcArray包装成一个数组返回(asArray总是返回一个数组),如果srcArray为数组类型则两者行为一致(本质上均返回数组)
		 */
		public static <E> Object transferArray(Object srcArray,Class<E> newComType){
			if(null==srcArray) return null;
			if(null==newComType) return srcArray;
			if(!srcArray.getClass().isArray()) return srcArray;
			
			int arrayLen=Array.getLength(srcArray);
			Object newArray=Array.newInstance(newComType, arrayLen);
			for(int i=0;i<arrayLen;Array.set(newArray, i, transferType(Array.get(srcArray, i),newComType)),i++);
			return newArray;
		}
		
		/**
		 * 将对象转换到指定的类型(本方法堪称为万能类型转换法)
		 * @param value 待转换的对象
		 * @param returnType 转换到的目标类型
		 * @param elementTypes 目标集合中的元素类型
		 * @return 目标类型对象
		 */
		public static <R,E> R transferType(Object value,Class<R> returnType,Class<E>... elementTypes){
			return transferType(value,returnType,null,elementTypes);
		}
		
		/**
		 * 将对象转换到指定的类型(本方法堪称为万能类型转换法)
		 * @param value 待转换的对象
		 * @param returnType 转换到的目标类型
		 * @param elementTypes 目标集合中的元素类型
		 * @return 目标类型对象
		 */
		public static <R,E> R transferType(Object value,Class<R> returnType,Class<?> keyType,Class<E>... elementTypes){
			if(null==value || null==returnType) return null;
			Class<?> valueType=value.getClass();
			if(compatible(returnType,valueType)) return (R)value;
			try {
				return objectToType(value,returnType,keyType,elementTypes);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		/**
		 * 判断给定的基类型superType是否可以兼容到指定的子类型childType
		 * @param superType 基类型
		 * @param childType 子类型
		 * @return 是否兼容
		 */
		public static boolean compatible(Class<?> superType,Class<?> childType){
			if(superType==childType) return true;
			if(null==superType && null!=childType) return false;
			if(null!=superType && null==childType) return false;
			if(superType.isAssignableFrom(childType)) return true;
			try{
				if(superType.isPrimitive() && superType==childType.getField("TYPE").get(null)) return true;
				if(childType.isPrimitive() && childType==superType.getField("TYPE").get(null)) return true;
				return false;
			}catch(Exception e){
				return false;
			}
		}
		
		/**
		 * 判断给定的基类型superTypes数组是否可以兼容到指定的子类型数组childTypes
		 * @param superTypes 基类型
		 * @param childTypes 子类型
		 * @return 数组类型是否兼容
		 */
		public static boolean compatible(Class<?>[] superTypes,Class<?>[] childTypes){
			if(superTypes==childTypes) return true;
			if(null==superTypes && null!=childTypes) return false;
			if(null!=superTypes && null==childTypes) return false;
			if(superTypes.length!=childTypes.length) return false;
			try{
				for(int i=0;i<superTypes.length;i++){
					if(superTypes[i]==childTypes[i] || superTypes[i].isAssignableFrom(childTypes[i])) continue;
					if(superTypes[i].isPrimitive() && superTypes[i]==childTypes[i].getField("TYPE").get(null)) continue;
					if(childTypes[i].isPrimitive() && childTypes[i]==superTypes[i].getField("TYPE").get(null)) continue;
					return false;
				}
				return true;
			}catch(Exception e){
				return false;
			}
		}
		
		/**
		 * 将ASCII码数组中的每个元素值转换成对应的字符
		 * @param asciis 源ASCII数组
		 * @return 字符串
		 */
		public static String asciiToChar(int... asciis){
			if(null==asciis||0==asciis.length) return null;
			StringBuilder builder=new StringBuilder();
			for (int i=0; i<asciis.length;builder.append((char)asciis[i]),i++);
			return builder.toString();
		}
		
		/**
		 * 将字串中每个字符转换成对应的ASCII码
		 * @param utfString 源串
		 * @return ASCII码数组
		 */
		public static int[] charToAscii(String utfString){
			if(null==utfString||utfString.isEmpty()) return null;
			char[] chars = utfString.toCharArray();
			int[] asciis=new int[chars.length];
			for (int i=0; i<chars.length;asciis[i]=(int)chars[i],i++);
			return asciis;
		}
	    
	    /**
		 * 将中文字串转换为Unicode编码
		 * @param utfString 源字符串
		 * @return Unicode字符串
		 */
		public static String encodeToUnicode(String utfString) {
			if(isEmpty(utfString)) return utfString;
		    char[] utfChars = utfString.toCharArray();
		    StringBuilder charBuilder = new StringBuilder("");
		    for (int i = 0; i<utfChars.length; i++) {
		        String hexChar = Integer.toHexString(utfChars[i]);
		        charBuilder.append("\\u");
		        if(2<hexChar.length()){
		        	charBuilder.append(hexChar);
		        	continue;
		        }
		        charBuilder.append("00").append(hexChar);
		    }
		    return charBuilder.toString();
		}
		 
		/**
		 * 将Unicode编码转换为中文字串
		 * @param unicode Unicode编码
		 * @return 中文字串
		 */
		public static String decodeFromUnicode(String unicode) {
			if(isEmpty(unicode)) return unicode;
		    StringBuilder builder = new StringBuilder();
		    for (int start=0,end=0;-1!=start;start=end) {
		        end = unicode.indexOf("\\u", start+2);
		        if (end != -1)  {
		        	builder.append((char)Integer.parseInt(unicode.substring(start+2, end), 16));
		        	continue;
		        }
		        builder.append((char)Integer.parseInt(unicode.substring(start+2, unicode.length()), 16));
		    }
		    return builder.toString();
		}
		
	/**
	 * 将ISO8859-1字符集转换为UTF-8字符集
	 * @param unicode 源字串
	 * @return UTF8字串
	 */
	public static String ios8859ToUtf8(String unicode) {
		return srcCharsetToDstCharset(unicode,"ISO8859-1","UTF-8");
	}
	
	/**
	 * 将字串由srcCharset字符集转换为dstCharset字符集
	 * @param unicode 源字串
	 * @param srcCharset 源字符集
	 * @param dstCharset 目标字符集
	 * @return dstCharset字串
	 */
	public static String srcCharsetToDstCharset(String unicode,String srcCharset,String dstCharset) {
		try {
			return new String(unicode.getBytes(srcCharset),dstCharset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * 将对象转换为字符串描述
     * @description 格式化Object类中的toString方法
     * @param object 对象
     * @return 字符串
     */
    public static String toString(Object object){
    	if(null==object) return null;
    	Class<?> type=object.getClass();
    	if(Date.class.isAssignableFrom(type)) return DateUtil.dateToString((Date)object);
    	if(Calendar.class.isAssignableFrom(type)) return DateUtil.calendarToString((Calendar)object);
    	if(isSimpleType(type)) return object.toString().trim();
    	if(Iterable.class.isAssignableFrom(type)) {
    		ArrayList<String> retList=new ArrayList<String>();
    		((Iterable)object).forEach(obj->retList.add(toString(obj)));
    		return retList.toString();
    	}
    	LinkedHashMap<String,String> retMap=new LinkedHashMap<String,String>();
    	if(Map.class.isAssignableFrom(type)) {
    		((Map)object).forEach((k,v)->retMap.put(toString(k), toString(v)));
    		return retMap.toString();
    	}
    	toMap(object).forEach((k,v)->retMap.put(k, toString(v)));
    	return retMap.toString();
    }
	
	/**
	 * 将数组对象转换为对象数组
	 * @param array 数组对象
	 * @return 对象数组
	 */
	public static Object[] asArray(Object array) {
	    return asArray(array,Object.class);
	}
	
	/**
	 * 将数组对象转换为基本字节数组
	 * @param array 数组对象
	 * @return 字节数组
	 */
	public static byte[] asByteArray(Object array) {
		Byte[] k1=asArray(array,Byte.class);
		byte[] k2=new byte[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本短整型数组
	 * @param array 数组对象
	 * @return 短整型数组
	 */
	public static short[] asShortArray(Object array) {
		Short[] k1=asArray(array,Short.class);
		short[] k2=new short[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本整型数组
	 * @param array 数组对象
	 * @return 整型数组
	 */
	public static int[] asIntArray(Object array) {
		Integer[] k1=asArray(array,Integer.class);
		int[] k2=new int[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本长整型数组
	 * @param array 数组对象
	 * @return 长整型数组
	 */
	public static long[] asLongArray(Object array) {
		Long[] k1=asArray(array,Long.class);
		long[] k2=new long[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本浮点型数组
	 * @param array 数组对象
	 * @return 浮点型数组
	 */
	public static float[] asFloatArray(Object array) {
		Float[] k1=asArray(array,Float.class);
		float[] k2=new float[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本实数型数组
	 * @param array 数组对象
	 * @return 实数型数组
	 */
	public static double[] asDoubleArray(Object array) {
		Double[] k1=asArray(array,Double.class);
		double[] k2=new double[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本字符型数组
	 * @param array 数组对象
	 * @return 字符型数组
	 */
	public static char[] asCharArray(Object array) {
		Character[] k1=asArray(array,Character.class);
		char[] k2=new char[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为基本布尔型数组
	 * @param array 数组对象
	 * @return 布尔型数组
	 */
	public static boolean[] asBooleanArray(Object array) {
		Boolean[] k1=asArray(array,Boolean.class);
		boolean[] k2=new boolean[k1.length];
		for(int i=0;i<k1.length;k2[i]=k1[i],i++);
	    return k2;
	}
	
	/**
	 * 将数组对象转换为对象数组
	 * @param array 数组对象
	 * @param elementType 数组元素类型(不能是基本类型)
	 * @return 泛化数组
	 */
	public static <E> E[] asArray(Object array,Class<E> elementType) {
		 Object newArray=null;
		 if (null==array) return null;
		
		 if(!array.getClass().isArray()) {
			 newArray=Array.newInstance(elementType, 1);
			 Array.set(newArray,0, transferType(array,elementType));
		 } else{
			 int length = Array.getLength(array);
			 newArray=Array.newInstance(elementType, length);
			 for(int i=0;i<length;Array.set(newArray, i, transferType(Array.get(array, i),elementType)),i++);
		 }
		 
		 return (E[])newArray;
	  }
	
	/**
	 * 字符串转换为字串数组(默认使用逗号分隔符)
	 * @param src 源串
	 * @param separators 分隔符
	 * @return 字串数组
	 */
	public static String[] splitToArray(String src,String... separators){
		if(isEmpty(src)) return null;
		if(isEmpty(separators)) return COMMA.split(src);
		return src.split(separators[0]);
	}
	
	/**
	 * 字符串转换为字串列表(默认使用逗号分隔符)
	 * @param src 源串
	 * @param separators 分隔符
	 * @return 字串列表
	 */
	public static List<String> splitToList(String src,String... separators){
		if(isEmpty(src)) return null;
		String[] array=splitToArray(src);
		return Arrays.asList(array);
	}
	
	/**
	 * 字串数组串联成字符串(默认使用空串连接)
	 * @param src 源字串数组
	 * @param separators 分隔符
	 * @return 字符串
	 */
	public static String joinToString(String[] srcs,String... separators){
		if(isEmpty(srcs)) return null;
		StringBuilder builder=new StringBuilder();
		if(isEmpty(separators)) {
			for(int i=0;i<srcs.length;builder.append(srcs[i++]));
			return builder.toString();
		}
		for(int i=0;i<srcs.length;builder.append(srcs[i++]).append(separators[0]));
		if(builder.length()>0) builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
	
	/**
	 * 字串列表串联成字符串(默认使用空串连接)
	 * @param src 源字串列表
	 * @param separators 分隔符
	 * @return 字符串
	 */
	public static String joinToString(List<String> srcs,String... separators){
		if(isEmpty(srcs)) return null;
		String[] srcArray=srcs.toArray(new String[srcs.size()]);
		return joinToString(srcArray,separators);
	}
	
	/**
	 * 将字符串按"key1=val1,key2=val2..."格式解析为字典
	 * @param src 源串
	 * @return 字典
	 */
	public static HashMap<String,String> parseToMap(String src){
		HashMap<String,String> map=new HashMap<String,String>();
		String[] array=ELEMENT_SEPARATOR.split(src);
		for(String ele:array){
			String[] entry=KEYVAL_SEPARATOR.split(ele);
			if(2>entry.length) continue;
			map.put(entry[0].trim(), entry[1].trim());
		}
		return map;
	}
	
	/**
	 * 将字符串按指定分隔符解析为字典
	 * @param src 源串
	 * @param eleSeparator 元素分隔符
	 * @param keyvalSeparator 键值分隔符
	 * @return 字典
	 */
	public static HashMap<String,String> parseToMap(String src,String eleSeparator,String keyvalSeparator){
		HashMap<String,String> map=new HashMap<String,String>();
		String[] array=src.split(eleSeparator);
		for(String ele:array){
			String[] entry=ele.split(keyvalSeparator);
			if(2>entry.length) continue;
			map.put(entry[0].trim(), entry[1].trim());
		}
		return map;
	}
	
	/**
	 * 将字符串按"key1=val1,key2=val2..."格式解析为字典
	 * @param src 源串
	 * @param separators 分隔符
	 * @return 字典
	 */
	public static <R> R parseToEntity(String src,Class<R> entityType){
		String[] array=ELEMENT_SEPARATOR.split(src);
		try {
			R r = entityType.newInstance();
			for(String ele:array){
				String[] entry=KEYVAL_SEPARATOR.split(ele);
				if(2>entry.length) continue;
				Field field=findField(entityType, entry[0].trim());
				Object fieldValue=transferType(entry[1].trim(), field.getType());
				field.set(r, fieldValue);
			}
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将字符串按指定分隔符解析为字典
	 * @param src 源串
	 * @param separators 分隔符
	 * @return 字典
	 */
	public static <R> R parseToEntity(String src,Class<R> entityType,String eleSeparator,String keyvalSeparator){
		String[] array=src.split(eleSeparator);
		try {
			R r = entityType.newInstance();
			for(String ele:array){
				String[] entry=ele.split(keyvalSeparator);
				if(2>entry.length) continue;
				Field field=findField(entityType, entry[0].trim());
				Object fieldValue=transferType(entry[1].trim(), field.getType());
				field.set(r, fieldValue);
			}
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将字节输入流转换为缓冲字符输入流
	 * @param inStream 字节流
	 * @return 缓冲字符流
	 */
	public static BufferedReader getBufferReader(InputStream inStream) {
		return getBufferReader(inStream,"UTF-8");
	}
	
	/**
	 * 将字节输入流转换为缓冲字符输入流
	 * @param inStream 字节流
	 * @param charset 转换字符集
	 * @return 缓冲字符流
	 */
	public static BufferedReader getBufferReader(InputStream inStream,String charset) {
		try {
			return new BufferedReader(new InputStreamReader(inStream,charset));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将字节数组反序列化为对象
	 * @param b 字节数组
	 * @param returnTypes 返回类型
	 * @return 泛化类型
	 */
	public static <R> R deserialize(byte[] b,Class<R>... returnTypes) {
		ByteArrayInputStream bais=new ByteArrayInputStream(b);
		try{
			R r=deserialize(bais,returnTypes);
			return r;
		}finally{
			try {
				if(null!=bais) bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 从指定输入流反序列化对象
	 * @param is 字节输入流
	 * @param returnTypes 返回类型
	 * @return 泛化类型
	 */
	public static <R> R deserialize(InputStream is,Class<R>... returnTypes) {
		Class<?> returnType=isEmpty(returnTypes)?Object.class:returnTypes[0];
		ObjectInputStream ois=null;
		try {
			ois=new ObjectInputStream(is);
			R r= (R)returnType.cast(ois.readObject());
			return r;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try{
				if(null!=ois) ois.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 对象序列化
	 * @param object 对象
	 * @return 序列化字节数组
	 */
	public static byte[] serializeByBytes(Object object) {
		ByteArrayOutputStream baos=null;
		try{
			baos=serializeByStream(object);
			byte[] b=baos.toByteArray();
			return b;
		} finally{
			try{
				if(null!=baos) baos.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 对象序列化
	 * @param object 对象
	 * @return 字节数组输出流
	 */
	public static ByteArrayOutputStream serializeByStream(Object object) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		serialize(object,baos);
		return baos;
	}
	
	/**
	 * 对象序列化
	 * @param object 对象
	 * @param os 字节输出流
	 */
	public static void serialize(Object object,OutputStream os) {
		if(isEmpty(object)) return;
		ObjectOutputStream oos=null;
		try {
			oos=new ObjectOutputStream(os);
			oos.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try{
				if(null!=oos) oos.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 随机一个UUID串
	 * @return UUID串
	 */
	public static String randomUUID(int... section){
		if(isEmpty(section)) return UUID.randomUUID().toString().replace("-", "");
		return DASH_HUMP.split(UUID.randomUUID().toString())[section[0]];
	}
	
	/**
	 * 使用当前时间毫秒数和可选前缀随机一个ID串
	 * @return ID串
	 */
	public static String getIDByCurrentTime(String... prefix){
		if(isEmpty(prefix)) return ""+System.currentTimeMillis();
		return new StringBuilder(prefix[0].trim()).append(System.currentTimeMillis()).toString();
	}
	
	/**
	 * 使用可选前缀随机一个整数串
	 * @param range 随机数的范围
	 * @param prefix 随机数的前缀
	 * @return 返回1~range之间的整数
	 * @description 返回的整数位与参数range的位数相同,位数不足则在前面补0
	 */
	public static String randomInt(int range,String... prefix){
		int numSize=(range+"").length();
		Random random=new Random();
		String ranNum=random.nextInt(range)+"";
		
		int loopTimes=numSize-ranNum.length();
		StringBuilder builder=new StringBuilder("");
		for(int i=0;i<loopTimes;builder.append("0"),i++);
		builder.append(ranNum);
		
		if(isEmpty(prefix)) return builder.toString();
		return prefix[0].trim()+builder.toString();
	}
    
    /**
     * 将字符串首字母转换成小写
     * @param src 字符串
     * @return 小写首字母的字符串
     */
    public static String lowerFirstChar(String src){
    	if(null==src || src.trim().isEmpty()) return src;
    	String string=src.trim();
    	char firstChar=string.charAt(0);
    	return Character.toLowerCase(firstChar)+string.substring(1);
    }
    
    /**
     * 将字符串首字母转换成大写
     * @param src 字符串
     * @return 小写首字母的字符串
     */
    public static String upperFirstChar(String src){
    	if(null==src || src.trim().isEmpty()) return src;
    	String string=src.trim();
    	char firstChar=string.charAt(0);
    	return Character.toUpperCase(firstChar)+string.substring(1);
    }
    
    /**
     * 获取指定类型的简单对象名
     * @param type 类型
     * @return 简单对象名
     */
    public static String getBeanName(Class<?> type){
    	return lowerFirstChar(type.getSimpleName());
    }
    
    /**
     * 获取全类名对应的简单对象名
     * @param className 全类名
     * @return 简单对象名
     */
    public static String getBeanName(String className){
    	int lastIndex=className.lastIndexOf(".");
    	String simpleClassName=className.substring(lastIndex+1);
    	return lowerFirstChar(simpleClassName);
    }
    
    /**
     * 根据字段获取get方法名
     * @param field 字段
     * @return get方法名
     */
    public static String getGetMethodNameFromFieldName(Field field){
    	return getGetMethodNameFromFieldName(field.getName());
    }
    
    /**
     * 根据字段名称获取get方法名
     * @param fieldName 字段名称
     * @return get方法名
     */
    public static String getGetMethodNameFromFieldName(String fieldName){
    	return "get"+upperFirstChar(fieldName);
    }
    
    /**
     * 根据字段获取set方法名
     * @param field 字段
     * @return set方法名
     */
    public static String getSetMethodNameFromFieldName(Field field){
    	return getSetMethodNameFromFieldName(field.getName());
    }
    
    /**
     * 根据字段名称获取set方法名
     * @param fieldName 字段名称
     * @return set方法名
     */
    public static String getSetMethodNameFromFieldName(String fieldName){
    	return "set"+upperFirstChar(fieldName);
    }
    
    /**
     * 从JavaBean的get/set方法中提取字段名称
     * @param method get/set方法
     * @return 字段名称
     */
    public static String getFieldNameFromGetSetMethod(Method getsetMethod){
    	return getFieldNameFromGetSetMethod(getsetMethod.getName());
    }
    
    /**
     * 从JavaBean的get/set方法名中提取字段名称
     * @param getsetMethodName get/set方法名
     * @return 字段名称
     */
    public static String getFieldNameFromGetSetMethod(String getsetMethodName){
    	String tmpAttrName=getsetMethodName.substring(3);
    	return lowerFirstChar(tmpAttrName);
    }
    
    /**
     * 获取指定类的直接超类的泛型参数类型表
     * @param subClass 子类
     * @param defaultClass 默认泛型参数类型
     * @return 泛型参数类型表
     */
    public static Class<?>[] getSuperClassGenericArgument(Class<?> subClass,Class<?>... defaultClass) {
    	Class<?>[] defaultClassArray=null==defaultClass||0==defaultClass.length?null:defaultClass;
    	if(null==subClass || Object.class==subClass) return defaultClassArray;
    	Type genericSuperclass = subClass.getGenericSuperclass();
    	if (!ParameterizedType.class.isInstance(genericSuperclass)) return defaultClassArray;
		Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
		if(null==actualTypeArguments||0==actualTypeArguments.length) return defaultClassArray;
		Class<?>[] classArray=new Class<?>[actualTypeArguments.length];
		System.arraycopy(actualTypeArguments, 0, classArray, 0, classArray.length);
		return classArray;
	}
    
    /**
     * 判断对象是否为空
     * @param object 对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object object){
    	return 0==getLength(object,true);
    }
    
    /**
     * 判断对象是否为非空
     * @param object 对象
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Object object){
    	return 0!=getLength(object,true);
    }
    
    /**
     * 判断对象数组中的每个对象是否都为空
     * @param objects 对象数组
     * @return 是否所有对象都为空
     */
    public static boolean isAllEmpty(Object... objects){
    	for(Object object:objects) if(0!=getLength(object,true)) return false;
    	return true;
    }
    
    /**
     * 判断对象数组中的每个对象是否都为非空
     * @param objects 对象数组
     * @return 是否所有对象都为非空
     */
    public static boolean isAllNotEmpty(Object... objects){
    	for(Object object:objects) if(0==getLength(object,true)) return false;
    	return true;
    }
    
    /**
     * 判断数字类型值是否为空或0
     * @param number
     * @return
     */
    public static boolean isEmpty(Number number){
    	return (null==number || "0".equals(number.toString())) ? true : false;
    }
    
    /**
     * 获取序列、字典或实体的长度
     * @param object 序列或字典
     * @param nullIsZeros NULL是否等效于0长度
     * @return 序列或字典的长度
     * @description 序列的长度是序列中元素的数量,字典的长度是字典中映射的数量,实体的长度是实体中字段的数量
     */
    public static Integer getLength(Object object,Boolean... nullIsZeros){
    	boolean nullIsZero=null==nullIsZeros||0==nullIsZeros.length?false:nullIsZeros[0];
    	if(null==object) return nullIsZero?0:null;
    	if(Map.class.isInstance(object)){
    		return ((Map)object).size();
    	} else if(object.getClass().isArray()){
    		return Array.getLength(object);
    	} else if(String.class.isInstance(object)){
    		return object.toString().trim().length();
    	} else if(CharSequence.class.isInstance(object)){
    		return ((CharSequence)object).length();
    	} else if(Iterable.class.isInstance(object)){
    		int counter=0;
    		Iterator its=((Iterable)object).iterator();
    		for(;its.hasNext();counter++,its.next());
    		return counter;
    	} else{
    		return object.getClass().getDeclaredFields().length;
    	}
    }
    
    /**
     * 将对象中的字段值包装为一个对象数组
     * @param bean 对象
     * @return 对象数组
     */
    public static Object[] toArray(Object bean){
    	List<Object> list=toList(bean);
    	if(null==list) return null;
    	return list.toArray(new Object[list.size()]);
    }
    
    /**
     * 将对象中的字段值包装为一个对象列表
     * @param bean 对象
     * @return 对象列表
     */
    public static ArrayList<Object> toList(Object bean){
    	if(null==bean) return null;
    	Class<?> type=bean.getClass();
    	Field[] fields=type.getDeclaredFields();
    	ArrayList<Object> list=new ArrayList<Object>();
    	for(Field field:fields){
    		field.setAccessible(true);
    		try {
				list.add(field.get(bean));
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return list;
    }
    
    /**
     * 将对象中的字段名与字段值包装成一个字典对象
     * @param bean 对象
     * @return 字典对象
     */
    public static LinkedHashMap<String,Object> toMap(Object bean){
    	if(null==bean) return null;
    	Class<?> type=bean.getClass();
    	Field[] fields=type.getDeclaredFields();
    	LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
    	for(Field field:fields){
    		field.setAccessible(true);
    		try {
    			map.put(field.getName(), field.get(bean));
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return map;
    }
    
    /**
	 * 字串类型JSON转换为Java对象列表
	 * @param srcJson 字串JSON源
	 * @param elementType List集合元素类型
	 * @return 泛化列表
	 */
	public static <E> LinkedHashSet<E> jsonStrToSet(String srcJson,Class<E> elementType) {
		return new LinkedHashSet<E>(jsonStrToList(srcJson,elementType));
	}
	
	/**
	 * 字串类型JSON转换为Java对象列表
	 * @param srcJson 字串JSON源
	 * @param elementType List集合元素类型
	 * @return 泛化列表
	 */
	public static <E> ArrayList<E> jsonStrToList(String srcJson,Class<E> elementType) {
		ArrayList<?> list=jsonStrToJava(srcJson,ArrayList.class);
		if(null==list || list.isEmpty()) return null;
		if(Object.class==elementType) return (ArrayList)list;
		
		ArrayList<E> returnList=new ArrayList<E>();
		try {
			if(isSimpleType(elementType)){
				for(Object object:list) returnList.add(transferType(object, elementType));
				return returnList;
			}
			
			if(Map.class.isAssignableFrom(elementType)){
				for(Object object:list)returnList.add((E)object);
				return returnList;
			}
			
			for(Object object:list){
				E e=elementType.newInstance();
				Set<Entry<String, Object>> entrys=((Map<String,Object>)object).entrySet();
				for(Entry<String, Object> entry:entrys){
					Field field=findField(elementType,entry.getKey());
					if(null==field) continue;
					field.set(e, transferType(entry.getValue(),field.getType()));
				}
				returnList.add(e);
			}
			return returnList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 字串类型JSON转换为Java对象
	 * @param srcJson 字串JSON源
	 * @param returnType 返回类型
	 * @return 泛化类型
	 */
	public static <R> R jsonStrToJava(String srcJson,Class<R> returnType) {
		if(null==srcJson) return null;
		
		Object mapper=getObjectMapper();
		if(null==mapper) return null;
		
		Method targetMethod=getObjectMapperMethod("readValue");
		if(null==targetMethod) return null;
		
		try {
			return (R)targetMethod.invoke(mapper, srcJson,returnType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Java对象转换为字串类型JSON
	 * @param object java对象
	 * @return 字串类型
	 */
	public static String javaToJsonStr(Object object) {
		if(null==object) return null;
		
		Object mapper=getObjectMapper();
		if(null==mapper) return null;
		
		Method targetMethod=getObjectMapperMethod("writeValueAsString");
		if(null==targetMethod) return null;
		
		try {
			return (String)targetMethod.invoke(mapper, object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取ObjectMapper类中的方法
	 * @param methodName 方法名
	 * @return 方法对象
	 */
	public static Method getObjectMapperMethod(String methodName) {
		if(null==methodName || (methodName=methodName.trim()).isEmpty()) return null;
		Method method=ApplicationUtil.getBean(methodName,Method.class);
		if(null!=method) return method;
		
		Object mapper=getObjectMapper();
		if(null==mapper) return null;
		
		Method targetMethod=null;
		if("readValue".equals(methodName)) {
			targetMethod=findMethod(mapper.getClass(),"readValue",String.class,Class.class);
		}else if("writeValueAsString".equals(methodName)) {
			targetMethod=findMethod(mapper.getClass(),"writeValueAsString",Object.class);
		}
		
		if(null!=targetMethod) ApplicationUtil.registerSingleton(methodName, targetMethod);
		return targetMethod;
	}
	
	/**
	 * 将JavaBean类型转换为字典Map类型
	 * @description 按公有get方法转化
	 * @param bean Bean类型
	 * @return 字典对象
	 */
	public static LinkedHashMap<String,Object> beanToMap(Class beanType){
		if(null==beanType) return null;
		LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
		HashMap<String, Method> getMethodDict=findGetMethods(beanType);
		for(Map.Entry<String, Method> entry:getMethodDict.entrySet()) {
			Object fieldValue=null;
			try {
				fieldValue=entry.getValue().invoke(beanType);
				if(null==fieldValue) continue;
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(getFieldNameFromGetSetMethod(entry.getKey()), fieldValue);
		}
		return map;
	}
	
	/**
	 * 将JavaBean类型转换为字典Map类型
	 * @description 按公有get方法转化
	 * @param bean Bean对象
	 * @return 字典对象
	 */
	public static LinkedHashMap<String,Object> beanToMap(Object bean){
		if(null==bean) return null;
		LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
		HashMap<String, Method> getMethodDict=findGetMethods(bean.getClass());
		for(Map.Entry<String, Method> entry:getMethodDict.entrySet()) {
			Object fieldValue=null;
			try {
				fieldValue=entry.getValue().invoke(bean);
				if(null==fieldValue) continue;
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(getFieldNameFromGetSetMethod(entry.getKey()), fieldValue);
		}
		return map;
	}
	
	/**
	 * 将Entity类型转换为字典Map类型
	 * @description 按所有声明字段转化
	 * @param entity 实体对象
	 * @return 字典对象
	 */
	public static LinkedHashMap<String,Object> entityToMap(Class entityType){
		if(null==entityType) return null;
		HashMap<String,Field> fields=findFields(entityType);
		LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
		for(Map.Entry<String,Field> fieldEntry:fields.entrySet()){
			try {
				map.put(fieldEntry.getKey(), fieldEntry.getValue().get(entityType));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	/**
	 * 将Entity类型转换为字典Map类型
	 * @description 按所有声明字段转化
	 * @param entity 实体对象
	 * @return 字典对象
	 */
	public static LinkedHashMap<String,Object> entityToMap(Object entity){
		if(null==entity) return null;
		HashMap<String,Field> fields=findFields(entity.getClass());
		LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
		for(Map.Entry<String,Field> fieldEntry:fields.entrySet()){
			try {
				map.put(fieldEntry.getKey(), fieldEntry.getValue().get(entity));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	/**
	 * 将字典Map类型转换为JavaBean类型
	 * @description 按公有set方法转化
	 * @param map 字典对象
	 * @param type Bean类型
	 * @return Bean对象
	 */
	public static <R> R mapToBean(Map map,Class<R> type){
		R r=null;
		try {
			r = type.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		HashMap<String, Set<Method>> setMethodMap=findSetMethods(type);
		Set<Map.Entry> entrys=map.entrySet();
		for(Map.Entry entry:entrys){
			String setMethodName=getSetMethodNameFromFieldName((String)entry.getKey());
			Set<Method> methodSet=setMethodMap.get(setMethodName);
			if(null==methodSet||0==methodSet.size()) continue;
			
			Method targetMethod=null;
			Object value=entry.getValue();
			for(Method method:methodSet){
				if(!compatible(method.getParameterTypes()[0], value.getClass())) continue;
				targetMethod=method;
				break;
			}
			if(null==targetMethod) continue;
			
			try {
				targetMethod.invoke(r, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return r;
	}
	
	/**
	 * 将字典Map类型转换为Entity类型
	 * @description 按所有声明字段转化
	 * @param map 字典对象
	 * @param type 实体类型
	 * @return 实体对象
	 */
	public static <R> R mapToEntity(Map map,Class<R> type){
		R r=null;
		try {
			r = type.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		HashMap<String, Field> fieldDict=findFields(type);
		Set<Map.Entry> entrys=map.entrySet();
		for(Map.Entry entry:entrys){
			Field field=fieldDict.get(entry.getKey());
			if(null==field) continue;
			try {
				field.set(r, transferType(entry.getValue(),field.getType()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return r;
	}
	
	/**
	 * 将可迭代类型转换为字典类型
	 * @param iterable 可迭代对象
	 * @return 字典类型
	 */
	public static LinkedHashMap<Object,Object> iterableToMap(Iterable<Map.Entry> iterable){
		LinkedHashMap<Object,Object> map=new LinkedHashMap<Object,Object>();
		for(Map.Entry entry:iterable){
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 将字典类型转换为集合类型
	 * @param map 字典对象
	 * @param collectionType 集合类
	 * @return 集合类型
	 */
	public static <R extends Collection> R MapToCollection(Map map,Class<R> collectionType){
		if(Set.class.isAssignableFrom(collectionType)){
			return (R)new LinkedHashSet(map.entrySet());
		}
		ArrayList list=new ArrayList();
		for(Object object:map.entrySet()) list.add(object);
		return (R)list;
	}
	
	/**
	 * 将数组类型转换为列表类型
	 * @param array 数组类型
	 * @return 列表类型
	 */
	public static ArrayList arrayToList(Object array){
		if(null==array) return null;
		Class<?> type=array.getClass();
		if(!type.isArray()) return new ArrayList(Arrays.asList(array));
		ArrayList list=new ArrayList();
		int arrLen=Array.getLength(array);
		for(int i=0;i<arrLen;list.add(Array.get(array, i)),i++);
		return list;
	}
	
	/**
	 * 将数组类型转换为字典类型
	 * @param array 数组类型
	 * @return 字典类型
	 */
	public static LinkedHashMap<String,Object> arrayToMap(Map.Entry[] array){
		if(null==array) return null;
		LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
		for(Map.Entry entry:array){
			Object key=entry.getKey();
			map.put(null==key?null:key.toString(), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 将字典类型转换为数组类型
	 * @param map 字典类型
	 * @return 数组类型
	 */
	public static Map.Entry[] mapToArray(Map map){
		Set<Map.Entry> set=map.entrySet();
		return set.toArray(new Map.Entry[map.size()]);
	}
	
	/**
	 * 将可迭代类型转换为Entity类型
	 * @description 按所有声明字段转化
	 * @param iterable 可迭代对象
	 * @param type 实体类型
	 * @return 实体对象
	 */
	public static <R> R iterableToEntity(Iterable iterable,Class<R> type){
		LinkedHashMap<Object,Object> map=iterableToMap(iterable);
		 return mapToEntity(map,type);
	}
	
	/**
	 * 将Entity类型转换为集合类型
	 * @description 按所有声明字段转化
	 * @param entity 实体对象
	 * @param collectionType 集合类型
	 * @return 集合类型
	 */
	public static <R> R entityToCollection(Object entity,Class<R> collectionType){
		LinkedHashMap<String,Object> map=entityToMap(entity);
		if(Set.class.isAssignableFrom(collectionType)){
			return (R)new LinkedHashSet(map.entrySet());
		}
		ArrayList list=new ArrayList();
		for(Object object:map.entrySet()) list.add(object);
		return (R)list;
	}
	
	/**
	 * 将第一个Entity参数对象的值转化到第二个参数目标类型对象中
	 * @param srcEntity 源对象类型
	 * @param dstType 目标实体类型
	 * @return 泛化类型
	 */
	public static <R> R entityToEntity(Object srcEntity,Class<R> dstType){
		R r=null;
		try {
			r = dstType.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mergeTo(srcEntity,r);
		return r;
	}
	
	/**
	 * 将第一个Entity参数对象的值合并到第二个参数Entity对象中
	 * @param srcEntity 源对象
	 * @param dstEntity 目标对象
	 */
	public static void mergeTo(Object srcEntity,Object dstEntity){
		if(null==srcEntity || null==dstEntity) return;
		if(Class.class.isInstance(srcEntity) || Class.class.isInstance(dstEntity)) return;
		
		HashMap<String,Field> srcFieldDict=findFields(srcEntity.getClass());
		HashMap<String,Field> dstFieldDict=findFields(dstEntity.getClass());
		if(null==srcFieldDict || null==dstFieldDict || 0==srcFieldDict.size() || 0==dstFieldDict.size()) return;
		
		for(Map.Entry<String, Field> srcFieldEntry:srcFieldDict.entrySet()) {
			Field dstField=dstFieldDict.get(srcFieldEntry.getKey());
			if(null==dstField) continue;
			try {
				dstField.set(dstEntity, transferType(srcFieldEntry.getValue(),dstField.getType()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取SpringBoot的JSON工具
	 * @return ObjectMapper对象
	 */
	public static Object getObjectMapper() {
		Object object=ApplicationUtil.getBean("objectMapper",Object.class);
		if(null!=object) return object;
		Class<?> mapperType=ClassLoaderUtil.loadType(JSON_UTIL_CLASS);
		if(null==mapperType) return null;
		return getObjectMapper(mapperType);
	}
	
	/**
	 * 获取SpringBoot的JSON工具
	 * @param type ObjectMapper类型
	 * @return ObjectMapper对象
	 */
	public static <R> R getObjectMapper(Class<R> type) {
		if(!JSON_UTIL_CLASS.equals(type.getName())) return null;
		R r=ApplicationUtil.getRandomBean(type);
		if(null!=r) return r;
		try {
			ApplicationUtil.registerSingleton("objectMapper", r=type.newInstance());
			return r;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 短横线命名法转驼峰命名法
	 * @param name 短横线参数名或字段名
	 * @return 实体类属性名
	 */
	public static String dashToHump(String name){
		String[] parts=DASH_HUMP.split(name);
		if(1 == parts.length)return parts[0];
		StringBuilder builder=new StringBuilder(parts[0]);
		for(int i=1;i<parts.length;i++){
			String part=parts[i];
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}
    
    /**
     * JAVA类型之间的相互转换
     * @param object 被转换的源对象
     * @param returnType 返回类型
     * @param keyType 集合下标类型
     * @param elementTypes 集合元素类型
     * @return 泛化类型
     */
	public static <R,E> R objectToType(Object object,Class<R> returnType,Class<?> keyType,Class<E>... elementTypes) throws Exception{
		if(null==object) return null;
		if(null==returnType || Object.class==returnType) return (R)object;
		
		Class<?> srcType=object.getClass();
		if(returnType.isAssignableFrom(srcType)) return (R)object;
		
    	String stringValue=toString(object);
    	if(null==stringValue) return null;
    	
    	if(null==keyType) keyType=Object.class;
    	Class<?> elementType=null==elementTypes||0==elementTypes.length?Object.class:elementTypes[0];
    	if(String.class.isAssignableFrom(returnType)){
    		if(srcType.isArray()||Collection.class.isAssignableFrom(srcType)||Map.class.isAssignableFrom(srcType))
    		return (R)javaToJsonStr(object);
            return (R)stringValue;
        }else if(isNumber(returnType)){
        	String parseValue=null;
        	if(String.class.isAssignableFrom(srcType) || isNumber(srcType)) {
        		parseValue=stringValue;
        	}else if(Date.class.isAssignableFrom(srcType)){
        		parseValue=DateUtil.dateToMillSeconds((Date)object)+"";
        	}else if(Calendar.class.isAssignableFrom(srcType)){
        		parseValue=DateUtil.calendarToMillSeconds((Calendar)object)+"";
        	}else{
        		throw new RuntimeException("array, collection, and compound types are not supported to convert numeric types!!!");
        	}
        	Class<?> wrapType=getWrapType(returnType);
        	 return (R)wrapType.getConstructor(String.class).newInstance(parseValue);
        }else if(Date.class.isAssignableFrom(returnType)){
        	if(String.class.isAssignableFrom(srcType) || Date.class.isAssignableFrom(srcType)) {
        		return (R)DateUtil.stringToDate(stringValue, (Class<Date>)returnType);
        	}else if(isNumber(srcType)){
        		return (R)DateUtil.millSecondsToDate(((Number)object).longValue(), (Class<Date>)returnType);
        	}else if(Calendar.class.isAssignableFrom(srcType)){
        		return (R)DateUtil.millSecondsToDate(((Calendar)object).getTimeInMillis(), (Class<Date>)returnType);
        	}else{
        		throw new RuntimeException("array, collection, and compound types are not supported to convert date types!!!");
        	}
        }else if(Calendar.class.isAssignableFrom(returnType)){
        	if(String.class.isAssignableFrom(srcType) || Calendar.class.isAssignableFrom(srcType)) {
        		return (R)DateUtil.stringToCalendar(stringValue);
        	}else if(isNumber(srcType)){
        		return (R)DateUtil.millSecondsToCalendar(((Number)object).longValue());
        	}else if(Date.class.isAssignableFrom(srcType)){
        		return (R)DateUtil.dateToCalendar((Date)object);
        	}else{
        		throw new RuntimeException("array, collection, and compound types are not supported to convert calendar types!!!");
        	}
        }else if(Boolean.class==returnType || boolean.class==returnType){
            return (R)Boolean.valueOf(stringValue);
        }else if(Character.class==returnType || char.class==returnType){
            return (R)Character.valueOf(stringValue.charAt(0));
        }else if(returnType.isArray()){
        	Class<?> componentType=returnType.getComponentType();
        	if(String.class.isAssignableFrom(srcType)) {
        		Object[] array=null;
        		if(stringValue.startsWith("[") && stringValue.endsWith("]")){
        			array=jsonStrToJava(stringValue,Object[].class);
        		}else{
        			array=COMMA.split(stringValue);
        		}
        		Object newArray=Array.newInstance(componentType, array.length);
        		for(int i=0;i<array.length;Array.set(newArray, i, objectToType(array[i],componentType,null)),i++);
        		return (R)newArray;
        	}else if(isNumber(componentType) && (Date.class.isAssignableFrom(srcType) || Calendar.class.isAssignableFrom(srcType))) {
        		Calendar calendar=null;
        		if(Calendar.class.isAssignableFrom(srcType)) {
        			calendar=(Calendar)object;
        		}else{
        			calendar=DateUtil.dateToCalendar((Date)object);
        		}
        		Integer[] array=(Integer[])DateUtil.getCalendarFields(calendar, Integer.class);
        		Object newArray=Array.newInstance(componentType, array.length);
        		for(int i=0;i<array.length;Array.set(newArray, i, objectToType(array[i],componentType,null)),i++);
        		return (R)newArray;
        	}else if(isNumber(srcType) && isNumber(componentType)){
        		Object newArray=Array.newInstance(componentType, 1);
        		Array.set(newArray, 0, objectToType(object,componentType,null));
        		return (R)newArray;
        	}else if(srcType.isArray()){
        		int len=Array.getLength(object);
        		Object newArray=Array.newInstance(componentType, len);
        		for(int i=0;i<len;Array.set(newArray, i, objectToType(Array.get(object, i),componentType,null)),i++);
        		return (R)newArray;
        	}else if(Collection.class.isAssignableFrom(srcType)){
        		int i=0;
        		Collection cols=(Collection)object;
        		Object newArray=Array.newInstance(componentType, cols.size());
        		for(Object ele:cols) Array.set(newArray, i++, objectToType(ele,componentType,null));
        		return (R)newArray;
        	}else{
        		int i=0;
        		Set set=null;
        		if(Map.class.isAssignableFrom(srcType)){
        			set=((Map)object).entrySet();
        		}else{
        			set=entityToMap(object).entrySet();
        		}
        		Object newArray=Array.newInstance(componentType, set.size());
        		for(Object ele:set) Array.set(newArray, i++, objectToType(ele,componentType,null));
        		return (R)newArray;
        	}
        }else if(Collection.class.isAssignableFrom(returnType)){
        	Collection cols=null;
        	if(!returnType.isInterface()){
        		cols = (Collection)returnType.newInstance();
        	}else if(returnType.isAssignableFrom(ArrayList.class)){
        		cols=new ArrayList(); 
        	}else if(returnType.isAssignableFrom(HashSet.class)){
        		cols=new HashSet();
        	}else if(returnType.isAssignableFrom(LinkedList.class)){
        		cols=new LinkedList();
        	}else{
        		cols=new TreeSet();
        	}
        	
        	if(String.class.isAssignableFrom(srcType)) {
        		Object[] array=null;
        		if(stringValue.startsWith("[") && stringValue.endsWith("]")){
        			array=jsonStrToJava(stringValue,Object[].class);
        		}else{
        			array=COMMA.split(stringValue);
        		}
        		cols.addAll(Arrays.asList(array));
        		return (R)cols;
        	}else if(Date.class.isAssignableFrom(srcType) || Calendar.class.isAssignableFrom(srcType)) {
        		Calendar calendar=null;
        		if(Calendar.class.isAssignableFrom(srcType)) {
        			calendar=(Calendar)object;
        		}else{
        			calendar=DateUtil.dateToCalendar((Date)object);
        		}
        		Integer[] array=(Integer[])DateUtil.getCalendarFields(calendar, Integer.class);
        		for(int i=0;i<array.length;cols.add(objectToType(array[i],elementType,null)),i++);
        		return (R)cols;
        	}else if(isNumber(srcType) && isNumber(elementType)){
        		cols.add(objectToType(object,elementType,null));
        		return (R)cols;
        	}else if(srcType.isArray()){
        		int len=Array.getLength(object);
        		for(int i=0;i<len;cols.add(objectToType(Array.get(object, i),elementType,null)),i++);
        		return (R)cols;
        	}else if(Collection.class.isAssignableFrom(srcType)) {
        		for(Object element:(Collection)object) cols.add(objectToType(element,elementType,null));
        		return (R)cols;
        	}else{
        		Set set=null;
        		if(Map.class.isAssignableFrom(srcType)){
        			set=((Map)object).entrySet();
        		}else{
        			set=entityToMap(object).entrySet();
        		}
        		cols.addAll(set);
        		return (R)cols;
        	}
        }else if(Map.class.isAssignableFrom(returnType)){
        	Map map=null;
        	if(!returnType.isInterface()){
        		map=(Map)returnType.newInstance();
        	}else if(returnType.isAssignableFrom(HashMap.class)){
        		map=new HashMap();
        	}else if(returnType.isAssignableFrom(TreeMap.class)){
        		map=new TreeMap();
        	}else if(returnType.isAssignableFrom(ConcurrentHashMap.class)){
        		map=new ConcurrentHashMap();
        	}else{
        		map=new Hashtable();
        	}
        	
        	if(String.class.isAssignableFrom(srcType)){
        		Map srcMap=null;
        		if(stringValue.startsWith("{") && stringValue.endsWith("}")){
        			srcMap=jsonStrToJava(stringValue,Map.class);
        		}else{
        			String[] entrys=ELEMENT_SEPARATOR.split(stringValue);
        			srcMap=new HashMap();
        			for(String entry:entrys){
        				String[] keyVal=KEYVAL_SEPARATOR.split(entry);
        				if(keyVal.length<2) continue;
        				srcMap.put(keyVal[0].trim(), keyVal[1].trim());
        			}
        		}
        		for(Map.Entry entry:(Set<Map.Entry>)srcMap.entrySet())
        		map.put(objectToType(entry.getKey(),keyType,null), objectToType(entry.getValue(),elementType,null));
        		return (R)map;
        	}else if(Date.class.isAssignableFrom(srcType) || Calendar.class.isAssignableFrom(srcType)) {
        		Calendar calendar=null;
        		if(Calendar.class.isAssignableFrom(srcType)) {
        			calendar=(Calendar)object;
        		}else{
        			calendar=DateUtil.dateToCalendar((Date)object);
        		}
        		HashMap<String,Integer> tmpMap=DateUtil.getCalendarFields(calendar);
        		for(Map.Entry<String, Integer> entry:tmpMap.entrySet())
        		map.put(objectToType(entry.getKey(),keyType,null), objectToType(entry.getValue(),elementType,null));
        		return (R)map;
        	}else if(isNumber(srcType)){
        		map.put(objectToType("number",keyType,null), objectToType(object,elementType,null));
        		return (R)map;
        	}else if(srcType.isArray()){
        		int length=Array.getLength(object);
        		if(0==length) return (R)map;
        		Class<?> componentType=srcType.getComponentType();
        		if(Map.Entry.class.isAssignableFrom(componentType)) {
        			for(int i=0;i<length;i++){
        				Map.Entry entry=(Map.Entry)Array.get(object, i);
        				map.put(objectToType(entry.getKey(),keyType,null), objectToType(entry.getValue(),elementType,null));
        			}
        			return (R)map;
        		}else{
        			for(int i=0;i<length;map.put(objectToType(i,keyType,null), objectToType(Array.get(object, i),elementType,null)),i++);
        			return (R)map;
        		}
        	}else if(Collection.class.isAssignableFrom(srcType)){
        		Iterator iterator=((Collection)object).iterator();
        		for(int i=0;iterator.hasNext();i++){
        			Object tmpObj=iterator.next();
        			if(!Map.Entry.class.isInstance(tmpObj)) {
        				map.put(objectToType(i,keyType,null), objectToType(tmpObj,elementType,null));
        				continue;
        			}
        			Map.Entry entry=(Map.Entry)tmpObj;
    				map.put(objectToType(entry.getKey(),keyType,null), objectToType(entry.getValue(),elementType,null));
        		}
        		return (R)map;
        	}else {
        		Set entrys=null;
        		if(Map.class.isAssignableFrom(srcType)){
        			entrys=((Map)object).entrySet();
        		}else{
        			entrys=entityToMap(object).entrySet();
        		}
        		for(Object ele:entrys){
        			Map.Entry entry=(Map.Entry)ele;
        			map.put(objectToType(entry.getKey(),keyType,null), objectToType(entry.getValue(),elementType,null));
        		}
        		return (R)map;
        	}
        }else{
        	if(String.class.isAssignableFrom(srcType)){
        		return jsonStrToJava(stringValue,returnType);
        	}else if(Date.class.isAssignableFrom(srcType) || Calendar.class.isAssignableFrom(srcType) || isNumber(srcType)){
        		throw new RuntimeException("java.util.Date/java.util.Calendar/java.lang.Number cannot convert to entity type!!!");
        	}else if(srcType.isArray()){
        		Class<?> componentType=srcType.getComponentType();
        		if(!Map.Entry.class.isAssignableFrom(componentType)){
        			throw new RuntimeException("array to entity conversion is not supported unless the component type is Map.Entry!!!");
        		}
        		
        		R r=returnType.newInstance();
        		HashMap<String, Field> fieldDict=findFields(returnType);
        		int len=Array.getLength(object);
    			for(int i=0;i<len;i++){
    				Map.Entry entry=(Map.Entry)Array.get(object, i);
    				if(null==entry) continue;
    				Object key=entry.getKey();
    				if(null==key || String.class!=key.getClass()) continue;
    				String fieldName=((String)key).trim();
    				if(0==fieldName.length()) continue;
    				Field field=fieldDict.get(fieldName);
    				if(null==field) continue;
    				field.set(r, objectToType(entry.getValue(),field.getType(),null));
    			}
    			return r;
        	}else if(Collection.class.isAssignableFrom(srcType)){
        		R r=returnType.newInstance();
        		Iterator iterator=((Collection)object).iterator();
        		HashMap<String, Field> fieldDict=findFields(returnType);
        		while(iterator.hasNext()){
        			Object tmpObj=iterator.next();
        			if(null==tmpObj) continue;
        			if(!Map.Entry.class.isInstance(tmpObj)) continue;
        			Map.Entry entry=(Map.Entry)tmpObj;
    				Object key=entry.getKey();
    				if(null==key || String.class!=key.getClass()) continue;
    				String fieldName=((String)key).trim();
    				if(0==fieldName.length()) continue;
    				Field field=fieldDict.get(fieldName);
    				if(null==field) continue;
    				field.set(r, objectToType(entry.getValue(),field.getType(),null));
        		}
        		return r;
        	}else if(Map.class.isAssignableFrom(srcType)) {
        		return mapToEntity((Map)object,returnType);
        	}else{
        		return entityToEntity(object,returnType);
        	}
        }
    }
	
	/**
	 * 从JSON表达中获取参数键映射的值
	 * @param src JSON表达源(字串、字典或实体)
	 * @param key 键
	 * @param defaultValues 默认值
	 * @return 泛化类型
	 */
	public static Object getValue(Object src,Object key,Object... defaultValues){
		return getValue(src,key,Object.class,defaultValues);
	}
	
	/**
	 * 从JSON表达中获取参数键映射的值
	 * @param src JSON表达源
	 * @param key 键
	 * @param valueType 返回类型
	 * @param defaultValues 默认值
	 * @return 泛化类型
	 */
	public static <R> R getValue(Object src,Object key,Class<R> valueType,R... defaultValues){
		return getValue(src,key,null,valueType,defaultValues);
	}
	
	/**
	 * 从JSON表达中获取参数键映射的值
	 * @param src JSON表达源
	 * @param key 键
	 * @param defaultKey 默认键
	 * @param valueType 返回类型
	 * @param defaultValues 默认值
	 * @return 泛化类型
	 */
	public static <R> R getValue(Object src,Object key,Object defaultKey,Class<R> valueType,R... defaultValues){
		if(null==src||null==key) return null;
		R defaultValue=null==defaultValues||0==defaultValues.length?null:defaultValues[0];
		
		Object result=null;
		Map<String, Object> dict=null;
		try {
			dict = transferType(src,Map.class);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		
		if(null==(result=dict.get(key))&&null!=defaultKey) result=dict.get(defaultKey);
		if(null==result && null!=defaultValue) return defaultValue;
		if(null==result) return null;
		
		try {
			return transferType(result,valueType);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取EL表达式的值
	 * @param src EL对象源
	 * @param elExpression EL表达式
	 * @param defaultValues 默认值
	 * @return 对象值
	 */
	public static Object getELValue(Object src,String elExpression,Object... defaultValues){
		return getELValue(src,elExpression,Object.class,defaultValues);
	}
	
	/**
	 * 获取EL表达式的值
	 * @param src EL对象源
	 * @param elExpression EL表达式
	 * @param returnType 返回类型
	 * @param defaultValues 默认值
	 * @return 对象值
	 */
	public static <R> R getELValue(Object src,String elExpression,Class<R> returnType,R... defaultValues){
		R defaultValue=null==defaultValues||0==defaultValues.length?null:defaultValues[0];
		if(null==src) return defaultValue;
		if(!elExpression.startsWith("${")||!elExpression.endsWith("}"))  throw new RuntimeException("error! not be EL expression...");
		String el=elExpression.substring(2, elExpression.length()-1);
		Object result=getOgnlValue(src,el);
		if(null==result) return defaultValue;
		try {
			return transferType(result,returnType);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取OGNL对象图中的属性值
	 * @description 获取对象导航图中的值,暂不支持集合投影
	 * @param src ognl源对象(字串、字典或实体)
	 * @param ognlExpression ognl表达式 
	 * @return 对象值
	 */
	public static Object getOgnlValue(Object src,String ognlExpression){
		if(null==src||null==ognlExpression||ognlExpression.trim().isEmpty()) return null;
		String ognl=ognlExpression.trim();
		int dotIndex=ognl.indexOf(".");
		int squareIndex=ognl.indexOf("[");
		
		int endIndex=-1;
		if(-1!=dotIndex && -1!=squareIndex){
			endIndex=dotIndex<squareIndex?dotIndex:squareIndex;
		}else if(-1!=dotIndex) {
			endIndex=dotIndex;
		}else{
			endIndex=squareIndex;
		}
		
		String nextKey=null;
		String currentKey=null;
		if(-1==endIndex){
			currentKey=ognl;
		}else if(0!=endIndex){
			currentKey=ognl.substring(0, endIndex).trim();
			nextKey=ognl.substring(endIndex+1).trim();
		}else{
			return getOgnlValue(src,ognlExpression.substring(1));
		}
		
		int arrayIndex=-1;
		String fieldName=null;
		if(!currentKey.endsWith("]")){
			fieldName=currentKey;
		}else{
			String temp=currentKey.substring(0, currentKey.length()-1).trim();
			if(QUOTATIONS.contains(temp.charAt(0))&&QUOTATIONS.contains(temp.charAt(temp.length()-1))){
				fieldName=temp.substring(1, temp.length()-1).trim();
			}else{
				if(QUOTATIONS.contains(temp.charAt(0))) temp=temp.substring(1, temp.length()).trim();
				if(QUOTATIONS.contains(temp.charAt(temp.length()-1))) temp=temp.substring(0, temp.length()-1).trim();
				if(isNumber(temp)){
					arrayIndex=Integer.parseInt(temp);
				}else{
					fieldName=temp;
				}
			}
		}
		
		Object nextSrc=null;
		if(-1==arrayIndex && null!=fieldName){
			Matcher matcher=METHOD_SIGNATURE.matcher(fieldName);
			if(!matcher.find()){
				nextSrc=getValue(src,fieldName);
			}else{
				String methodName=matcher.group(1);
				String methodParams=matcher.group(2);
				if(null==methodParams||methodParams.trim().isEmpty()){
					Method method=findMethod(src,methodName);
					try {
						nextSrc=method.invoke(src);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}else{
					String[] args=COMMA.split(methodParams);
					Object[] params=new Object[args.length];
					for(int i=0;i<args.length;i++){
						String tmpArgs=args[i].trim();
						if(isNumber(tmpArgs)){
							if(-1==tmpArgs.indexOf(".")){
								params[i]=Integer.parseInt(tmpArgs);
							}else{
								params[i]=Double.parseDouble(tmpArgs);
							}
						}else if(tmpArgs.equals("true")){
							params[i]=true;
						}else if(tmpArgs.equals("false")){
							params[i]=false;
						}else{
							if(QUOTATIONS.contains(tmpArgs.charAt(0))) tmpArgs=tmpArgs.substring(1);
							if(QUOTATIONS.contains(tmpArgs.charAt(tmpArgs.length()-1))) tmpArgs=tmpArgs.substring(0,tmpArgs.length()-1);
							params[i]=tmpArgs;
						}
					}
					Method method=findMethod(src,methodName,params);
					try {
						nextSrc=method.invoke(src,params);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}else if(null==fieldName && -1!=arrayIndex){
			nextSrc=getValue(src,arrayIndex);
		}else{
			return null;
		}
		
		if(null==nextSrc||null==nextKey||nextKey.trim().isEmpty()) return nextSrc;
		return getOgnlValue(nextSrc,nextKey);
	}
	
	/**
	 * 使用srcObject更新dstObject
	 * @param srcObject 被更新的原始对象
	 * @param newObject 用于更新的新对象
	 * @description 将srcObject中的非null字段值更新到dstObject中对应字段上
	 * @return dstObject
	 */
	public static final Map merge(Map dstObject,Map srcObject){
		HashMap copyMap=new HashMap(dstObject);
		Set<Entry> dstEntrys=dstObject.entrySet();
		for(Map.Entry dstEntry:dstEntrys){
			Object dstKey=dstEntry.getKey();
			if(!srcObject.containsKey(dstKey)) continue;
			Object srcValue=srcObject.get(dstKey);
			if(null==srcValue) continue;
			if(!dstEntry.getValue().getClass().isInstance(srcValue)) continue;
			copyMap.put(dstKey, srcValue);
		}
		dstObject.putAll(copyMap);
		return dstObject;
	}
	
	/**
	 * 使用srcObject更新dstObject
	 * @param srcObject 被更新的原始对象
	 * @param newObject 用于更新的新对象
	 * @description 将srcObject中的非null字段值更新到dstObject中对应字段上
	 * @return dstObject
	 */
	public static final <R> R merge(R dstObject,Map<String,Object> srcObject){
		Class<?> dstType=dstObject.getClass();
		Field[] dstFields=dstType.getDeclaredFields();
		try{
			for(Field dstField:dstFields){
				dstField.setAccessible(true);
				String dstFieldName=dstField.getName();
				if(!srcObject.containsKey(dstFieldName)) continue;
				Object srcValue=srcObject.get(dstFieldName);
				if(null==srcValue) continue;
				if(!dstField.getType().isInstance(srcValue)) continue;
				dstField.set(dstObject, srcValue);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dstObject;
	}
	
	/**
	 * 使用srcObject更新dstObject
	 * @param srcObject 被更新的原始对象
	 * @param newObject 用于更新的新对象
	 * @description 将srcObject中的非null字段值更新到dstObject中对应字段上
	 * @return dstObject
	 */
	public static final <R> R merge(R dstObject,R srcObject){
		Class<?> dstType=dstObject.getClass();
		Class<?> srcType=srcObject.getClass();
		Field[] dstFields=dstType.getDeclaredFields();
		try{
			for(Field dstField:dstFields){
				dstField.setAccessible(true);
				String dstFieldName=dstField.getName();
				Field srcField=findField(srcType,dstFieldName);
				if(null==srcField || !dstField.getType().isAssignableFrom(srcField.getType())) continue;
				
				Object srcValue=null;
				if(null==(srcValue=srcField.get(srcObject))) continue;
				dstField.set(dstObject, srcValue);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dstObject;
	}
	
	/**
     * 查找对象中的方法
     * @param target 对象
     * @param method 方法名
     * @param args 参数列表
     * @return 方法对象
     */
	public static final Method findMethod(Object target,String methodName,Object... args){
		final Class[] typeArgs=new Class[null==args?0:args.length];
		if(null!=args && 0!=args.length)for(int i=0;i<args.length;typeArgs[i]=args[i].getClass(),i++);
		return findMethod(target, methodName, typeArgs);
	}
	
	/**
	 * 查找类中的构造方法
	 * @param target 对象
	 * @param args 参数列表
	 * @return 构造方法对象
	 */
	public static final Constructor<?> findConstructor(Object target,Object... args){
		final Class[] typeArgs=new Class[null==args?0:args.length];
		if(null!=args && 0!=args.length)for(int i=0;i<args.length;typeArgs[i]=args[i].getClass(),i++);
		return findConstructor(target, typeArgs);
	}
	
	/**
	 * 查找类或接口中的字段(含参数类对象)
	 * @param type 接口类对象
	 * @param fieldName 字段名
	 * @return 字段对象
	 */
	public static final Field findField(Object type,String fieldName){
		return findDeclaredField(ClassType.ALL,type,fieldName);
	}
	
	/**
	 * 查找接口中的字段(含参数类对象)
	 * @param type 接口类对象
	 * @param fieldName 常量名
	 * @return 字段对象
	 */
	public static final Field findFieldByFace(Object type,String fieldName){
		return findDeclaredField(ClassType.FACE,type,fieldName);
	}
	
	/**
	 * 查找类中的字段(含参数类对象)
	 * @param type 类对象
	 * @param fieldName 属性名
	 * @return 字段对象
	 */
	public static final Field findFieldByClass(Object type,String fieldName){
		return findDeclaredField(ClassType.CLASS,type,fieldName);
	}
	
	/**
	 * 查找类或接口世系树中的字段(含参数类对象)
	 * @param kindType 查找模式
	 * @param classType 查找类型
	 * @param fieldName 字段名称
	 * @return 字段对象
	 */
	public static final Field findDeclaredField(ClassType kindType,Object classType,String fieldName){
		if(null==classType || null==fieldName || fieldName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			Field targetField=null;
			Field[] fields=type.getDeclaredFields();
			for(Field field:fields){
				if(!(fieldName.equals(field.getName()))) continue;
				targetField=field;
				break;
			}
			
			if(null!=targetField) {
				targetField.setAccessible(true);
				return targetField;
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetField=findDeclaredField(kindType,finalSuperClass,fieldName);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetField=findDeclaredField(kindType,finalSuperFaces,fieldName);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetField=findDeclaredField(kindType,finalAllTypes,fieldName);
			}
			
			if(null==targetField) continue;
			return targetField;
		}
		return null;
	}
	
	/**
	 * 查找当前类及超类世系树中的所有字段(含参数类对象)
	 * @param classType 查找类型
	 * @return 字段名称到字段对象的映射字典
	 * @description 扩展类将覆盖基类同名字段
	 */
	public static final HashMap<String,Field> findFieldsByType(Object classType){
		return findFields(ClassType.CLASS,classType);
	}
	
	/**
	 * 查找当前类及超接口世系树中的所有字段(含参数类对象)
	 * @param classType 查找类型
	 * @return 字段名称到字段对象的映射字典
	 * @description 扩展类将覆盖基类同名字段
	 */
	public static final HashMap<String,Field> findFieldsByFace(Object classType){
		return findFields(ClassType.FACE,classType);
	}
	
	/**
	 * 查找当前类或超类及超接口世系树中的所有字段(含参数类对象)
	 * @param classType 查找类型
	 * @return 字段名称到字段对象的映射字典
	 * @description 扩展类将覆盖基类同名字段
	 */
	public static final HashMap<String,Field> findFields(Object classType){
		return findFields(ClassType.ALL,classType);
	}
	
	/**
	 * 查找类或接口世系树中的所有字段(含参数类对象)
	 * @param kindType 查找模式
	 * @param classType 查找类型
	 * @return 字段名称到字段对象的映射字典
	 * @description 扩展类将覆盖基类同名字段
	 */
	public static final HashMap<String,Field> findFields(ClassType kindType,Object classType){
		if(null==classType) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		HashMap<String,Field> subMap=new HashMap<String,Field>();
		for(Class<?> type:types){
			for(Field field:type.getDeclaredFields()) {
				field.setAccessible(true);
				subMap.putIfAbsent(field.getName(), field);
			}
			
			HashMap<String,Field> parentMap=null;
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				parentMap=findFields(kindType,finalSuperClass);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				parentMap=findFields(kindType,finalSuperFaces);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				parentMap=findFields(kindType,finalAllTypes);
			}
			
			if(null==parentMap) continue;
			for(Map.Entry entry:parentMap.entrySet()) subMap.putIfAbsent((String)entry.getKey(), (Field)entry.getValue());
		}
		return subMap;
	}
	
	/**
	 * 查找类或接口中的第一个方法
	 * 若方法重载多次则返回的方法是不确定的
	 * @param type 类或接口类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findFirstMethod(Object type,String methodName){
		return findFirstDeclaredMethod(ClassType.ALL,type,methodName);
	}
	
	/**
	 * 查找类或接口中的方法
	 * @param type 类或接口类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findMethod(Object type,String methodName,Class<?>... paramTypes){
		return findDeclaredMethod(ClassType.ALL,type,methodName,paramTypes);
	}
	
	/**
	 * 查找类中的第一个方法
	 * 若方法重载多次则返回的方法是不确定的
	 * @param type 类类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findFirstMethodByClass(Object type,String methodName){
		return findFirstDeclaredMethod(ClassType.CLASS,type,methodName);
	}
	
	/**
	 * 查找类中的方法
	 * @param type 类类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findMethodByClass(Object type,String methodName,Class<?>... paramTypes){
		return findDeclaredMethod(ClassType.CLASS,type,methodName,paramTypes);
	}
	
	/**
	 * 查找接口中的第一个方法
	 * 若方法重载多次则返回的方法是不确定的
	 * @param type 接口类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findFirstMethodByFace(Object type,String methodName){
		return findFirstDeclaredMethod(ClassType.FACE,type,methodName);
	}
	
	/**
	 * 查找接口中的方法
	 * @param type 接口类型
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findMethodByFace(Object type,String methodName,Class<?>... paramTypes){
		return findDeclaredMethod(ClassType.FACE,type,methodName,paramTypes);
	}
	
	/**
	 * 查找类或接口世系树中的第一个方法(含参数类对象)
	 * @param kindType 递归通道(类、接口、所有)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @return 方法对象
	 */
	public static final Method findFirstDeclaredMethod(ClassType kindType,Object classType,String methodName){
		if(null==classType || null==methodName || methodName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			Method targetMethod=null;
			Method[] methods=type.getDeclaredMethods();
			for(Method method:methods){
				if(!(methodName.equals(method.getName()))) continue;
				targetMethod=method;
				break;
			}
			
			if(null!=targetMethod) {
				targetMethod.setAccessible(true);
				return targetMethod;
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetMethod=findFirstDeclaredMethod(kindType,finalSuperClass,methodName);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetMethod=findFirstDeclaredMethod(kindType,finalSuperFaces,methodName);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetMethod=findFirstDeclaredMethod(kindType,finalAllTypes,methodName);
			}
			
			if(null==targetMethod) continue;
			return targetMethod;
		}
		return null;
	}
	
	/**
	 * 查找类或接口世系树中的方法(含参数类对象)
	 * @param kindType 递归通道(类、接口、所有)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Method findDeclaredMethod(ClassType kindType,Object classType,String methodName,Class<?>... paramTypes){
		if(null==classType || null==methodName || methodName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			Method targetMethod=null;
			Method[] methods=type.getDeclaredMethods();
			out:for(Method method:methods){
				if(!(methodName.equals(method.getName()))) continue out;
				int argCount=method.getParameterCount();
				if(0==argCount) {
					if(null==paramTypes || 0==paramTypes.length){
						targetMethod=method;
						break out;
					}
					continue out;
				}else{
					if(null==paramTypes || argCount!=paramTypes.length) continue out;
					if(!compatible(method.getParameterTypes(),paramTypes))  continue out;
					targetMethod=method;
					break out;
				}
			}
			
			if(null!=targetMethod) {
				targetMethod.setAccessible(true);
				return targetMethod;
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetMethod=findDeclaredMethod(kindType,finalSuperClass,methodName,paramTypes);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetMethod=findDeclaredMethod(kindType,finalSuperFaces,methodName,paramTypes);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetMethod=findDeclaredMethod(kindType,finalAllTypes,methodName,paramTypes);
			}
			
			if(null==targetMethod) continue;
			return targetMethod;
		}
		return null;
	}
	
	/**
	 * 获取类或接口世系树中的所有方法(含参数类对象)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Set<Method> getDeclaredMethods(Object classType,String methodName,Class<?>... paramTypes){
		return getDeclaredMethods(ClassType.ALL,classType,methodName,paramTypes);
	}
	
	/**
	 * 获取类世系树中的所有方法(含参数类对象)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Set<Method> getDeclaredMethodsByClass(Object classType,String methodName,Class<?>... paramTypes){
		return getDeclaredMethods(ClassType.CLASS,classType,methodName,paramTypes);
	}
	
	/**
	 * 获取接口世系树中的所有方法(含参数类对象)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Set<Method> getDeclaredMethodsByFace(Object classType,String methodName,Class<?>... paramTypes){
		return getDeclaredMethods(ClassType.FACE,classType,methodName,paramTypes);
	}
	
	/**
	 * 获取类或接口世系树中的所有方法(含参数类对象)
	 * @param kindType 递归通道(类、接口、所有)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	public static final Set<Method> getDeclaredMethods(ClassType kindType,Object classType,String methodName,Class<?>... paramTypes){
		return getDeclaredMethods(kindType,classType,methodName,new HashSet<Method>(),paramTypes);
	}
	
	/**
	 * 获取类或接口世系树中的所有方法(含参数类对象)
	 * 返回世系树中相同方法名和参数列表的所有方法组成的集合
	 * @param kindType 递归通道(类、接口、所有)
	 * @param classType 类型数组
	 * @param methodName 方法名
	 * @param methodSet 方法集
	 * @param paramsType 参数列表
	 * @return 方法对象
	 */
	private static final Set<Method> getDeclaredMethods(ClassType kindType,Object classType,String methodName,Set<Method> methodSet,Class<?>... paramTypes){
		if(null==classType || null==methodName || methodName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			Method[] methods=type.getDeclaredMethods();
			out:for(Method method:methods){
				if(!(methodName.equals(method.getName()))) continue out;
				int argCount=method.getParameterCount();
				if(0==argCount) {
					if(null==paramTypes || 0==paramTypes.length){
						method.setAccessible(true);
						methodSet.add(method);
						break out;
					}
					continue out;
				}else{
					if(null==paramTypes || argCount!=paramTypes.length) continue out;
					if(!compatible(method.getParameterTypes(),paramTypes))  continue out;
					method.setAccessible(true);
					methodSet.add(method);
					break out;
				}
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				getDeclaredMethods(kindType,finalSuperClass,methodName,methodSet,paramTypes);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				getDeclaredMethods(kindType,finalSuperFaces,methodName,methodSet,paramTypes);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				getDeclaredMethods(kindType,finalAllTypes,methodName,methodSet,paramTypes);
			}
			continue;
		}
		return methodSet;
	}
	
	/**
	 * 查找类或接口世系树中的所有标准SET方法(含参数类对象)
	 * @param classType 查找类型
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Set<Method>> findSetMethods(Object classType){
		HashMap<String,Set<Method>> map=findPrefixMethods(classType,"set",true);
		if(null==map || 0==map.size()) return null;
		
		HashMap<String,Set<Method>> retMap=new HashMap<String,Set<Method>>();
		for(Map.Entry<String, Set<Method>> entry:map.entrySet()){
			for(Method method:entry.getValue()){
				if(!Modifier.isPublic(method.getModifiers())) continue;
				Class<?> returnType=method.getReturnType();
				if(void.class!=returnType && Void.class!=returnType) continue;
				if(1!=method.getParameterCount()) continue;
				String methodName=entry.getKey();
				Set<Method> methodSet=retMap.get(methodName);
				if(null==methodSet) retMap.put(methodName, methodSet=new HashSet<Method>());
				methodSet.add(method);
			}
		}
		return retMap;
	}
	
	/**
	 * 查找类或接口世系树中的所有标准GET方法(含参数类对象)
	 * @param classType 查找类型
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Method> findGetMethods(Object classType){
		HashMap<String,Set<Method>> map=findPrefixMethods(classType,"get",true);
		if(null==map || 0==map.size()) return null;
		
		HashMap<String,Method> retMap=new HashMap<String,Method>();
		for(Map.Entry<String, Set<Method>> entry:map.entrySet()){
			for(Method method:entry.getValue()){
				if(!Modifier.isPublic(method.getModifiers())) continue;
				Class<?> returnType=method.getReturnType();
				if(void.class==returnType||Void.class==returnType) continue;
				if(0!=method.getParameterCount()) continue;
				retMap.put(entry.getKey(),method);
			}
		}
		return retMap;
	}
	
	/**
	 * 查找类或接口世系树中的所有方法(含参数类对象)
	 * @param classType 查找类型
	 * @param prefixs 查找方法名前缀
	 * @param compatibles 是否按类型兼容排重(默认为false)
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 * 方法名前缀通常为get/set/add/create/del/remove/mod/update/is/has/enable/disable等
	 */
	public static final HashMap<String,Set<Method>> findPrefixMethods(Object classType,String prefixs,boolean... compatibles){
		if(null==classType || null==prefixs || 0==prefixs.trim().length()) return null;
		String prefix=prefixs.trim();
		HashMap<String, Set<Method>> prefixMethodMap=new HashMap<String, Set<Method>>();
		HashMap<String, Set<Method>> allMethodMap=findMethods(ClassType.ALL,classType,compatibles);
		for(Map.Entry<String, Set<Method>> entry:allMethodMap.entrySet()){
			String methodName=entry.getKey();
			if(!methodName.startsWith(prefix)) continue;
			prefixMethodMap.put(methodName, entry.getValue());
		}
		return prefixMethodMap;
	}
	
	/**
	 * 查找当前类及超类世系树中的所有方法(含参数类对象)
	 * @param classType 查找类型
	 * @param compatibles 是否按类型兼容排重(默认为false)
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Set<Method>> findMethodsByType(Object classType,boolean... compatibles){
		return findMethods(ClassType.CLASS,classType,compatibles);
	}
	
	/**
	 * 查找当前类及超接口世系树中的所有方法(含参数类对象)
	 * @param classType 查找类型
	 * @param compatibles 是否按类型兼容排重(默认为false)
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Set<Method>> findMethodsByFace(Object classType,boolean... compatibles){
		return findMethods(ClassType.FACE,classType,compatibles);
	}
	
	/**
	 * 查找类或接口世系树中的所有方法(含参数类对象)
	 * @param classType 查找类型
	 * @param compatibles 是否按类型兼容排重(默认为false)
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Set<Method>> findMethods(Object classType,boolean... compatibles){
		return findMethods(ClassType.ALL,classType,compatibles);
	}
	
	/**
	 * 查找类或接口世系树中的所有方法(含参数类对象)
	 * @param kindType 查找模式
	 * @param classType 查找类型
	 * @param compatibles 是否按类型兼容排重(默认为false)
	 * @return 方法名称到方法对象的映射字典
	 * @description 扩展类将覆盖基类同名同参数列表方法
	 */
	public static final HashMap<String,Set<Method>> findMethods(ClassType kindType,Object classType,boolean... compatibles){
		if(null==classType) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		HashMap<String,Set<Method>> subMap=new HashMap<String,Set<Method>>();
		boolean compatible=null==compatibles||0==compatibles.length?false:compatibles[0];
		
		for(Class<?> type:types){
			out:for(Method method:type.getDeclaredMethods()) {
				method.setAccessible(true);
				String methodName=method.getName();
				Set<Method> set=subMap.get(methodName);
				if(null==set) subMap.put(methodName, set=new HashSet());
				if(compatible){
					for(Method imethod:set) if(compatible(imethod.getParameterTypes(),method.getParameterTypes()))  continue out;
				}else{
					for(Method imethod:set) if(Arrays.equals(method.getParameterTypes(),imethod.getParameterTypes())) continue out;
				}
				set.add(method);
			}
			
		HashMap<String,Set<Method>> parentMap=null;
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				parentMap=findMethods(kindType,finalSuperClass);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				parentMap=findMethods(kindType,finalSuperFaces);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				parentMap=findMethods(kindType,finalAllTypes);
			}
			
			if(null==parentMap) continue;
			out:for(Map.Entry entry:parentMap.entrySet()) {
				String methodName=(String)entry.getKey();
				Method method=(Method)entry.getValue();
				Set<Method> set=subMap.get(methodName);
				if(null==set) subMap.put(methodName, set=new HashSet());
				if(compatible){
					for(Method imethod:set) if(compatible(imethod.getParameterTypes(),method.getParameterTypes()))  continue out;
				}else{
					for(Method imethod:set) if(Arrays.equals(method.getParameterTypes(),imethod.getParameterTypes())) continue out;
				}
				set.add(method);
			}
		}
		return subMap;
	}
	
	/**
	 * 查找类中的第一个构造方法
	 * 若重载多次构造则返回的构造方法是不确定的
	 * @param type 类型
	 * @return 构造方法对象
	 */
	public static final Constructor<?> findFirstConstructor(Object classType){
		if(null==classType) return null;
		Class<?> type=Class.class.isInstance(classType)?(Class<?>)classType:classType.getClass();
		Constructor<?> constructor=type.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor;
	}
	
	/**
	 * 查找类中的第一个公共构造方法
	 * 若重载多次构造则返回的构造方法是不确定的
	 * @param classType 类对象
	 * @return 构造方法对象
	 */
	public static final Constructor<?> findFirstPublicConstructor(Object classType){
		if(null==classType) return null;
		Class<?> type=Class.class.isInstance(classType)?(Class<?>)classType:classType.getClass();
		Constructor<?>[] constructors=type.getConstructors();
		if(null==constructors||0==constructors.length) return null;
		Constructor<?> constructor=constructors[0];
		constructor.setAccessible(true);
		return constructor;
	}
	
	/**
	 * 查找类中的构造方法
	 * @param type 类对象
	 * @param paramsType 参数列表
	 * @return 构造方法对象
	 */
	public static final Constructor<?> findConstructor(Object classType,Class<?>... paramTypes){
		if(null==classType) return null;
		Constructor<?> targetConstructor=null;
		Class<?> type=Class.class.isInstance(classType)?(Class<?>)classType:classType.getClass();
		
		Constructor<?>[] constructors=type.getDeclaredConstructors();
		out:for(Constructor<?> constructor:constructors){
			int argCount=constructor.getParameterCount();
			if(0==argCount) {
				if(null==paramTypes || 0==paramTypes.length){
					targetConstructor=constructor;
					break out;
				}
				continue out;
			}else{
				if(null==paramTypes || argCount!=paramTypes.length) continue out;
				Class<?>[] curMethodTypes=constructor.getParameterTypes();
				for(int i=0;i<paramTypes.length;i++){
					Class<?> paramType=paramTypes[i];
					Class<?> curMethodType=curMethodTypes[i];
					if(!compatible(curMethodType,paramType)) continue out;
				}
				targetConstructor=constructor;
				break out;
			}
		}
		
		if(null!=targetConstructor) targetConstructor.setAccessible(true);
		return targetConstructor;
	}
	
	/**
	 * 查找类或世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param args 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotation(Object classType,String methodName,Class<R> annotationType,Object... args){
		return findMethodAnnotation(ClassType.ALL,classType,methodName,annotationType,args);
	}
	
	/**
	 * 查找类世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param args 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotationByClass(Object classType,String methodName,Class<R> annotationType,Object... args){
		return findMethodAnnotation(ClassType.CLASS,classType,methodName,annotationType,args);
	}
	
	/**
	 * 查找接口世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param args 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotationByFace(Object classType,String methodName,Class<R> annotationType,Object... args){
		return findMethodAnnotation(ClassType.FACE,classType,methodName,annotationType,args);
	}
	
	/**
	 * 查找类或接口世系树中方法上的注解
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param args 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findMethodAnnotation(ClassType kindType,Object classType,String methodName,Class<R> annotationType,Object... args){
		final Class[] typeArgs=new Class[null==args?0:args.length];
		if(null!=args && 0!=args.length)for(int i=0;i<args.length;typeArgs[i]=args[i].getClass(),i++);
		return findMethodAnnotation(kindType,classType,methodName,annotationType,typeArgs);
	}
	
	/**
	 * 查找类或接口中第一个方法上的注解
	 * 如果重载了多个方法则查找的注解是不确定的
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findFirstAnnotation(Object classType,String methodName,Class<R> annotationType){
		return findFirstMethodAnnotation(ClassType.ALL,classType,methodName,annotationType);
	}
	
	/**
	 * 查找类中第一个方法上的注解
	 * 如果重载了多个方法则查找的注解是不确定的
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findFirstAnnotationByClass(Object classType,String methodName,Class<R> annotationType){
		return findFirstMethodAnnotation(ClassType.CLASS,classType,methodName,annotationType);
	}
	
	/**
	 * 查找接口中第一个方法上的注解
	 * 如果重载了多个方法则查找的注解是不确定的
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findFirstAnnotationByFace(Object classType,String methodName,Class<R> annotationType){
		return findFirstMethodAnnotation(ClassType.FACE,classType,methodName,annotationType);
	}
	
	/**
	 * 查找类或接口中第一个方法上的注解
	 * 如果重载了多个方法则查找的注解是不确定的
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findFirstMethodAnnotation(ClassType kindType,Object classType,String methodName,Class<R> annotationType){
		if(null==classType || null==methodName || null==annotationType || methodName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		R targetAnnotation=null;
		for(Class<?> type:types){
			Method targetMethod=null;
			Method[] methods=type.getDeclaredMethods();
			for(Method method:methods){
				if(!(methodName.equals(method.getName()))) continue;
				targetMethod=method;
				break;
			}
			
			if(null!=targetMethod) {
				targetMethod.setAccessible(true);
				targetAnnotation=findOneLayerAnnotation(targetMethod,annotationType);
				if(null!=targetAnnotation) return targetAnnotation;
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetAnnotation=findMethodAnnotation(kindType,finalSuperClass,methodName,annotationType);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetAnnotation=findMethodAnnotation(kindType,finalSuperFaces,methodName,annotationType);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetAnnotation=findMethodAnnotation(kindType,finalAllTypes,methodName,annotationType);
			}
			
			if(null==targetAnnotation) continue;
			return targetAnnotation;
		}
		return null;
	}
	
	/**
	 * 查找类或接口世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotation(Object classType,String methodName,Class<R> annotationType,Class<?>... argTypes){
		return findMethodAnnotation(ClassType.ALL,classType,methodName,annotationType,argTypes);
	}
	
	/**
	 * 查找类世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotationByClass(Object classType,String methodName,Class<R> annotationType,Class<?>... argTypes){
		return findMethodAnnotation(ClassType.CLASS,classType,methodName,annotationType,argTypes);
	}
	
	/**
	 * 查找接口世系树中方法上的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findAnnotationByFace(Object classType,String methodName,Class<R> annotationType,Class<?>... argTypes){
		return findMethodAnnotation(ClassType.FACE,classType,methodName,annotationType,argTypes);
	}
	
	/**
	 * 查找类或接口世系树中方法上的注解
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationType 注解类型
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final <R extends Annotation> R findMethodAnnotation(ClassType kindType,Object classType,String methodName,Class<R> annotationType,Class<?>... paramTypes){
		if(null==classType || null==methodName || null==annotationType || methodName.trim().isEmpty()) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		R targetAnnotation=null;
		for(Class<?> type:types){
			Method targetMethod=null;
			Method[] methods=type.getDeclaredMethods();
			out:for(Method method:methods){
				if(!(methodName.equals(method.getName()))) continue out;
				int argCount=method.getParameterCount();
				if(0==argCount) {
					if(null==paramTypes || 0==paramTypes.length){
						targetMethod=method;
						break out;
					}
					continue out;
				}else{
					if(null==paramTypes || argCount!=paramTypes.length) continue out;
					Class<?>[] curMethodTypes=method.getParameterTypes();
					for(int i=0;i<paramTypes.length;i++){
						Class<?> paramType=paramTypes[i];
						Class<?> curMethodType=curMethodTypes[i];
						if(!compatible(curMethodType,paramType)) continue out;
					}
					targetMethod=method;
					break out;
				}
			}
			
			if(null!=targetMethod) {
				targetMethod.setAccessible(true);
				targetAnnotation=findOneLayerAnnotation(targetMethod,annotationType);
				if(null!=targetAnnotation) return targetAnnotation;
			}
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetAnnotation=findMethodAnnotation(kindType,finalSuperClass,methodName,annotationType,paramTypes);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetAnnotation=findMethodAnnotation(kindType,finalSuperFaces,methodName,annotationType,paramTypes);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetAnnotation=findMethodAnnotation(kindType,finalAllTypes,methodName,annotationType,paramTypes);
			}
			
			if(null==targetAnnotation) continue;
			return targetAnnotation;
		}
		return null;
	}
	
	/**
	 * 类或接口世系树中方法上是否包含指定的注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationTypes 是否被包含的注解
	 * @param argTypes 参数列表
	 * @return 是否包含指定的注解
	 */
	public static final <R extends Annotation> Boolean containsAnnotation(Object classType,String methodName,Class<R> annotationType,Class<?>... argTypes){
		Set<Annotation> annotationSet=getMethodAnnotations(classType,methodName,argTypes);
		Set<Class<? extends Annotation>> annotationTypeSet=annotationSet.stream().map(annotation->annotation.annotationType()).collect(Collectors.toSet());
		return annotationTypeSet.contains(annotationType);
	}
	
	/**
	 * 类或接口世系树中方法上是否包含参数注解集中的所有注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationTypes 是否被包含的参数注解集
	 * @param argTypes 参数列表
	 * @return 是否包含参数注解集中的所有注解
	 */
	public static final <R extends Annotation> Boolean containsAllAnnotation(Object classType,String methodName,Class<R>[] annotationTypes,Class<?>... argTypes){
		Set<Annotation> annotationSet=getMethodAnnotations(classType,methodName,argTypes);
		Set<Class<? extends Annotation>> annotationTypeSet=annotationSet.stream().map(annotation->annotation.annotationType()).collect(Collectors.toSet());
		return annotationTypeSet.containsAll(new HashSet(Arrays.asList(annotationTypes)));
	}
	
	/**
	 * 类或接口世系树中方法上是否至少包含参数注解集中的其中一个注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param annotationTypes 是否被包含的参数注解集
	 * @param argTypes 参数列表
	 * @return 是否至少包含参数注解集中的其中一个注解
	 */
	public static final <R extends Annotation> Boolean containsAnyAnnotation(Object classType,String methodName,Class<R>[] annotationTypes,Class<?>... argTypes){
		Set<Annotation> annotationSet=getMethodAnnotations(classType,methodName,argTypes);
		Set<Class<? extends Annotation>> annotationTypeSet=annotationSet.stream().map(annotation->annotation.annotationType()).collect(Collectors.toSet());
		for(Class<R> condiType:annotationTypes) if(annotationTypeSet.contains(condiType)) return true;
		return false;
	}
	
	/**
	 * 查找类或接口世系树中方法上的所有注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final HashSet<Annotation> getMethodAnnotations(Object classType,String methodName,Class<?>... argTypes){
		return getMethodAnnotations(ClassType.ALL,classType,methodName,argTypes);
	}
	
	/**
	 * 查找类世系树中方法上的所有注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final HashSet<Annotation> getMethodAnnotationsByClass(Object classType,String methodName,Class<?>... argTypes){
		return getMethodAnnotations(ClassType.CLASS,classType,methodName,argTypes);
	}
	
	/**
	 * 查找接口世系树中方法上的所有注解
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final HashSet<Annotation> getMethodAnnotationsByFace(Object classType,String methodName,Class<?>... argTypes){
		return getMethodAnnotations(ClassType.FACE,classType,methodName,argTypes);
	}
	
	/**
	 * 查找类或接口世系树中方法上的所有注解
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param methodName 方法名
	 * @param argTypes 参数列表
	 * @return 注解
	 */
	public static final HashSet<Annotation> getMethodAnnotations(ClassType kindType,Object classType,String methodName,Class<?>... argTypes){
		HashSet<Annotation> annotationSet=new HashSet<Annotation>();
		Set<Method> methods=getDeclaredMethods(kindType,classType,methodName,argTypes);
		for(Method method:methods){
			Annotation[] annotations=findAnnotations(method);
			if(null!=annotations&&0!=annotations.length)annotationSet.addAll(Arrays.asList(annotations));
		}
		return annotationSet;
	}
	
	/**
	 * 查找类或接口世系树上的注解(含参数类对象)
	 * @param classType 类对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R> R findAnnotation(Object classType,Class<R> annotationType){
		return findTypeAnnotation(ClassType.ALL,classType,annotationType);
	}
	
	/**
	 * 查找类世系树上的注解(含参数类对象)
	 * @param classType 类对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R> R findAnnotationByClass(Object classType,Class<R> annotationType){
		return findTypeAnnotation(ClassType.CLASS,classType,annotationType);
	}
	
	/**
	 * 查找接口世系树上的注解(含参数类对象)
	 * @param classType 类对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R> R findAnnotationByFace(Object classType,Class<R> annotationType){
		return findTypeAnnotation(ClassType.FACE,classType,annotationType);
	}
	
	/**
	 * 查找类或接口世系树上的注解(含参数类对象)
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R> R findTypeAnnotation(ClassType kindType,Object classType,Class<R> annotationType){
		if(null==classType || null==annotationType) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			R targetAnnotation=null;
			Annotation[] annotations=findAnnotations(type);
			if(null!=annotations&&0!=annotations.length) {
				for(Annotation annotation:annotations){
					if(!annotationType.isInstance(annotation)) continue;
					targetAnnotation=(R)annotation;
					break;
				}
			}
			
			if(null!=targetAnnotation) return targetAnnotation;
			
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				targetAnnotation=findTypeAnnotation(kindType,finalSuperClass,annotationType);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				targetAnnotation=findTypeAnnotation(kindType,finalSuperFaces,annotationType);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				targetAnnotation=findTypeAnnotation(kindType,finalAllTypes,annotationType);
			}
			
			if(null==targetAnnotation) continue;
			return targetAnnotation;
		}
		return null;
	}
	
	/**
	 * 获取类或接口世系树上所有注解(含参数类对象)
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param annotationSet 注解集合(通常为null)
	 * @return 注解集合
	 */
	public static final Set<Annotation> getTypeAnnotations(ClassType kindType,Object classType){
		return getTypeAnnotations(kindType,classType,new HashSet<Annotation>());
	}
	
	/**
	 * 获取类或接口世系树上所有注解(含参数类对象)
	 * @param kindType 递归类型(类、接口、所有)
	 * @param classType 类对象
	 * @param annotationSet 注解集合
	 * @return 注解集合
	 */
	private static final Set<Annotation> getTypeAnnotations(ClassType kindType,Object classType,Set<Annotation> annotationSet){
		if(null==classType) return null;
		
		Class<?>[] types=null;
		if(!Class.class.isInstance(classType) && !Class[].class.isInstance(classType)) {
			types=new Class<?>[]{classType.getClass()};
		}else{
			types=Class[].class.isInstance(classType)?(Class<?>[])classType:new Class<?>[]{(Class<?>)classType};
		}
		if(null==types||0==types.length) return null;
		
		for(Class<?> type:types){
			Annotation[] annotations=findAnnotations(type);
			if(null!=annotations&&0!=annotations.length) annotationSet.addAll(Arrays.asList(annotations));
			if(ClassType.CLASS==kindType){
				Class<?> superClass=type.getSuperclass();
				Class<?>[] finalSuperClass=null==superClass?null:new Class<?>[]{superClass};
				getTypeAnnotations(kindType,finalSuperClass,annotationSet);
			}else if(ClassType.FACE==kindType){
				Class<?>[] superFaces=type.getInterfaces();
				Class<?>[] finalSuperFaces=null==superFaces||0==superFaces.length?null:superFaces;
				getTypeAnnotations(kindType,finalSuperFaces,annotationSet);
			}else{
				Class<?>[] finalAllTypes=null;
				Class<?> superClass=type.getSuperclass();
				Class<?>[] superFaces=type.getInterfaces();
				if(null==superFaces||0==superFaces.length){
					if(null==superClass){
						finalAllTypes=null;
					}else{
						finalAllTypes=new Class<?>[]{superClass};
					}
				}else{
					if(null==superClass){
						finalAllTypes=superFaces;
					}else{
						finalAllTypes=new Class<?>[superFaces.length+1];
						finalAllTypes[0]=superClass;
						System.arraycopy(superFaces, 0, finalAllTypes, 1, superFaces.length);
					}
				}
				getTypeAnnotations(kindType,finalAllTypes,annotationSet);
			}
			continue;
		}
		return annotationSet;
	}
	
	/**
	 * 查找指定类或接口上的注解
	 * @param type 类对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findOneLayerAnnotation(Object classType,Class<R> annotationType){
		if(null==classType||null==annotationType) return null;
		Class<?> type=Class.class.isInstance(classType)?(Class<?>)classType:classType.getClass();
		Annotation[] annotations=findAnnotations(type);
		if(null==annotations) return null;
		for(Annotation annotation:annotations)
		if(annotationType.isInstance(annotation)) return (R)annotation;
		return null;
	}
	
	/**
	 * 查找指定方法上的注解
	 * @param method 方法对象
	 * @param annotationType 注解类型
	 * @return 注解
	 */
	public static final <R extends Annotation> R findOneLayerAnnotation(Method method,Class<R> annotationType){
		if(null==method||null==annotationType) return null;
		Annotation[] annotations=findAnnotations(method);
		if(null==annotations) return null;
		for(Annotation annotation:annotations)
		if(annotationType.isInstance(annotation)) return (R)annotation;
		return null;
	}
	
	/**
	 * 查找指定类或接口上的所有注解
	 * @param type 类对象
	 * @return 注解数组
	 */
	public static final Annotation[] findAnnotations(Class<?> type){
		if(null==type) return null;
		return type.getAnnotations();
	}
	
	/**
	 * 查找指定方法上的所有注解
	 * @param method 方法对象
	 * @return 注解数组
	 */
	public static final Annotation[] findAnnotations(Method method){
		if(null==method) return null;
		return method.getAnnotations();
	}
}