package com.github.lixiang2114.flow.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lixiang
 * IP地址工具
 */
@SuppressWarnings({"unused","rawtypes","unchecked"})
public class IPUtil {
	/**
	 * IP文件解析参数
	 */
	private static int cout;
	
	/**
	 * IP文件解析参数
	 */
	private static int[] index;
	
	/**
	 * IP文件解析参数
	 */
	private static long textLen[];
	
	/**
	 * IP文件解析参数
	 */
	private static long ipEndArr[];
	
	/**
	 * IP文件解析参数
	 */
	private static byte[] textData;
	
	/**
	 * IP文件解析参数
	 */
	private static long textOffset[];
	
	/**
	 * IP文件解析参数
	 */
	private static long max=(1L << 32) - 1;
	
	/**
	 * IPV6地址的长度
	 */
	private static final int IPV6Length = 8; 
	
    /**
     * IPV6地址长度
     */
    private static final int IPV4Length = 4;
    
    /**
     * IPV4分段长度  
     */
    private static final int IPV4ParmLength = 2;
    
    /**
     * IPV4分段长度  
     */
    private static final int IPV6ParmLength = 4;
    
    /**
     * 客户端端IP地址
     */
	private static final String IP="ip";
    
    /**
     * 错误信息(为空表示无误)
     */
    private static final String ERROR="err";
    
    /**
     * IP归宿城市(省级市或地级市)
     */
    private static final String CITY="city";
    
    /**
     * 省份
     */
    private static final String PROVINCE="pro";
    
    /**
     * 详细地址描述(只能精确到地级市)
     */
    private static final String ADDRESS="addr";
    
    /**
     * 城市编码(省级市或地级市)
     */
    private static final String CITY_CODE="cityCode";
    
    /**
     * 省份编码
     */
    private static final String PROVINCE_CODE="proCode";
	
	/**
	 * 内网地址(不含本机回环地址)
	 */
	private static HashSet<String> lanIpAddress;
	
	/**
	 * 网络接口到Mac和IP地址的映射字典
	 */
	private static HashMap<String,Map<String,ArrayList[]>> ifaceToMacIpDict;
	
	static{
		try {
			ifaceToMacIpDict=getIfaceToMacIpDict();
		} catch (Exception e) {
			e.printStackTrace();
		}
		lanIpAddress=new HashSet<String>(Arrays.asList("10","172","192"));
	}
	
	/**
	 * 初始化IP地址库参数
	 */
	public static void initLocalIpData() {
		int fileSize=0;
        byte[] data = null;
        InputStream fis=null;
        String ipFilePath = IPUtil.class.getClassLoader().getResource("conf/ip.dat").getPath();
        try {
            File file = new File(ipFilePath);
            fis = new FileInputStream(file);
            fileSize=(int) file.length();
            data = new byte[fileSize];
            fis.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	try{
        		if(null!=fis)fis.close();
        	}catch(IOException e){
        		e.printStackTrace();
        	}
        }
        
        byte[] buf = Arrays.copyOfRange(data, 0, 4);
        long offset = (((buf)[0] & 0xFF) | (((buf)[1] << 8) & 0xFF00) | (((buf)[2] << 16) & 0xFF0000) | (((buf)[3] << 24) & 0xFF000000));
        cout = (int)((offset - 4 - 256 * 4) / 9);
        
        index = new int[256];
        for (int i = 0; i < 256; i++) {
            int offsetIndex = 4 + i * 4;
            byte[] temp = Arrays.copyOfRange(data, offsetIndex, (offsetIndex + 4));
            index[i] =(int) (((temp)[0] & 0xFF) | (((temp)[1] << 8) & 0xFF00) | (((temp)[2] << 16) & 0xFF0000) | (((temp)[3] << 24) & 0xFF000000));
        }
        
        textData = Arrays.copyOfRange(data, (int)(offset), (((int)(offset)) + ((int)(fileSize - offset))));
        
        textLen = new long[cout];
        ipEndArr = new long[cout];
        textOffset = new long[cout];
        for (int i = 0; i < cout; i++) {
            int offsetIndex = 4 + 1024 + i * 9;
            byte[] temp = Arrays.copyOfRange(data, offsetIndex, (offsetIndex + 4));
            ipEndArr[i] = (((temp)[0] & 0xFF) | (((temp)[1] << 8) & 0xFF00) | (((temp)[2] << 16) & 0xFF0000) | (((temp)[3] << 24) & 0xFF000000)) & max;
            temp = Arrays.copyOfRange(data, offsetIndex + 4, (offsetIndex + 8));
            textOffset[i] =(((temp)[0] & 0xFF) | (((temp)[1] << 8) & 0xFF00) | (((temp)[2] << 16) & 0xFF0000) | (((temp)[3] << 24) & 0xFF000000)) & max;
            textLen[i] = data[offsetIndex + 8] & max;
        }
    }

	/**
	 * 获取客户端(非反向代理)的真实IP地址
	 * @param request HTTP请求
	 * @return IP地址
	 */
	public static String getIp(HttpServletRequest request) {
		String ipAddr=null;
        String ipString = request.getHeader("X-Forwarded-For");
        if (null==ipString || ipString.trim().isEmpty() || "unKnown".equalsIgnoreCase(ipString)) {
        	ipString = request.getHeader("X-Real-IP");
            if (null!=ipString && !ipString.trim().isEmpty() && !"unKnown".equalsIgnoreCase(ipString)) ipAddr=ipString;
            ipAddr=request.getRemoteAddr();
        }else{
        	 int index = ipString.indexOf(",");
             if (-1==index) {
            	 ipAddr=ipString;
             }else{
            	 ipAddr=ipString.substring(0, index);
             }
        }
        
        if(null==ipAddr || ipAddr.trim().isEmpty()) return "127.0.0.1";
        return ipAddr;
    }
	
    /**
     * 将IP地址转换为长整型
     * @param strIp IP地址
     * @return 长整型
     */
    private static long ipToLong(String strIp) {
        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return ((ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]) & max;
    }
    
    /**
     * 查找参数IP地址在文件中的索引
     * @param ip IP地址
     * @param start 查找起始位置
     * @param end 查找结束位置
     * @return IP索引
     */
    private static int findIndexOffset(long ip, int start, int end) {
        while (start < end) {
            int mid = (start + end) / 2;
            if (ip > ipEndArr[mid]) {
                start = mid + 1;
            } else {
                end = mid;
            }
        }
        if (ipEndArr[end] >= ip) {
            return end;
        }
        return start;
    }

    /**
     * 获取IP地址串
     * @param request 请求
     * @return 地址串
     */
    public static String getAddress(HttpServletRequest request) {
        String address = "未知";
        try {
            // 获取登录IP地址
            String strIP = getIp(request);
            String reg6 = "(?i)^((([\\da-f]{1,4}:){7}[\\da-f]{1,4})|(([\\da-f]{1,4}:){1,7}:)|(([\\da-f]{1,4}:){6}:[\\da-f]{1,4})|(([\\da-f]{1,4}:){5}(:[\\da-f]{1,4}){1,2})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){1,3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){1,4})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){1,5})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){1,6})|(:(:[\\da-f]{1,4}){1,7})|(([\\da-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[\\da-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$";
            if (strIP.matches(reg6)) {
                address = "ip6:" + strIP;
            } else {
                String tempReg = "(?i)^((([\\da-f]{1,4}:){7}[\\da-f]{1,4})|(([\\da-f]{1,4}:){1,7}:)|(([\\da-f]{1,4}:){6}:[\\da-f]{1,4})|(([\\da-f]{1,4}:){5}(:[\\da-f]{1,4}){1,2})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){1,3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){1,4})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){1,5})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){1,6})|(:(:[\\da-f]{1,4}){1,7})|(([\\da-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[\\da-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$";
                if(strIP.matches(tempReg)) {
                    try {
                    	address = new String(toByte(strIP), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else {
                    long ip = ipToLong(strIP);
                    int end = 0;
                    if (ip >> 24 != 0xff) {
                        end = index[(int) ((ip >> 24) + 1)];
                    }
                    if (end == 0) {
                        end = cout;
                    }
                    
                    int k=index[(int)( ip >> 24)];
                    int index = findIndexOffset(ip, k, end);
                    byte[] res = Arrays.copyOfRange(textData, (int)textOffset[index], (((int)textOffset[index]) + ((int)textLen[index])));
                    try {
                    	address = new String(res, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        }
        return address;
    }
    
    /**
     * 获取IP地址的归属地
     * @param request 请求
     * @return 地区(仅精确到地级市)
     */
    public static Map<String,Object> getIpDestination(HttpServletRequest request) {
    	String queryIp=getIp(request);
    	return getIpDestination(queryIp);
    }

    /**
     * 获取IP地址的归属地
     * @param queryIp 查询IP地址
     * @return 地区(仅精确到地级市)
     */
	public static Map<String,Object> getIpDestination(String queryIp) {
    	String response = null;
        BufferedReader reader=null;
        DataOutputStream writer=null;
        HttpURLConnection connection = null;
        try{
            //建立连接
            connection = (HttpURLConnection) new URL("http://whois.pconline.com.cn/ipJson.jsp").openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.connect();
            
            //发送请求参数
            writer = new DataOutputStream(connection.getOutputStream());
            writer.writeBytes("ip="+queryIp+"&json=true");
            writer.flush();
            
            //读取响应结果
            String line = "";
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
            while ((line = reader.readLine()) != null) builder.append(line);
            response = builder.toString();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
        	try{
        		if (reader != null) reader.close();
            	if (writer != null) writer.close();
                if (connection != null) connection.disconnect();
        	}catch(IOException e){
        		e.printStackTrace();
        	}
        }
        
        if(null==response || response.trim().isEmpty()) return null;
        return CommonUtil.jsonStrToJava(response, Map.class);
    }

    /**
     * 获取Linux下的IP地址
     * @return IP地址
     * @throws SocketException
     */
    public static String getLinuxServerIp() throws SocketException {
    	String ip=getWanUncertainIPV4();
    	if(null==ip || ip.isEmpty()) ip=getLanUncertainIPV4();
    	if(null==ip || ip.isEmpty()) ip="127.0.0.1";
        return ip;
    }
    
    /**
     * Unicode转换成 中文
     * @param theString Unicode字串
     * @return 中文串
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                        default:
                            throw new IllegalArgumentException("Malformed      encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }
    
    /**
	 * 端口扫描
	 * 给定超时时间为0则表示连接将被阻塞以直到建立连接或发生错误为止
	 * @param host 需要扫描的主机名(可以为IP地址或域名)
	 * @param port 需要扫描的主机端口(范围为:1-65536)
	 * @param timeOut 超时时间(单位:毫秒)
	 * @return 端口是否正常
	 */
	public static Boolean scanPort(String host,Integer port, Integer... timeOut) {
		Socket socket = null;
		try {
			socket = new Socket();
			if(null==timeOut || 0==timeOut.length){
				socket.connect(new InetSocketAddress(host, port));
			}else{
				socket.connect(new InetSocketAddress(host, port),timeOut[0]);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (null != socket) socket.close();
			} catch (IOException e) {}
		}
	}
	
	/**
	 * 获取Lan内网任意IPV4地址
	 * @param ifaceName 网络接口名称
	 * @return IP地址
	 */
	public static String getLanUncertainIPV4(){
		if(0==ifaceToMacIpDict.size())return null;
		Collection<Map<String, ArrayList[]>> cols=ifaceToMacIpDict.values();
		List<String> ipv4List=cols.iterator().next().entrySet().iterator().next().getValue()[0];
		for(String ip:ipv4List){
			String ipPrefix=ip.substring(0, ip.indexOf("."));
			if(lanIpAddress.contains(ipPrefix))return ip;
		}
		return null;
	}
	
	/**
	 * 获取Lan内网第一个IPV4地址
	 * @param ifaceName 网络接口名称
	 * @return IP地址
	 */
	public static String getLanFirstIPV4(String ifaceName){
		if(0==ifaceToMacIpDict.size())return null;
		Map<String,ArrayList[]> macToIpDict=ifaceToMacIpDict.get(ifaceName);
		if(null==macToIpDict)return null;
		Entry<String, ArrayList[]> entry=macToIpDict.entrySet().iterator().next();
		List<String> ipV4List=entry.getValue()[0];
		for(String ip:ipV4List){
			String ipPrefix=ip.substring(0, ip.indexOf("."));
			if(lanIpAddress.contains(ipPrefix))return ip;
		}
		return null;
	}
	
	/**
	 * 获取Wan外网任意IPV4地址
	 * @param ifaceName 网络接口名称
	 * @return IP地址
	 */
	public static String getWanUncertainIPV4(){
		if(0==ifaceToMacIpDict.size())return null;
		Collection<Map<String, ArrayList[]>> cols=ifaceToMacIpDict.values();
		List<String> ipv4List=cols.iterator().next().entrySet().iterator().next().getValue()[0];
		for(String ip:ipv4List){
			String ipPrefix=ip.substring(0, ip.indexOf("."));
			if(!lanIpAddress.contains(ipPrefix))return ip;
		}
		return null;
	}
	
	/**
	 * 获取Wan外网第一个IPV4地址
	 * @param ifaceName 网络接口名称
	 * @return IP地址
	 */
	public static String getWanFirstIPV4(String ifaceName){
		if(0==ifaceToMacIpDict.size())return null;
		Map<String,ArrayList[]> macToIpDict=ifaceToMacIpDict.get(ifaceName);
		if(null==macToIpDict)return null;
		Entry<String, ArrayList[]> entry=macToIpDict.entrySet().iterator().next();
		List<String> ipV4List=entry.getValue()[0];
		for(String ip:ipV4List){
			String ipPrefix=ip.substring(0, ip.indexOf("."));
			if(!lanIpAddress.contains(ipPrefix))return ip;
		}
		return null;
	}
	
	/**
	 * 获取Lan内网所有IPV4地址
	 * @return IP地址集
	 */
	public static HashSet<String> getLanAllIPV4(){
		if(0==ifaceToMacIpDict.size())return null;
		Collection<Map<String, ArrayList[]>> cols=ifaceToMacIpDict.values();
		HashSet<String> ipv4Set=new HashSet<String>();
		cols.forEach(dict->{
			List<String> ipv4List=dict.entrySet().iterator().next().getValue()[0];
			ipv4List.forEach(ipv4->{
				if(lanIpAddress.contains(ipv4))ipv4Set.add(ipv4);
			});
		});
		return ipv4Set;
	}
	
	/**
	 * 获取Wan外网所有IPV4地址
	 * @param ifaceName 网络接口名称
	 * @return IP地址集
	 */
	public static HashSet<String> getWanAllIPV4(String ifaceName){
		if(0==ifaceToMacIpDict.size())return null;
		Map<String,ArrayList[]> macToIpDict=ifaceToMacIpDict.get(ifaceName);
		if(null==macToIpDict)return null;
		Entry<String, ArrayList[]> entry=macToIpDict.entrySet().iterator().next();
		List<String> ipV4List=entry.getValue()[0];
		HashSet<String> ipv4Set=new HashSet<String>();
		for(String ip:ipV4List){
			String ipPrefix=ip.substring(0, ip.indexOf("."));
			if(!lanIpAddress.contains(ipPrefix))ipv4Set.add(ip);
		}
		return ipv4Set;
	}
	
	/**
	 * 获取当前主机上任意一个MAC地址
	 * @return MAC地址
	 */
	public static String getUncertainMac(){
		if(0==ifaceToMacIpDict.size())return null;
		Collection<Map<String, ArrayList[]>> cols=ifaceToMacIpDict.values();
		return cols.iterator().next().entrySet().iterator().next().getKey();
	}
	
	/**
	 * 获取当前主机指定网卡接口上的Mac地址
	 * @param ifaceName 网络接口名称
	 * @return MAC地址
	 */
	public static String getMac(String ifaceName){
		if(0==ifaceToMacIpDict.size())return null;
		Map<String,ArrayList[]> macToIpDict=ifaceToMacIpDict.get(ifaceName);
		if(null==macToIpDict)return null;
		Entry<String, ArrayList[]> entry=macToIpDict.entrySet().iterator().next();
		return entry.getKey();
	}
	
	/**
	 * 获取当前主机所有网卡接口上的Mac地址
	 * @return MAC地址集
	 */
	public static HashSet<String> getAllMac(){
		if(0==ifaceToMacIpDict.size())return null;
		Collection<Map<String, ArrayList[]>> cols=ifaceToMacIpDict.values();
		HashSet<String> macSet=new HashSet<String>();
		cols.forEach(dict->macSet.add(dict.entrySet().iterator().next().getKey()));
		return macSet;
	}
	
	/**
     * 获取所有网卡接口的Mac及IP地址
     * @return {mac01:[ipv4,ipv6],mac02:[ipv4,ipv6]}
     * @throws Exception
     */
	private static HashMap<String,Map<String,ArrayList[]>> getIfaceToMacIpDict() throws Exception {
        java.util.Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
        StringBuilder builder = new StringBuilder();
        HashMap<String,Map<String,ArrayList[]>> ifaceToMacIPDict=new HashMap<String,Map<String,ArrayList[]>>();
        while(interfaceList.hasMoreElements()){
            NetworkInterface iface = interfaceList.nextElement();
            List<InterfaceAddress> addressList = iface.getInterfaceAddresses();
            for(InterfaceAddress address : addressList) {
                InetAddress inetAddress = address.getAddress();
                NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress);
                if(null==network)continue;
                byte[] mac = network.getHardwareAddress();
                if(null==mac)continue;
                builder.delete(0,builder.length());
                for (int i = 0; i < mac.length; i++)builder.append(String.format("%02X%s",mac[i],(i < mac.length - 1)?"-":""));
                
                String ifaceName=network.getName();
                String macAddress=builder.toString();
                String ipAddr=inetAddress.getHostAddress();
                
                Map<String,ArrayList[]> macToIpDict=ifaceToMacIPDict.get(ifaceName);
                if(null==macToIpDict){
                	macToIpDict=Collections.singletonMap(macAddress, new ArrayList[]{new ArrayList<String>(),new ArrayList<String>()});
                	ifaceToMacIPDict.put(ifaceName, macToIpDict);
                }
                
                ArrayList<String>[] ipList=macToIpDict.get(macAddress);
                if(inetAddress instanceof Inet4Address){
                	ipList[0].add(ipAddr);
                }else{
                	ipList[1].add(ipAddr);
                }
            }
        }
        return ifaceToMacIPDict;
    }
	
	/** 
     * 将ip地址都转成16个字节的数组。先将v6地址存以":"分隔存放到数组中，再将数组中的每两位取存到长度为16的字符串数组中， 
     * 再将这两位十六进制数转成十进制，再转成byte类型存放到16个字的数组中。 
     */  
    public static byte[] toByte(String ip) {  
        // 将ip地址转换成16进制  
        String Key = buildKey(ip);  
        // 将16进制转换成ip地址  
        String ip6 = splitKey(Key);  
  
        // 将v6f地址存以":"分隔存放到数组中  
        String[] ip6Str = ip6.split(":");  
        String[] ipStr = new String[16];  
        byte[] ip6Byte = new byte[16];  
  
        // 将数组中的每两位取存到长度为16的字符串数组中  
        for (int j = 0, i = 0; i < ip6Str.length; j = j + 2, i++) {  
            ipStr[j] = ip6Str[i].substring(0, 2);  
            ipStr[j + 1] = ip6Str[i].substring(2, 4);  
        }  
  
        // 将ipStr中的十六进制数转成十进制，再转成byte类型存放到16个字的数组中  
        for (int i = 0; i < ip6Byte.length; i++) {  
            ip6Byte[i] = (byte) Integer.parseInt(ipStr[i], 16);  
        }  
        return ip6Byte;  
    }
    
    /** 
     * 十六进制串转化为IP地址 
     */  
    private static String splitKey(String key) {  
        String IPV6Address = "";  
        String IPAddress = "";  
        String strKey = "";  
        String ip1 = key.substring(0, 24);  
        String tIP1 = ip1.replace("0000", "").trim();  
        if (!"".equals(tIP1) && !"FFFF".equals(tIP1)) {  
            // 将ip按：分隔  
            while (!"".equals(key)) {  
                strKey = key.substring(0, 4);  
                key = key.substring(4);  
                if ("".equals(IPV6Address)) {  
                    IPV6Address = strKey;  
                } else {  
                    IPV6Address += ":" + strKey;  
                }  
            }  
            IPAddress = IPV6Address;  
        }  
        return IPAddress;  
    }
    
    /** 
     * IPV6、IPV4转化为十六进制串 
     * @param ipAddress IP地址串
     * @return 十六进制串
     */  
    private static String buildKey(String ipAddress) {  
        String Key = "";  
        // ipv4标识 。判断是否是ipv4地址  
        int dotFlag = ipAddress.indexOf(".");  
        // ipv6标识 。判断是否是ipv6地址  
        int colonFlag = ipAddress.indexOf(":");  
        // ipv6标识 。判断是否是简写的ipv6地址  
        int dColonFlag = ipAddress.indexOf("::");  
        // 将v6或v4的分隔符用&代替  
        ipAddress = ipAddress.replace(".", "&");  
        ipAddress = ipAddress.replace(":", "&");  
        // ipv4 address。将ipv4地址转换成16进制的形式  
        if (dotFlag != -1 && colonFlag == -1) {  
            String[] arr = ipAddress.split("&");  
            // 1、 ipv4转ipv6，前4组数补0或f  
            for (int i = 0; i < IPV6Length - IPV4ParmLength; i++) {  
                // 根据v4转v6的形式，除第4组数补ffff外，前3组数补0000  
                if (i == IPV6Length - IPV4ParmLength - 1) {  
                    Key += "ffff";  
                } else {  
                    Key += "0000";  
                }  
            }  
            // 2、将ipv4地址转成16进制  
            for (int j = 0; j < IPV4Length; j++) {  
                // 1)将每组ipv4地址转换成16进制  
                arr[j] = Integer.toHexString(Integer.parseInt(arr[j]));  
                // 2) 位数不足补0，ipv4地址中一组可转换成一个十六进制，两组数即可标识ipv6中的一组，v6中的一组数不足4位补0  
                for (int k = 0; k < (IPV4ParmLength - arr[j].length()); k++) {  
                    Key += "0";  
                }  
                Key += arr[j];  
            }  
        }  
        // Mixed address with ipv4 and ipv6。将v4与v6的混合地址转换成16进制的形式  
        if (dotFlag != -1 && colonFlag != -1 && dColonFlag == -1) {  
            String[] arr = ipAddress.split("&");  
  
            for (int i = 0; i < IPV6Length - IPV4ParmLength; i++) {  
                // 将ip地址中每组不足4位的补0  
                for (int k = 0; k < (IPV6ParmLength - arr[i].length()); k++) {  
                    Key += "0";  
                }  
                Key += arr[i];  
            }  
  
            for (int j = 0; j < IPV4Length; j++) {  
                arr[j] = Integer.toHexString(Integer.parseInt(arr[j]));  
                for (int k = 0; k < (IPV4ParmLength - arr[j].length()); k++) {  
                    Key += "0";  
                }  
                Key += arr[j];  
            }  
        }  
        // Mixed address with ipv4 and ipv6,and there are more than one  
        // '0'。将v4与v6的混合地址(如::32:dc:192.168.62.174)转换成16进制的形式  
        // address param  
        if (dColonFlag != -1 && dotFlag != -1) {  
            String[] arr = ipAddress.split("&");  
            // 存放16进制的形式  
            String[] arrParams = new String[IPV6Length + IPV4ParmLength];  
            int indexFlag = 0;  
            int pFlag = 0;  
            // 1、将简写的ip地址补0  
            // 如果ip地址中前面部分采用简写，做如下处理  
            if ("".equals(arr[0])) {  
                // 1)如果ip地址采用简写形式，不足位置补0，存放到arrParams中  
                for (int j = 0; j < (IPV6Length + IPV4ParmLength - (arr.length - 2)); j++) {  
                    arrParams[j] = "0000";  
                    indexFlag++;  
                }  
                // 2)将已有值的部分(如32:dc:192.168.62.174)存放到arrParams中  
                for (int i = 2; i < arr.length; i++) {  
                    arrParams[indexFlag] = arr[i];  
                    indexFlag++;  
                }  
            } else {  
                for (int i = 0; i < arr.length; i++) {  
                    if ("".equals(arr[i])) {  
                        for (int j = 0; j < (IPV6Length + IPV4ParmLength  
                                - arr.length + 1); j++) {  
                            arrParams[indexFlag] = "0000";  
                            indexFlag++;  
                        }  
                    } else {  
                        arrParams[indexFlag] = arr[i];  
                        indexFlag++;  
                    }  
                }  
            }  
            // 2、ip(去除ipv4的部分)中采用4位十六进制数表示一组数，将不足4位的十六进制数补0  
            for (int i = 0; i < IPV6Length - IPV4ParmLength; i++) {  
                // 如果arrParams[i]组数据不足4位，前补0  
                for (int k = 0; k < (IPV6ParmLength - arrParams[i].length()); k++) {  
                    Key += "0";  
                }  
                Key += arrParams[i];  
                // pFlag用于标识位置，主要用来标识ipv4地址的起始位  
                pFlag++;  
            }  
            // 3、将ipv4地址转成16进制  
            for (int j = 0; j < IPV4Length; j++) {  
                // 1)将每组ipv4地址转换成16进制  
                arrParams[pFlag] = Integer.toHexString(Integer  
                        .parseInt(arrParams[pFlag]));  
                // 2)位数不足补0，ipv4地址中一组可转换成一个十六进制，两组数即可标识ipv6中的一组，v6中的一组数不足4位补0  
                for (int k = 0; k < (IPV4ParmLength - arrParams[pFlag].length()); k++) {  
                    Key += "0";  
                }  
                Key += arrParams[pFlag];  
                pFlag++;  
            }  
        }  
        // ipv6 address。将ipv6地址转换成16进制  
        if (dColonFlag == -1 && dotFlag == -1 && colonFlag != -1) {  
            String[] arrParams = ipAddress.split("&");  
            // 将v6地址转成十六进制  
            for (int i = 0; i < IPV6Length; i++) {  
                // 将ipv6地址中每组不足4位的补0  
                for (int k = 0; k < (IPV6ParmLength - arrParams[i].length()); k++) {  
                    Key += "0";  
                }  
  
                Key += arrParams[i];  
            }  
        }  
  
        if (dColonFlag != -1 && dotFlag == -1) {  
            String[] arr = ipAddress.split("&");  
            String[] arrParams = new String[IPV6Length];  
            int indexFlag = 0;  
            if ("".equals(arr[0])) {  
                for (int j = 0; j < (IPV6Length - (arr.length - 2)); j++) {  
                    arrParams[j] = "0000";  
                    indexFlag++;  
                }  
                for (int i = 2; i < arr.length; i++) {  
                    arrParams[indexFlag] = arr[i];  
                    i++;  
                    indexFlag++;  
                }  
            } else {  
                for (int i = 0; i < arr.length; i++) {  
                    if ("".equals(arr[i])) {  
                        for (int j = 0; j < (IPV6Length - arr.length + 1); j++) {  
                            arrParams[indexFlag] = "0000";  
                            indexFlag++;  
                        }  
                    } else {  
                        arrParams[indexFlag] = arr[i];  
                        indexFlag++;  
                    }  
                }  
            }  
            for (int i = 0; i < IPV6Length; i++) {  
                for (int k = 0; k < (IPV6ParmLength - arrParams[i].length()); k++) {  
                    Key += "0";  
                }  
                Key += arrParams[i];  
            }  
        }  
        return Key;  
    }
}
