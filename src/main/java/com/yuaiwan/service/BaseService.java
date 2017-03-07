package com.yuaiwan.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuaiwan.dao.AbstractBaseDao;

/**
 * 集成了基本的增删改查方法
 * @author guangyu
 * @param <T>
 */
public abstract class BaseService<T> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	public abstract AbstractBaseDao<T> getDao();
	
	/** 
	 * 插入并返回影响的条数(会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int insert(T t) {
		return this.getDao().insert(t);
	}

	/**
	 * 更新 (该方法会自动检测实体类中的版本号,会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int update(T t) {
		return this.getDao().update(t);
	}

	/**
	 * 不加锁更新,不进行版本号校验会强制更新数据,也不会更新版本号 (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int updateUnLock(T t) {
		return this.getDao().updateUnLock(t);
	}

	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param args
	 */
	public int update(String sql, Object... args) {
		return this.getDao().update(sql, args);
	}

	/**
	 * 提供对于T表更更新某些字段的方法,返回影响条数
	 * 当where中加入版本号字段或删除字段条件后才会进行对应的校验
	 * @param sql
	 * @param where
	 */
	public int updateEntity(Map<String, ?> setMap,Map<String, ?> where) {
		return this.getDao().updateEntity(setMap, where);
	}
	
	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param paramMap
	 */
	public int updateByMap(String sql, Map<String, ?> paramMap) {
		return this.getDao().updateByMap(sql, paramMap);
	}

	/**
	 * 更新实体
	 * @param id
	 * @param setMap
	 * @return
	 */
	public int updateEntity(Serializable id, Map<String, ?> setMap) {
		return this.getDao().updateEntity(id, setMap);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public int delete(Serializable id) {
		return this.getDao().delete(id);
	}

	/**
	 * 删除
	 * @param where 条件
	 * @return
	 */
	public int delete(Map<String,?> where) {
		return this.getDao().delete(where);
	}

	/**
	 * 真正的删除这条数据
	 * @param id
	 * @return
	 */
	public int deleteData(Serializable id) {
		return this.getDao().deleteData(id);
	}

	/**
	 * 真正的删除这条数据
	 * @param where 条件
	 * @return
	 */
	public int deleteData(Map<String,?> where) {
		return this.getDao().deleteData(where);
	}

	/**
	 * 根据主键id查询实体,该方法会自动校验删除标记
	 * @param id 主键
	 * @return
	 */
	public T get(Serializable id) {
		return this.getDao().get(id);
	}
	
	/**
	 * 根据主键id查询实体,不校验删除标记 
	 * @param id 主键
	 * @return
	 */
	public T getUncheckDelete(Serializable id){
		return this.getDao().getUncheckDelete(id);
	}
	
	/**
	 * 根据指定条件查询实体,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 条件
	 * @return
	 */
	public T get(Map<String,?> where) {
		return this.getDao().get(where);
	}
	
	/**
	 * 根据指定条件查询实体,不自动校验删除标记
	 * @param where 条件
	 * @return
	 */
	public T getUncheckDelete(Map<String,?> where) {
		return this.getDao().getUncheckDelete(where);
	}
	
	/**
	 * 根据主键id查询类中的某个字段,该方法会自动校验删除标记
	 * @param id 主键
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public Map<String, Object> getMap(Serializable id, List<String> fieldList) {
		return this.getDao().getMap(id, fieldList);
	}
	
	/**
	 * 根据主键id查询类中的某个字段,不校验删除标记
	 * @param id 主键
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public Map<String, Object> getMapUncheckDelete(Serializable id, List<String> fieldList) {
		return this.getDao().getMapUncheckDelete(id, fieldList);
	}
	
	/**
	 * 根据主键id查询类中的某个字段,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param fieldList T类中的字段值或列名
	 * @param where 条件
	 * @return
	 */
	public Map<String, Object> getMap(List<String> fieldList,Map<String, ?> where) {
		return this.getDao().getMap(fieldList, where);
	}
	
	/**
	 * 根据主键id查询类中的某个字段,不自动校验删除标记
	 * @param fieldList T类中的字段值或列名
	 * @param where 条件
	 * @return
	 */
	public Map<String, Object> getMapUncheckDelete(List<String> fieldList,Map<String, ?> where) {
		return this.getDao().getMapUncheckDelete(fieldList, where);
	}

	/**
	 * 根据查询条件查询类中的某个字段,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 查询条件
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public List<Map<String,Object>> queryForMapList(List<String> fieldList,Map<String, ?> where,int startIndex ,int size){
		return this.getDao().queryForMapList(fieldList, where,startIndex,size);
	}
	
	/**
	 * 根据查询条件查询类中的某个字段,不自动校验删除标记
	 * @param where 查询条件
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public List<Map<String,Object>> queryForMapUncheckDeleteList(List<String> fieldList,Map<String, ?> where,int startIndex ,int size){
		return this.getDao().queryForMapUncheckDeleteList(fieldList, where,startIndex,size);
	}
	
	/**
	 * 根据sql查询出结果返回map对象
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object... args) {
		return this.getDao().queryForMapList(sql, args);
	}

	/**
	 * 使用where条件查询出T实体的集合,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 查询条件
	 * @return
	 */
	public List<T> queryForEntityList(Map<String, ?> where,int startIndex ,int size) {
		return this.getDao().queryForEntityList(where,startIndex,size);
	}
	
	/**
	 * 使使用where条件查询出T实体的集合,不自动校验删除标记
	 * @param where 查询条件
	 * @return
	 */
	public List<T> queryForEntityUncheckDeleteList(Map<String, ?> where,int startIndex ,int size) {
		return this.getDao().queryForEntityUncheckDeleteList(where,startIndex,size);
	}

	/**
	 * 根据sql查询出T实体的集合
	 * @param sql
	 * @param params
	 * @return
	 */
	public List<T> queryForEntityList(String sql, Object... params) {
		return this.getDao().queryForEntityList(sql, params);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public List<T> queryForEntityListByMap(String sql, Map<String,?> paramMap) {
		return this.getDao().queryForEntityListByMap(sql, paramMap);
	}
	
	/**
	 * 根据sql查询出一个单一的值
	 * @param sql
	 * @param params
	 * @return
	 */
	public String getValue(String sql,Object... params){
		return this.getDao().getValue(sql, params);
	}
}
