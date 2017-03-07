package com.yuaiwan.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @类功能说明： 提供Md5加密的工具类
 * @类修改者：
 * @修改日期：
 * @修改说明：
 * @公司名称：qm
 * @作者：zp
 * @创建时间：2016年6月16日 下午1:50:16
 * @版本：V1.0
 */
public class Md5Utils {

	/**
	 * 
	 * @方法功能说明： 对字符串进行加密 
	 * @创建日期：2016年6月16日
	 * @创建者：zp
	 * @修改日期 ：
	 * @修改者 ： 
	 * @修改内容：
	 * @参数： @param content
	 * @参数： @return
	 * @return String
	 * @throws
	 */
	public static String getMD5(String content) {
		String md5str = "";
		try {
			byte data[] = content.getBytes("UTF8");
			// 生成MessageDigest对象
			MessageDigest m = MessageDigest.getInstance("MD5");
			// 将原始数据传递给该对象
			m.update(data);
			// 计算消息摘要
			byte resultData[] = m.digest();
			md5str = bytesToHexString(resultData);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return md5str;
	}

	/**
	 * 
	 * @方法功能说明：  Convert byte[] to hex string.
	 * @创建日期：2016年6月16日 
	 * @创建者：zp
	 * @修改日期 ：
	 * @修改者 ： 
	 * @修改内容：
	 * @参数： @param src
	 * @参数： @return
	 * @return String
	 * @throws
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}
