package com.yuaiwan.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.CollectionUtils;

/**
 * 读/写动态选择数据库实现
 * 目前实现功能
 *   一写库多读库选择功能，请参考
 *   默认按顺序轮询使用读库
 *   默认选择写库
 *   已实现：一写多读、当写时默认读操作到写库、当写时强制读操作到读库
 *   TODO 读库故障转移
 * @author guangyu 
 *
 */
public class DynamicDataSource extends AbstractDataSource implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DynamicDataSource.class);
    //写数据源
    private DataSource writeDataSource;
    //多个读数据源
    private Map<String, DataSource> readDataSourceMap;
    private String[] readDataSourceNames;
    private DataSource[] readDataSources;
    private int readDataSourceCount;

    private AtomicInteger counter = new AtomicInteger(0);

    /**
     * 设置读库（name, DataSource）
     * @param readDataSourceMap
     */
    public void setReadDataSourceMap(Map<String, DataSource> readDataSourceMap) {
        this.readDataSourceMap = readDataSourceMap;
    }
    public void setWriteDataSource(DataSource writeDataSource) {
        this.writeDataSource = writeDataSource;
    }

    /**
     * 读取完配置文件后初始化数据源
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if(writeDataSource == null) {
            throw new IllegalArgumentException("property 'writeDataSource' is required");
        }
        if(CollectionUtils.isEmpty(readDataSourceMap)) {
            throw new IllegalArgumentException("property 'readDataSourceMap' is required");
        }
        readDataSourceCount = readDataSourceMap.size();
        readDataSources = new DataSource[readDataSourceCount];
        readDataSourceNames = new String[readDataSourceCount];
        int i = 0;
        for(Entry<String, DataSource> e : readDataSourceMap.entrySet()) {
        	readDataSourceNames[i] = e.getKey();
            readDataSources[i] = new MyDataSource(e.getValue(),true);
            i++;
        }
    }
    
    /**
     * 判断是否选择写库
     * @return
     */
    private boolean choiceWrite(){
    	if(DynamicDataSourceDecision.isChoiceWrite()) {
            log.debug("current determine write datasource");
            return true;
        }
        if(DynamicDataSourceDecision.isChoiceNone()) {
            log.debug("no choice read/write, default determine write datasource");
            return true;
        } 
        return false;
    }
    
    private DataSource determineDataSource() {
        if(choiceWrite()) {
            return writeDataSource;
        }else{
        	return determineReadDataSource();
        }
    }
    
    private DataSource determineReadDataSource() {
        //按照顺序选择读库 
        int index = counter.incrementAndGet() % readDataSourceCount;
        if(index == 0) {
        	counter.set(0);//防止counter过大,重置
        }
        log.debug("current determine read datasource : {}", readDataSourceNames[index]);
        return readDataSources[index];
    }
    
    @Override
    public Connection getConnection() throws SQLException {
    	return determineDataSource().getConnection();
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }

}
