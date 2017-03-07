package com.yuaiwan.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuaiwan.dao.AbstractSuperDao;

/**
 * 集成了基本的增删改查方法
 * @author guangyu
 * @param <T>
 */
public abstract class SuperService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	public abstract AbstractSuperDao getDao();
	
	/**
	 * 根据sql插叙出map结构的结果集合
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String,Object>> queryForMapList(String sql,Object... args){
		return this.getDao().queryForMapList(sql, args);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public <T> List<T> queryForEntityListByMap(Class<T> clazz,String sql,Map<String,?> paramMap){
		return this.getDao().queryForEntityListByMap(clazz, sql, paramMap);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public <T> List<T> queryForEntityList(Class<T> clazz,String sql,Object... params){
		return this.getDao().queryForEntityList(clazz,sql,params);
	}
	
	/**
	 * 根据指定条件查出T实体类集合,当where中不传入删除标记的时候,该方法会自动校验删除标记 //TODO
	 * @param clazz
	 * @param where
	 * @param startIndex
	 * @param size
	 * @return
	 */
	public <T> List<T> queryForEntityList(Class<T> clazz,Map<String,?> where,int startIndex,int size){
		return this.getDao().queryForEntityList(clazz,where,startIndex,size);
	}
	
	/**
	 * 根据指定条件查出T实体类集合,不自动校验删除标记//TODO
	 * @param clazz
	 * @param where
	 * @param startIndex
	 * @param size
	 * @return
	 */
	public <T> List<T> queryForEntityUncheckDeleteList(Class<T> clazz,Map<String,?> where,int startIndex,int size){
		return this.getDao().queryForEntityUncheckDeleteList(clazz,where,startIndex,size);
	}
	
	/** 
	 * 插入并返回受影响的条数(会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int insert(T t){
		return this.getDao().insert(t);
	}
	
	/**
	 * 更新 (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int update(T t){
		return this.getDao().update(t);
	}

	/**
	 * 加锁更新  (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int updateUnLock(T t){
		return this.getDao().updateUnLock(t);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public <T> int delete(Class<T> clazz,Serializable id){
		return this.getDao().delete(clazz, id);
	}
	
	/**
	 * 删除
	 * @param where 条件
	 * @return
	 */
	public <T> int delete(Class<T> clazz,Map<String,?> where){
		return this.getDao().delete(clazz, where);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param id
	 * @return
	 */
	public <T> int deleteData(Class<T> clazz,Serializable id){
		return this.getDao().deleteData(clazz, id);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param where 条件
	 * @return
	 */
	public <T> int deleteData(Class<T> clazz,Map<String,?> where){
		return this.getDao().deleteData(clazz, where);
	}
	
	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param args
	 */
	public int update(String sql,Object... args) {
		return this.getDao().update(sql, args);
	}
}
