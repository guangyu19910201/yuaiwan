package com.yuaiwan.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpClientUtil {
	public static String doGet(String url) throws Exception{
		// 构造HttpClient的实例
		HttpClient httpClient = new HttpClient(); // 创建GET方法的实例
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
		GetMethod getMethod = new GetMethod(url);//http://192.168.16.41:8080/zzlform/form/mobile/zzlformperson?uid=228
		// 使用系统提供的默认的恢复策略
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());
		getMethod.addRequestHeader("Content-type" , "text/html; charset=utf-8"); 
		getMethod.getParams().setContentCharset("utf-8");
		String result = "";
		int statusCode = httpClient.executeMethod(getMethod);
		if (statusCode == HttpStatus.SC_OK) {
			result=new String(getMethod.getResponseBodyAsString());
		} else {
			throw new Exception("请求失败:(错误码:"+statusCode+")");
		}
		return result;
	}
	
	public static JSONObject doPost(String url,NameValuePair... param) throws Exception{ 
		PostMethod post = new PostMethod(url);
		post.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8"); 
		post.setRequestBody(param);
		HttpClient httpClient = new HttpClient();   
		int statusCode = httpClient.executeMethod(post); 
		if (statusCode == HttpStatus.SC_OK) {
			String result=new String(post.getResponseBodyAsString());
			post.releaseConnection();
			return JSONObject.fromObject(result);
		} else {
			post.releaseConnection();
			throw new Exception("请求失败:(错误码:"+statusCode+")");
		}
	}
	
	public static JSONObject doPost(String url,List<NameValuePair> paramList) throws Exception{ 
		PostMethod post = new PostMethod(url);
		post.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8"); 
		post.setRequestBody(paramList.toArray(new NameValuePair[paramList.size()]));
		HttpClient httpClient = new HttpClient();   
		int statusCode = httpClient.executeMethod(post); 
		if (statusCode == HttpStatus.SC_OK) {
			String result=new String(post.getResponseBodyAsString());
			post.releaseConnection();
			return JSONObject.fromObject(result);
		} else {
			post.releaseConnection();
			throw new Exception("请求失败:(错误码:"+statusCode+")");
		}
	}
	
	public static JSONObject doPost(String url,Map<String,String> stringParamMap,Map<String,File> fileParamMap) throws Exception{ 
		PostMethod post = new PostMethod(url);
		List<Part> paramList = new ArrayList<Part>();
		if(!ValidateUtils.isBlank(fileParamMap)){
			for (Map.Entry<String, File> entry : fileParamMap.entrySet()) {
				paramList.add(new FilePart(entry.getKey(), entry.getValue()));
			}
		}
		if(!ValidateUtils.isBlank(stringParamMap)){
			for (Map.Entry<String,String> entry : stringParamMap.entrySet()) {
				paramList.add(new StringPart(entry.getKey(), entry.getValue()));
			}
		}
		post.setRequestEntity(new MultipartRequestEntity(paramList.toArray(new Part[paramList.size()]),post.getParams()));
		HttpClient client = new HttpClient();
		int statusCode = client.executeMethod(post);
		if(statusCode == HttpStatus.SC_OK){
			String result = post.getResponseBodyAsString();
			post.releaseConnection();
			return JSONObject.fromObject(result);
		}else{
			post.releaseConnection();
			throw new Exception("请求失败:(错误码:"+statusCode+")");
		}
	}
}
