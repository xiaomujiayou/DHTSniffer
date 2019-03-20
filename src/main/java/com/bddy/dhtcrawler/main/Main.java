package com.bddy.dhtcrawler.main;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bddy.dhtcrawler.db.ConnectionPool;
import com.bddy.dhtcrawler.db.RedisPool;
import com.bddy.dhtcrawler.listener.OnAnnouncePeerListener;
import com.bddy.dhtcrawler.listener.OnGetPeersListener;
import com.bddy.dhtcrawler.server.DHTServer;
import com.bddy.dhtcrawler.structure.DownloadPeer;
import com.bddy.dhtcrawler.structure.Queue;
import com.bddy.dhtcrawler.task.WireMetadataDownloadTask;
import com.bddy.dhtcrawler.util.BZipUtil;
import com.bddy.dhtcrawler.util.ByteUtil;
import com.bddy.dhtcrawler.util.SHA1Util;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.xm.http.IpUtil;

import redis.clients.jedis.Jedis;
/**
 * 
 * @author xiaomu
 *
 */
public class Main {
	
	public static long count = 0;
	
	public static void main(String[] args) throws Exception {
		
		Jedis jedis = RedisPool.getJedis();
		if (jedis == null) {
			System.out.println("get jedis failed.");
			return;
		}
		DruidDataSource dds = new DruidDataSource();
		dds.setDriverClassName("com.mysql.jdbc.Driver"); 
		dds.setUsername("root");
		dds.setPassword("kxy521yu");
		dds.setUrl("jdbc:mysql://127.0.0.1/dht"); 
		dds.setInitialSize(5);
		dds.setMinIdle(1);
		BlockingQueue<DownloadPeer> dps = new LinkedBlockingQueue<>();
		for (int i = 0; i < 50; i++) {
			Thread t = new WireMetadataDownloadTask(dds, dps);
			t.start();
		}
		DHTServer server = new DHTServer("0.0.0.0", 6882, 88800);
		server.setOnGetPeersListener(new OnGetPeersListener() {
			
			@Override
			public void onGetPeers(InetSocketAddress address, byte[] info_hash) {
			}
		});
		server.setOnAnnouncePeerListener(new OnAnnouncePeerListener() {
			
			@Override
			public void onAnnouncePeer(InetSocketAddress address, byte[] info_hash, int port) {
				if (dps.size() > 10000)
					return;
				if (jedis.getSet(ByteUtil.byteArrayToHex(info_hash), "1") == null) {
					jedis.expire(ByteUtil.byteArrayToHex(info_hash), 60*60*24);
					try {
						dps.put(new DownloadPeer(address.getHostString(), port, info_hash));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else {
					updateHot(dds,new SHA1Util().SHA1(Arrays.copyOfRange(info_hash, 0, info_hash.length)),address.getAddress().getHostAddress());
				}
			}
		});
		server.setDaemon(true);
		server.start();
	}
	
	private static void updateHot(DruidDataSource dds, String infoHash,String address) {
		Connection connectHot = null;
		PreparedStatement stamentHot = null;
		try {
			connectHot = dds.getConnection();
			stamentHot = connectHot.prepareStatement("UPDATE magnet SET hot = hot + 1 WHERE hash = ?");
			stamentHot.setString(1, infoHash);
			stamentHot.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (stamentHot != null) 
					stamentHot.close();
				if (connectHot != null)
					connectHot.close();
				stamentHot = null;
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		addIp(dds,infoHash,address);
	}
	private static void addIp(DruidDataSource dds, String infoHash,String address) {
		Connection connectHot = null;
		PreparedStatement stamentHot = null;
		try {
			connectHot = dds.getConnection();
			stamentHot = connectHot.prepareStatement("insert into magnet_ip (hash,ip)values(?,?)");
			stamentHot.setString(1, infoHash);
			stamentHot.setString(2, address);
			
			stamentHot.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (stamentHot != null) 
					stamentHot.close();
				if (connectHot != null)
					connectHot.close();
				stamentHot = null;
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private static int save(DruidDataSource dds,String hash,String ip,Integer type) {
		DruidPooledConnection conn = null;
		PreparedStatement statement = null;
		int rs = -1;
		try {
			conn = dds.getConnection();
			statement = conn.prepareStatement("insert into magnet(hash,ip,type) values(?,?,?)");
			statement.setString(1, hash.toUpperCase());
			statement.setString(2, ip);
			statement.setInt(3, type);
			rs = statement.executeUpdate();
			statement.close();
		} catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return rs;
	}
}
