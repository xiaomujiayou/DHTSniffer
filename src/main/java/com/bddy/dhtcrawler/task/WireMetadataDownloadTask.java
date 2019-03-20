package com.bddy.dhtcrawler.task;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.bddy.dhtcrawler.db.ConnectionPool;
import com.bddy.dhtcrawler.handler.AnnouncePeerInfoHashWireHandler;
import com.bddy.dhtcrawler.listener.OnMetadataListener;
import com.bddy.dhtcrawler.structure.DownloadPeer;
import com.bddy.dhtcrawler.structure.Torrent;
import com.bddy.dhtcrawler.util.BZipUtil;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class WireMetadataDownloadTask extends Thread{
	
	private AnnouncePeerInfoHashWireHandler handler = new AnnouncePeerInfoHashWireHandler();
	
	private ConnectionPool connPool;
	private DruidDataSource druidDataSource;
	private BlockingQueue<DownloadPeer> dps;
	
	public WireMetadataDownloadTask(ConnectionPool connPool, BlockingQueue<DownloadPeer> dps) {
		super();
		this.connPool = connPool;
		this.dps = dps;
		initHandler();
	}
	public WireMetadataDownloadTask(DruidDataSource connPool, BlockingQueue<DownloadPeer> dps) {
		super();
		this.druidDataSource = connPool;
		this.dps = dps;
		initHandler();
	}

	@Override
	public void run() {
		
		while (true) {
			//System.out.println("current work thread: " + tid + ", dps size:" + dps.size());
			try {
				DownloadPeer peer = dps.take();
				handler.handler(new InetSocketAddress(peer.getIp(), peer.getPort()), peer.getInfo_hash());
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		
	}
	
	private void initHandler() {
		handler.setOnMetadataListener(new OnMetadataListener() {
			@Override
			public void onMetadata(Torrent torrent,String ip) {
//				System.out.println(JSON.toJSONString(torrent));
				if (torrent == null || torrent.getInfo() == null)
					return;
				//入库操作
				System.out.println("finished,dps size:" + dps.size());
				Connection conn = null;
				PreparedStatement stament = null;
				try {
					conn = druidDataSource.getConnection();
					stament = conn.prepareStatement("insert into magnet(hash,name,content,length,create_time,ip) values(?,?,?,?,?,?)");
					stament.setString(1, torrent.getInfo_hash());
					stament.setString(2, torrent.getInfo().getName());
					stament.setString(3, JSON.toJSONString(torrent.getInfo().getFiles()));
					Long length = torrent.getInfo().getLength();
					stament.setLong(4, length>0?(length/1024):length);
					stament.setTimestamp(5, new Timestamp(torrent.getCreationDate().getTime()));
					stament.setString(6, ip+"|");
					int i = stament.executeUpdate();
//					if (i != 1)
//						//System.out.println("insert info_hash[" + torrent.getInfo_hash() +"] failed.");
//					stament.close();
				} catch(MySQLIntegrityConstraintViolationException e) {
					System.out.println("hash已存在");
				} catch (SQLException e) {
					e.printStackTrace();
					if (stament != null) {
						try {
							stament.close();
							stament = null;
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					
				} finally {
					if (conn != null) {
//						druidDataSource.returnConnection(conn);
						try {
							conn.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
		});
	}
	
}
