package com.github.lixiang2114.flow.context;

import java.util.Date;
import java.util.HashMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * @author Lixiang
 */
public class Token {
	/**
	 * 当前Token值
	 */
	private String token;
	
	/**
	 * Token创建时间
	 */
	private long createTime;
	
	/**
	 * 验证Token的秘钥文本
	 */
	private String jwtSecret;
	
	/**
	 * 用户名
	 */
	private String userName;
	
	/**
	 * Token过期时长(单位:秒,>=1200s)
	 */
	private int tokenExpire;
	
	/**
	 * Token过期时间因子
	 */
	private Integer expireFactor;
	
	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setTokenExpire(int tokenExpire) {
		this.tokenExpire = tokenExpire;
	}

	public void setExpireFactor(Integer expireFactor) {
		this.expireFactor = expireFactor;
	}

	public Token(){}
	
	/**
	 * @param jwtSecret
	 * @param tokenExpire
	 * @throws Exception
	 */
	public Token(String jwtSecret,int tokenExpire) throws Exception{
		this(jwtSecret,tokenExpire,null,null);
	}
	
	/**
	 * @param jwtSecret
	 * @param tokenExpire
	 * @param factor
	 * @throws Exception
	 */
	public Token(String jwtSecret,int tokenExpire,Integer factor) throws Exception{
		this(jwtSecret,tokenExpire,null,factor);
	}
	
	/**
	 * @param jwtSecret
	 * @param tokenExpire
	 * @param userName
	 * @param factor
	 * @return Token
	 * @throws Exception
	 */
	public Token(String jwtSecret,int tokenExpire,String userName,Integer factor) throws Exception{
		if(tokenExpire<1200) throw new RuntimeException("ERROR:===token expire can not smaller than 20 minute...");
		this.expireFactor=factor;
		this.tokenExpire=tokenExpire;
		this.userName=userName;
		this.jwtSecret=jwtSecret;
		createToken();
	}
	
	/**
	 * @param userName
	 * @return Token
	 * @throws Exception
	 */
	public String getToken() {
		if(null==token) throw new RuntimeException("ERROR:===token has not been initialized...");
		if(System.currentTimeMillis()-createTime>(tokenExpire*expireFactor)) return createToken();
		return token;
	}
	
	/**
	 * @param userName
	 * @return Token
	 * @throws Exception
	 */
	private String createToken() {
		HashMap<String,Object> headerClaims=new HashMap<String,Object>();
		headerClaims.put("typ", "JWT");
		headerClaims.put("alg", "HS256");
		JWTCreator.Builder builder=JWT.create();
		if(null!=userName) builder.withSubject(userName);
		return token=builder.withExpiresAt(new Date((createTime=System.currentTimeMillis())+tokenExpire*1000L))
				.withHeader(headerClaims).withIssuedAt(new Date()).sign(Algorithm.HMAC256(jwtSecret));
    }
}
