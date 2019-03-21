package com.xm.dht.db;

import com.alibaba.druid.pool.DruidDataSource;

public class DataSourcePool {
	private static DruidDataSource druidDataSource;
	
	public static synchronized DruidDataSource getDataSource() {
		if(druidDataSource == null) {
			druidDataSource = new DruidDataSource();
			druidDataSource.setDriverClassName("com.mysql.jdbc.Driver"); 
			druidDataSource.setUsername("root");
			druidDataSource.setPassword("");
			druidDataSource.setUrl("jdbc:mysql://127.0.0.1/dht"); 
			druidDataSource.setInitialSize(5);
			druidDataSource.setMinIdle(1);
		} 
		return druidDataSource;
	}
}
