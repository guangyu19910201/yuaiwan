package com.yuaiwan.anno;

/**
 * 数据库表操作的类型
 * @author guangyu
 */
public enum TableAutoType {
	MANUAL,//不做处理
	AUTO_CREATETABLE,//自动创建表
	AUTO_CREATECOLUM,//自动增加列
	AUTO_DELETECOLUM,//自动删除列
	AUTO_ADDCOMMENT,//自动增加注释
	AUTO_CHECKDIFFERENCE;//自动比较列的不同
}
