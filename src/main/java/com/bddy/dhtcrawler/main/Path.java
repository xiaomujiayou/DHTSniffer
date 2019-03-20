package com.bddy.dhtcrawler.main;

import com.alibaba.druid.pool.DruidDataSource;

public class Path {
	public static void main(String[] args) throws Exception{
		DruidDataSource dds = new DruidDataSource();
		dds.setDriverClassName("com.mysql.jdbc.Driver"); 
		dds.setUsername("xiaomujiayou");
		dds.setPassword("xm123456");
		dds.setUrl("jdbc:mysql://106.15.230.101/dht"); 
		dds.setInitialSize(5);
		dds.setMinIdle(1);
		
		System.out.println(dds.getConnection());
//		System.out.println(Main.existInfoHash(dds, "441f79c4fe8734a02e81a0ce5c586ffc75ee9ddc"));
	}
}
