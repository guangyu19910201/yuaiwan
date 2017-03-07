package com.yuaiwan.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.yuaiwan.annoparse.SQLGenerator;
import com.yuaiwan.annoparse.SQLGeneratorFactory;
import com.yuaiwan.annoparse.SQLMap;
import com.yuaiwan.resultset.ResultMap;
import com.yuaiwan.resultset.ResultSetForMap;
import com.yuaiwan.resultset.RowMapperForModel;
import com.yuaiwan.utils.AutoTableExecutor;
import com.yuaiwan.utils.ReflectUtils;
import com.yuaiwan.utils.ValidateUtils;


/**
 * 集成了基本的增删改查方法
 * @author guangyu
 * @param <T>
 */
public abstract class AbstractBaseDao<T>{
	private Class<T> clazz;
	
	/**
	 * sql生成器
	 */
	private SQLGenerator<T> sqlGenerator;
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	//需要注解扫描com.yuaiwan.dao注入jdbcTemplate,或者重写setJdbcTemplate注入
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		//初始化 namedParameterJdbcTemplate
		this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		AutoTableExecutor.addAutoTableClass(clazz);
	}
	
	protected NamedParameterJdbcTemplate getJdbcTemplate(){
		return jdbcTemplate;
	}
	
	@SuppressWarnings("unchecked")
	public AbstractBaseDao() {
		Type type = this.getClass().getGenericSuperclass();
		ParameterizedType pt = (ParameterizedType) type;
		clazz = (Class<T>)pt.getActualTypeArguments()[0];
		// 初始化sql生成器
		sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
	}

	/** 
	 * 插入并返回影响的条数(会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int insert(T t){
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
	 * 更新
	 * @return
	 */
	protected int update(SQLMap sqlMap){
		return this.getJdbcTemplate().update(sqlMap.getSql().toString(), sqlMap.getParamMap());
	}
	
	/**
	 * 更新 (该方法会自动检测实体类中的版本号,会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int update(T t){
		SQLMap sqlMap = sqlGenerator.getUpdateSql(t);  
		return this.update(sqlMap);
	}
	
	/**
	 * 不加锁更新,不进行版本号校验会强制更新数据,也不会更新版本号 (会自动向实体类中的字段赋值新值)
	 * @param t
	 * @return
	 */
	public int updateUnLock(T t){
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
	 * 提供对于T表更更新某些字段的方法,返回影响条数
	 * 当where中加入版本号字段或删除字段条件后才会进行对应的校验
	 * @param sql
	 * @param where
	 */
	public int updateEntity(Map<String,?> setMap,Map<String,?> where) {
		SQLMap sqlMap = sqlGenerator.getUpdateSql(setMap, where);
		return this.update(sqlMap);
	}

	/**
	 * 执行任意sql语句,返回影响条数
	 * @param sql
	 * @param paramMap
	 */
	public int updateByMap(String sql,Map<String,?> paramMap) {
		return this.getJdbcTemplate().update(sql,paramMap);
	}

	/**
	 * 更新实体
	 * @param id
	 * @param setMap
	 * @return
	 */
	public int updateEntity(Serializable id,Map<String,?> setMap) {
		SQLMap sqlMap = sqlGenerator.getUpdateSql(id, setMap);
		return this.update(sqlMap);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public int delete(Serializable id){
		SQLMap sqlMap = sqlGenerator.getDeleteSql(id);
		return this.update(sqlMap);
	}
	
	/**
	 * 删除
	 * @param where 条件
	 * @return
	 */
	public int delete(Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getDeleteSql(where);  
		return this.update(sqlMap);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param id
	 * @return
	 */
	public int deleteData(Serializable id){
		SQLMap sqlMap = sqlGenerator.getDeleteDataSql(id);  
		return this.update(sqlMap);
	}
	
	/**
	 * 真正的删除这条数据
	 * @param where 条件
	 * @return
	 */
	public int deleteData(Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getDeleteDataSql(where);  
		return this.update(sqlMap);
	}
	
	/**
	 * 根据主键id查询实体,该方法会自动校验删除标记 
	 * @param id 主键
	 * @return
	 */
	public T get(Serializable id){
		SQLMap sqlMap = sqlGenerator.getGetSql(id, null,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultMap<T>(clazz));
	}
	
	/**
	 * 根据主键id查询实体,不校验删除标记 
	 * @param id 主键
	 * @return
	 */
	public T getUncheckDelete(Serializable id){
		SQLMap sqlMap = sqlGenerator.getGetSql(id, null,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultMap<T>(clazz));
	}
	
	/**
	 * 根据指定条件查询实体,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 条件
	 * @return
	 */
	public T get(Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getQuerySql(null, where, 0, 1,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultMap<T>(clazz));
	}
	
	/**
	 * 根据指定条件查询实体,不自动校验删除标记
	 * @param where 条件
	 * @return
	 */
	public T getUncheckDelete(Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getQuerySql(null, where, 0, 1,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultMap<T>(clazz));
	}
	
	/**
	 * 根据主键id查询类中的某个字段,该方法会自动校验删除标记
	 * @param id 主键
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public Map<String,Object> getMap(Serializable id,List<String> fieldList){
		SQLMap sqlMap = sqlGenerator.getGetSql(id,fieldList,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultSetForMap(fieldList));
	}
	
	/**
	 * 根据主键id查询类中的某个字段,不校验删除标记
	 * @param id 主键
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public Map<String,Object> getMapUncheckDelete(Serializable id,List<String> fieldList){
		SQLMap sqlMap = sqlGenerator.getGetSql(id,fieldList,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultSetForMap(fieldList));
	}
	
	/**
	 * 根据主键id查询类中的某个字段,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param fieldList T类中的字段值或列名
	 * @param where 条件
	 * @return
	 */
	public Map<String,Object> getMap(List<String> fieldList,Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getQuerySql(fieldList, where, 0, 1,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultSetForMap(fieldList));
	}
	
	/**
	 * 根据主键id查询类中的某个字段,不自动校验删除标记
	 * @param fieldList T类中的字段值或列名
	 * @param where 条件
	 * @return
	 */
	public Map<String,Object> getMapUncheckDelete(List<String> fieldList,Map<String,?> where){
		SQLMap sqlMap = sqlGenerator.getQuerySql(fieldList, where, 0, 1,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(),new ResultSetForMap(fieldList));
	}
	
	/**
	 * 根据查询条件查询类中的某个字段,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 查询条件
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public List<Map<String,Object>> queryForMapList(List<String> fieldList, Map<String,?> where,int startIndex ,int size){
		SQLMap sqlMap = sqlGenerator.getQuerySql(fieldList, where,startIndex,size,true);
		return this.getJdbcTemplate().queryForList(sqlMap.getSql().toString(),sqlMap.getParamMap());
	}
	
	/**
	 * 根据查询条件查询类中的某个字段,不自动校验删除标记
	 * @param where 查询条件
	 * @param fieldList T类中的字段值或列名
	 * @return
	 */
	public List<Map<String,Object>> queryForMapUncheckDeleteList(List<String> fieldList, Map<String,?> where,int startIndex ,int size){
		SQLMap sqlMap = sqlGenerator.getQuerySql(fieldList, where,startIndex,size,false);
		return this.getJdbcTemplate().queryForList(sqlMap.getSql().toString(),sqlMap.getParamMap());
	}
	
	/**
	 * 根据sql查询出结果返回map对象
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String,Object>> queryForMapList(String sql,Object... args){
		return this.getJdbcTemplate().getJdbcOperations().queryForList(sql, args);
	}
	
	/**
	 * 使用where条件查询出T实体的集合,当where中不传入删除标记的时候,该方法会自动校验删除标记
	 * @param where 查询条件
	 * @return
	 */
	public List<T> queryForEntityList(Map<String,?> where,int startIndex ,int size){
		SQLMap sqlMap = sqlGenerator.getQuerySql(null,where,startIndex,size,true);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(), new RowMapperForModel<T>(clazz));
	}
	
	/**
	 * 使用where条件查询出T实体的集合,不自动校验删除标记
	 * @param where 查询条件
	 * @return
	 */
	public List<T> queryForEntityUncheckDeleteList(Map<String,?> where,int startIndex ,int size){
		SQLMap sqlMap = sqlGenerator.getQuerySql(null,where,startIndex,size,false);
		return this.getJdbcTemplate().query(sqlMap.getSql().toString(),sqlMap.getParamMap(), new RowMapperForModel<T>(clazz));
	}
	
	/**
	 * 根据sql查询出T实体的集合
	 * @param sql
	 * @param params
	 * @return
	 */
	public List<T> queryForEntityList(String sql,Object... params){
		return this.getJdbcTemplate().getJdbcOperations().query(sql, new RowMapperForModel<T>(clazz), params);
	}
	
	/**
	 * 使用sql查询出T实体的集合
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public List<T> queryForEntityListByMap(String sql,Map<String,?> paramMap){
		return this.getJdbcTemplate().query(sql,paramMap,new RowMapperForModel<T>(clazz));
	}
	
	/**
	 * 根据sql查询出一个单一的值
	 * @param sql
	 * @param params
	 * @return
	 */
	public String getValue(String sql,Object... params){
		List<String> queryForList = this.getJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, params);
		if(ValidateUtils.isBlank(queryForList)){
			return null;
		}else{
			return queryForList.get(0);
		}
	}
}
