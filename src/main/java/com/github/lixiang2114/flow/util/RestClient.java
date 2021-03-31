package com.github.lixiang2114.flow.util;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.github.lixiang2114.flow.thread.ThreadHolder;

/**
 * @author Lixiang
 * Rest客户端工具
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class RestClient {
	/**
	 * RestTemplate工具模板(此工具模板是无状态的)
	 */
	private static RestTemplate restTemplate;
	
	/**
	 * 自动重定向
	 */
	private static Boolean autoRedirect=false;
	
	/**
	 * 自动会话跟踪
	 */
	private static Boolean autoSessionTrace=true;
	
	/**
	 * Web应用服务器会话跟踪标识
	 */
	private static String sessionId="JSESSIONID";
	
	/**
	 * RestTemplate工具模板构造器
	 */
	private static RestTemplateBuilder restTemplateBuilder;
	
	/**
	 * Web响应实体线程键
	 */
	private static final String WEB_RESPONSE="WEB_RESPONSE";
	
	/**
	 * 等号正则式
	 */
	private static final Pattern EQUALS_REGEX=Pattern.compile("=");
	
	/**
	 * 访问服务器的URL地址栈键
	 */
	private static final String URL_ADDRESS_STACK="URL_ADDRESS_STACK";

	static{
		try{
			restTemplateBuilder=ApplicationUtil.getBean(RestTemplateBuilder.class);
		} catch(RuntimeException e){}
		if(null==restTemplateBuilder) restTemplateBuilder=BeanUtils.instantiateClass(RestTemplateBuilder.class);
		restTemplate=restTemplateBuilder.build();
	}
	
	public static Boolean getAutoRedirect() {
		return autoRedirect;
	}

	public static void setAutoRedirect(Boolean autoRedirect) {
		RestClient.autoRedirect = autoRedirect;
	}

	public static Boolean getAutoSessionTrace() {
		return autoSessionTrace;
	}

	public static void setAutoSessionTrace(Boolean autoSessionTrace) {
		RestClient.autoSessionTrace = autoSessionTrace;
	}
	
	public static String getSessionId() {
		return sessionId;
	}

	public static void setSessionId(String sessionId) {
		RestClient.sessionId = sessionId;
	}
	
	public static URI getFirstUri() {
		return getUrlStack().firstElement();
	}
	
	public static URI getLastUri() {
		return getUrlStack().lastElement();
	}
	
	public static Stack<URI> getUrlStack() {
		return (Stack<URI>)ThreadHolder.peek(URL_ADDRESS_STACK);
	}
	
	public static Stack<URI> removeUrlStack() {
		return (Stack<URI>)ThreadHolder.poll(URL_ADDRESS_STACK);
	}

	public static <E> WebResponse<E> getCacheResponse() {
		return (WebResponse<E>)ThreadHolder.peek(WEB_RESPONSE);
	}
	
	public static <E> WebResponse<E> removeCacheResponse() {
		return (WebResponse<E>)ThreadHolder.poll(WEB_RESPONSE);
	}

	public static RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public static RestTemplateBuilder getRestTemplateBuilder() {
		return restTemplateBuilder;
	}
	
	/**
	 * 获取默认请求头
	 * @return HttpHeaders
	 */
	public static HttpHeaders getDefaultRequestHeader() {
		HttpHeaders httpHeader=new HttpHeaders();
		httpHeader.setConnection("keep-alive");
		httpHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeader.setAccept(Arrays.asList(new MediaType[]{MediaType.TEXT_HTML,MediaType.TEXT_PLAIN,MediaType.APPLICATION_JSON_UTF8,MediaType.APPLICATION_XML}));
		return httpHeader;
	}
	
	/**
	 * 获取自定义请求头
	 * @param headerMap 请求头参数字典
	 * @return HttpHeaders
	 */
	public static HttpHeaders getRequestHeader(Map<String,List<String>>... headerMaps) {
		Map<String,List<String>> headerMap=null==headerMaps||0==headerMaps.length?null:headerMaps[0];
		HttpHeaders httpHeader=new HttpHeaders();
		if(null==headerMap || 0==headerMap.size()) return httpHeader;
		httpHeader.putAll(headerMap);
		return httpHeader;
	}

	/**
	 * @author Louis
	 * Web响应实体类描述
	 */
	public static class WebResponse<E>{
		/**
		 * 响应实体对象
		 */
		private ResponseEntity<E> responseEntity;
		
		public WebResponse(ResponseEntity<E> responseEntity){
			this.responseEntity=responseEntity;
		}
		
		public ResponseEntity<E> getResponseEntity() {
			return responseEntity;
		}

		public Integer getStatusCode(){
			return responseEntity.getStatusCodeValue();
		}
		
		public HttpHeaders getHeader(){
			return responseEntity.getHeaders();
		}
		
		public E getBody(){
			return responseEntity.getBody();
		}
		
		public Boolean hasBody(){
			return responseEntity.hasBody();
		}
		
		public Boolean isRedirect(){
			return 3==responseEntity.getStatusCodeValue()/100;
		}
		
		public String getHeaderValue(String headerName){
			return responseEntity.getHeaders().getFirst(headerName);
		}
		
		public List<String> getHeaderValues(String headerName){
			return responseEntity.getHeaders().getValuesAsList(headerName);
		}
		
		public List<String> getCookies(){
			return responseEntity.getHeaders().getValuesAsList(HttpHeaders.SET_COOKIE);
		}
		
		public URI getLocation(){
			return responseEntity.getHeaders().getLocation();
		}
		
		public String getLocationAsString(){
			return responseEntity.getHeaders().getFirst("Location");
		}
		
		public Long getContentLength(){
			return responseEntity.getHeaders().getContentLength();
		}
		
		public MediaType getContentType(){
			return responseEntity.getHeaders().getContentType();
		}
		
		public String getContentTypeAsString(){
			return responseEntity.getHeaders().getContentType().toString();
		}
		
		public String getUpgrade(){
			return responseEntity.getHeaders().getUpgrade();
		}
		
		public List<MediaType> getAcceptMediaType(){
			return responseEntity.getHeaders().getAccept();
		}
		
		public List<Charset> getAcceptCharset(){
			return responseEntity.getHeaders().getAcceptCharset();
		}
		
		public ContentDisposition getContentDisposition(){
			return responseEntity.getHeaders().getContentDisposition();
		}
		
		public Long getClientExpires(){
			return responseEntity.getHeaders().getExpires();
		}
		
		public InetSocketAddress getServerHost(){
			return responseEntity.getHeaders().getHost();
		}
		
		public String getCacheControl(){
			return responseEntity.getHeaders().getCacheControl();
		}
	}
	
	/**
	 * 使用DELETE方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> delete(String url,Object... uriVariables){
		return delete(url,null,String.class,uriVariables);
	}
	
	/**
	 * 使用DELETE方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> delete(String url,HttpHeaders httpHeader,Object... uriVariables){
		return delete(url,httpHeader,String.class,uriVariables);
	}
	
	/**
	 * 使用DELETE方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> delete(String url,Class<E> returnType,Object... uriVariables){
		return delete(url,null,returnType,uriVariables);
	}
	
	/**
	 * 使用DELETE方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> delete(String url,HttpHeaders httpHeader,Class<E> returnType,Object... uriVariables){
		return request(url,HttpMethod.DELETE,httpHeader,null,returnType,uriVariables);
	}
	
	/**
	 * 使用PUT方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> put(String url,Object httpBody,Object... uriVariables){
		return put(url,null,httpBody,String.class,uriVariables);
	}
	
	/**
	 * 使用PUT方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> put(String url,HttpHeaders httpHeader,Object httpBody,Object... uriVariables){
		return put(url,httpHeader,httpBody,String.class,uriVariables);
	}
	
	/**
	 * 使用PUT方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> put(String url,Object httpBody,Class<E> returnType,Object... uriVariables){
		return put(url,null,httpBody,returnType,uriVariables);
	}
	
	/**
	 * 使用PUT方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> put(String url,HttpHeaders httpHeader,Object httpBody,Class<E> returnType,Object... uriVariables){
		return request(url,HttpMethod.PUT,httpHeader,httpBody,returnType,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> post(String url,Object... uriVariables){
		return post(url,null,null,String.class,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> post(String url,HttpHeaders httpHeader,Object... uriVariables){
		return post(url,httpHeader,null,String.class,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> post(String url,Object httpBody,Object... uriVariables){
		return post(url,null,httpBody,String.class,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> post(String url,HttpHeaders httpHeader,Object httpBody,Object... uriVariables){
		return post(url,httpHeader,httpBody,String.class,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> post(String url,Class<E> returnType,Object... uriVariables){
		return post(url,null,null,returnType,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> post(String url,HttpHeaders httpHeader,Class<E> returnType,Object... uriVariables){
		return post(url,httpHeader,null,returnType,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> post(String url,Object httpBody,Class<E> returnType,Object... uriVariables){
		return post(url,null,httpBody,returnType,uriVariables);
	}
	
	/**
	 * 使用POST方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> post(String url,HttpHeaders httpHeader,Object httpBody,Class<E> returnType,Object... uriVariables){
		return request(url,HttpMethod.POST,httpHeader,httpBody,returnType,uriVariables);
	}
	
	/**
	 * 使用GET方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> get(String url,Object... uriVariables){
		return get(url,null,String.class,uriVariables);
	}
	
	/**
	 * 使用GET方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> get(String url,HttpHeaders httpHeader,Object... uriVariables){
		return get(url,httpHeader,String.class,uriVariables);
	}
	
	/**
	 * 使用GET方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> get(String url,Class<E> returnType,Object... uriVariables){
		return get(url,null,returnType,uriVariables);
	}
	
	/**
	 * 使用GET方式请求指定的URL地址
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param httpHeader 请求消息头
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> get(String url,HttpHeaders httpHeader,Class<E> returnType,Object... uriVariables){
		return request(url,HttpMethod.GET,httpHeader,null,returnType,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> request(String url,HttpMethod method,Object... uriVariables){
		return request(url,method,null,null,String.class,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpHeader 请求消息头
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> request(String url,HttpMethod method,HttpHeaders httpHeader,Object... uriVariables){
		return request(url, method, httpHeader,null, String.class,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> request(String url,HttpMethod method,Object httpBody,Object... uriVariables){
		return request(url, method, null,httpBody, String.class,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static WebResponse<String> request(String url,HttpMethod method,HttpHeaders httpHeader,Object httpBody,Object... uriVariables){
		return request(url, method, httpHeader,httpBody, String.class,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> request(String url,HttpMethod method,Class<E> returnType,Object... uriVariables){
		return request(url, method, null,null,returnType,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpHeader 请求消息头
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> request(String url,HttpMethod method,HttpHeaders httpHeader,Class<E> returnType,Object... uriVariables){
		return request(url, method, httpHeader,null,returnType,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables 地址模板中标记的可选参数表或参数字典
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> request(String url,HttpMethod method,Object httpBody,Class<E> returnType,Object... uriVariables){
		return request(url, method, null,httpBody,returnType,uriVariables);
	}
	
	/**
	 * 发起HTTP请求
	 * @param url 请求地址模板(?var={1}或var={key})
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpHeader 请求消息头
	 * @param httpBody 请求消息体
	 * @param returnType 响应消息体封装类型
	 * @param uriVariables url中标记的可选参数表(可变对象数组Object[]或对象字典Map<String,Object>)
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> request(String url,HttpMethod method,HttpHeaders httpHeader,Object httpBody,Class<E> returnType,Object... uriVariables){
		URI uri =null;
		if(null!=uriVariables && 1==uriVariables.length && uriVariables[0] instanceof Map){
			uri = restTemplate.getUriTemplateHandler().expand(url, (Map)uriVariables[0]);
		}else if(null==uriVariables){
			uri = restTemplate.getUriTemplateHandler().expand(url, new Object[0]);
		}else{
			uri = restTemplate.getUriTemplateHandler().expand(url, uriVariables);
		}
		
		if(null==uri) return null;
		
		Object preWebResponse=ThreadHolder.peek(WEB_RESPONSE);
		if(null==httpHeader) httpHeader=getDefaultRequestHeader();
		List<String> currentCookies=httpHeader.getValuesAsList(HttpHeaders.COOKIE);
		if(null!=preWebResponse && (null==currentCookies || 0==currentCookies.size())){
			List<String> preCookies=((WebResponse<E>)preWebResponse).getHeaderValues(HttpHeaders.SET_COOKIE);
			if(null!=preCookies && 0!=preCookies.size())httpHeader.put(HttpHeaders.COOKIE, preCookies);
		}
		
		if(null==httpBody || MultiValueMap.class.isAssignableFrom(httpBody.getClass()) || !MediaType.APPLICATION_FORM_URLENCODED.equals(httpHeader.getContentType()))
		return request(uri, method, new HttpEntity(httpBody,httpHeader),returnType);
		
		Set<Map.Entry<Object, Object>> entrys=null;
		if(Map.class.isAssignableFrom(httpBody.getClass())){
			entrys=((Map)httpBody).entrySet();
		}else{
			entrys=BeanMap.create(httpBody).entrySet();
		}
		
		MultiValueMap multiValueMapBody=new LinkedMultiValueMap();
		for(Entry entry:entrys) multiValueMapBody.add(entry.getKey(), entry.getValue());
		return request(uri, method, new HttpEntity(multiValueMapBody,httpHeader),returnType);
	}
	
	/**
	 * 发起HTTP请求
	 * @param uri 请求地址(含URL参数)
	 * @param method 请求方式(get/post/put/delete/patch/head/option)
	 * @param httpEntity 请求消息(含消息头header和消息体body)
	 * @param returnType 响应消息体封装类型
	 * @return WebResponse
	 */
	public static <E> WebResponse<E> request(URI uri,HttpMethod method,HttpEntity httpEntity,Class<E> returnType){
		RequestCallback requestCallback = restTemplate.httpEntityCallback(httpEntity, returnType);
		ResponseExtractor<ResponseEntity<E>> responseExtractor = restTemplate.responseEntityExtractor(returnType);
		ResponseEntity<E> responseEntity=restTemplate.execute(uri, method, requestCallback, responseExtractor);
		
		Stack<URI> urlStack=(Stack)ThreadHolder.peek(URL_ADDRESS_STACK);
		if(null==urlStack) ThreadHolder.push(URL_ADDRESS_STACK, urlStack=new Stack<URI>());
		urlStack.push(uri);
		
		WebResponse<E> webResponse=new WebResponse<E>(responseEntity);
		if(autoSessionTrace)cacheResponse(webResponse);
		if(!autoRedirect || !webResponse.isRedirect()) return webResponse;
		String location=webResponse.getLocationAsString();
		if(null==location || location.trim().isEmpty()) return webResponse;
		return request(location.trim(), HttpMethod.GET, null,null,returnType,new Object[0]);
	}
	
	/**
	 * 缓存Web服务响应结果
	 * @param webResponse Web响应
	 * @return 是否被缓存
	 */
	private static <E> Boolean cacheResponse(WebResponse<E> webResponse){
		WebResponse<E> preWebResponse=(WebResponse<E>)ThreadHolder.peek(WEB_RESPONSE);
		if(null==preWebResponse){
			ThreadHolder.push(WEB_RESPONSE, webResponse);
			return true;
		}
		
		List<String> preCookies=preWebResponse.getHeaderValues(HttpHeaders.SET_COOKIE);
		for(String setCookie:preCookies) if(sessionId.equalsIgnoreCase(EQUALS_REGEX.split(setCookie)[0])) return false;
		ThreadHolder.push(WEB_RESPONSE, webResponse);
		return true;
	}
}
