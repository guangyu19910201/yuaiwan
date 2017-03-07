package com.yuaiwan.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.yuaiwan.annoparse.SQLGenerator;
import com.yuaiwan.annoparse.SQLGeneratorFactory;
import com.yuaiwan.annoparse.SQLMap;
import com.yuaiwan.resultset.RowMapperForModel;
import com.yuaiwan.utils.ReflectUtils;

/**
 * 该dao不需要泛型,service可以直接注入该dao做一些特殊操作
 * 主要用于查询返回自定义javabean结构的实体类集合
 * @author guangyu
 */
@Repository
public class AbstractSuperDao{
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	//需要注解扫描com.yuaiwan.dao注入jdbcTemplate,或者重写setJdbcTemplate注入
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		//初始化 namedParameterJdbcTemplate
		this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}
	
	protected NamedParameterJdbcTemplate getJdbcTemplate(){
		return jdbcTemplate;
	}
	
	/**
	 * 更新
	 * @return
	 */
	protected int update(SQLMap sqlMap){
		return this.getJdbcTemplate().update(sqlMap.getSql().toString(), sqlMap.getParamMap());
	}
	
	/**
	 * 根据sql插叙出map结构的结果集合
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String,Object>> queryForMapList(String sql,Object... args){
		return this.getJdbcTemplate().getJdbcOperations().queryForList(sql, args);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public <T> List<T> queryForEntityList(Class<T> clazz,String sql,Object... params){
		return this.getJdbcTemplate().getJdbcOperations().query(sql,new RowMapperForModel<T>(clazz),params);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public <T> List<T> queryForEntityListByMap(Class<T> clazz,String sql,Map<String,?> paramMap){
		return this.getJdbcTemplate().query(sql,paramMap,new RowMapperForModel<T>(clazz));
	}
	
	/**
	 * 根据指定条件查出T实体类集合,当where中不传入删除标记的时候,该方法会自动校验删除标记 //TODO
	 * @param clazz
	 * @param where
	 * @param startIndex
	 * @param size
	 * @return
	 */
	public <T> List<T> queryForEntityList(Class<T> clazz,Map<String,?> where,int startIndex ,int size){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getQuerySql(null,where,startIndex,size,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(), new RowMapperForModel<T>(clazz));
	}
	
	/**
	 * 根据指定条件查出T实体类集合,不自动校验删除标记//TODO
	 * @param clazz
	 * @param where
	 * @param startIndex
	 * @param size
	 * @return
	 */
	public <T> List<T> queryForEntityUncheckDeleteList(Class<T> clazz,Map<String,?> where,int startIndex ,int size){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getQuerySql(null,where,startIndex,size,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(), new RowMapperForModel<T>(clazz));
	}
	
	/** 
	 * 插入并返回影响的条数(会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int insert(T t){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) t.getClass();
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		String sql = sqlGenerator.getInsertSql(t);  
		SqlParameterSource ps = new BeanPropertySqlParameterSource(t); 
		if(sqlGenerator.isAutoIncrement()){
			KeyHolder keyholder = new GeneratedKeyHolder();
			int insertCount = this.getJdbcTemplate().update(sql, ps,keyholder);
			if (insertCount > 0 && sqlGenerator.isAutoIncrement()) {
				//自增型主键,返回num类型的主键
				Number key = keyholder.getKey();
				//将主键赋值给t
				ReflectUtils.setFieldValue(t, sqlGenerator.getPrimaryKey(), key);
			}
			return insertCount;
		}else{
			return this.getJdbcTemplate().update(sql, ps);
		}
	}
	
	/**
	 * 更新 (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int update(T t){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) t.getClass();
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getUpdateSql(t);  
		return this.update(sqlMap);
	}

	/**
	 * 不加锁更新,不进行版本号校验会强制更新数据  (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public <T> int updateUnLock(T t){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) t.getClass();
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getUnLockUpdateSql(t);  
		return this.update(sqlMap);
	}
	
	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param args
	 */
	public int update(String sql,Object... args) {
		return this.getJdbcTemplate().getJdbcOperations().update(sql,args);
	}
	
	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param args
	 */
	public int updateByMap(String sql,Map<String,?> paramMap) {
		return this.getJdbcTemplate().update(sql,paramMap);
	}
	
	/**
	 * 提供对于T表更更新某些字段的方法,返回影响条数
	 * @param sql
	 * @param args
	 */
	public <T> int updateEntity(Class<T> clazz,Map<String,?> setMap,Map<String,?> where) {
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getUpdateSql(setMap, where);
		return this.update(sqlMap);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public <T> int delete(Class<T> clazz,Serializable id){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getDeleteSql(id);
		return this.update(sqlMap);
	}
	
	/**
	 * 删除
	 * @param where 条件
	 * @return
	 */
	public <T> int delete(Class<T> clazz,Map<String,?> where){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getDeleteSql(where);  
		return this.update(sqlMap);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param id
	 * @return
	 */
	public <T> int deleteData(Class<T> clazz,Serializable id){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getDeleteDataSql(id);  
		return this.update(sqlMap);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param where 条件
	 * @return
	 */
	public <T> int deleteData(Class<T> clazz,Map<String,?> where){
		SQLGenerator<T> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		SQLMap sqlMap = sqlGenerator.getDeleteDataSql(where);  
		return this.update(sqlMap);
	}
}
