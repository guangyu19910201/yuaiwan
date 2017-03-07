package com.yuanwan.wx.utils;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.sf.json.JSONObject;

import com.yuaiwan.utils.Config;
import com.yuaiwan.utils.HttpClientUtil;

/**
 * 请求校验工具类
 * @author guangyu
 */
public class SignUtil {
	//获取微信token
	private static String token = Config.getProperties().getProperty("TOKEN");
	
	/**
	 * 验证签名
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public static boolean checkSignature(String signature, String timestamp, String nonce) {
		String[] arr = new String[] { token, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		content = null;
		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
	}

	/**
	 * 将字节数组转换为十六进制字符串
	 * @param byteArray
	 * @return
	 */
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 * @param mByte
	 * @return
	 */
	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];
		String s = new String(tempArr);
		return s;
	}
	
	/**
	 * 通过openod获取用户基本信息
	 * @param openid
	 * @param request
	 * @return
	 */
	public static JSONObject getUserInfo(String openid){
		try {
			JSONObject result1 = JSONObject.fromObject(HttpClientUtil.doGet( "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+Config.getProperties().getProperty("AppID")+"&secret="+Config.getProperties().getProperty("AppSecret")));
			JSONObject result2 = JSONObject.fromObject(HttpClientUtil.doGet( "https://api.weixin.qq.com/cgi-bin/user/info?access_token="+result1.getString("access_token")+"&openid="+openid+"&lang=zh_CN"));
			return result2;
		} catch (Exception e) {
			return null;
		}
	}
}
