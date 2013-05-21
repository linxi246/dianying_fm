package com.moviegat.dyfm.bean.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dianying_proxy")
public class ProxyBean implements Serializable {
	private static final long serialVersionUID = 8622672757348061183L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;
	@Column(name = "ip", length = 20, nullable = true)
	private String ip;
	@Column(name = "port", nullable = true)
	private Integer port;
	@Column(name = "exec_total", nullable = true)
	private Integer execTotal;
	@Column(name = "call_total", nullable = true)
	private Integer callTotal;
	@Column(name = "insert_tm", nullable = true)
	private Date insertTm;
	@Column(name = "last_use_tm", nullable = true)
	private Date lastusetm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Date getLastusetm() {
		return lastusetm;
	}

	public void setLastusetm(Date lastusetm) {
		this.lastusetm = lastusetm;
	}

	public Date getInsertTm() {
		return insertTm;
	}

	public void setInsertTm(Date insertTm) {
		this.insertTm = insertTm;
	}

	public Integer getExecTotal() {
		return execTotal;
	}

	public void setExecTotal(Integer execTotal) {
		this.execTotal = execTotal;
	}

	public Integer getCallTotal() {
		return callTotal;
	}

	public void setCallTotal(Integer callTotal) {
		this.callTotal = callTotal;
	}

	@PreUpdate
	public void preUpdate() {
		lastusetm = new Date();
	}

	@PrePersist
	public void prePersist() {
		insertTm = new Date();
		lastusetm = new Date();
	}
}
