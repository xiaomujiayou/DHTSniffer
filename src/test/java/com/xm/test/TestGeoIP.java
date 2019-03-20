package com.xm.test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.alibaba.fastjson.JSON;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.xm.http.IpUtil;

public class TestGeoIP {
	public static void main(String[] args) {
//		try {
//			LookupService service = new LookupService(new File("C:\\Users\\xiaomu\\Desktop\\GeoIP.dat"));
//			
//			System.out.println(JSON.toJSONString(service.getLocation(InetAddress.getByName("223.150.242.9")).));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		while (true) {
			long a = System.currentTimeMillis();
			System.out.println(IpUtil.query("223.150.242.9"));
			System.out.println(System.currentTimeMillis() - a);
			
		}
		
		
	}
}
