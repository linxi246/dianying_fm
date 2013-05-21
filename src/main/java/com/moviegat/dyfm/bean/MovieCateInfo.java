package com.moviegat.dyfm.bean;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 电影类型
 * 
 * @author XX
 * 
 */
public class MovieCateInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String id;
	private Map<String,Integer> urlPageTotal = Maps.newHashMap();
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Integer> getUrlPageTotal() {
		return urlPageTotal;
	}

	public void setUrlPageTotal(String passUrl,Integer total) {
		this.urlPageTotal.put(passUrl, total);
	}
	
}
