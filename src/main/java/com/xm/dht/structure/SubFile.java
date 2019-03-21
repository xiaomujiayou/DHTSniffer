package com.xm.dht.structure;

/**
 * 多文件列表的子文件结构
 * @author xiaomu
 * @version 
 */
public class SubFile {

	private Long length;
	private String path;

	public SubFile(Long length, String path) {
		super();
		this.length = length;
		this.path = path;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
