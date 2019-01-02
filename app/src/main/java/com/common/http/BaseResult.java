package com.common.http;

import java.io.Serializable;

import org.json.JSONObject;

public class BaseResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3328378153519235866L;
	public int errorCode;//        错误码	
	public long time;//            请求处理时间，单位为秒
	
	public String token;
	public JSONObject object;//业务请求返回数据
}