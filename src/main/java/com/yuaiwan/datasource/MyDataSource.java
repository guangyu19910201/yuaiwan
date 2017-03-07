package com.yuaiwan.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * 由于spring的org.springframework.jdbc.datasource.DataSourceTransactionManager不支持read-only参数,自定义数据源
 * @author guangyu
 */
public class MyDataSource extends AbstractDataSource implements InitializingBean{
	//数据源
    private DataSource dataSource;
    private boolean readOnly = false;
    
    public MyDataSource(DataSource dataSource) {
    	this.dataSource = dataSource;
	}
    
    public MyDataSource(DataSource dataSource,boolean readOnly) {
		this.dataSource = dataSource;
		this.readOnly = readOnly;
	}
    
	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setReadOnly(readOnly);
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password)throws SQLException {
		Connection connection = dataSource.getConnection(username, password);
		connection.setReadOnly(readOnly);
		return connection;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(dataSource == null){
			throw new IllegalArgumentException("property 'dataSource' is required");
		}
	}
}
