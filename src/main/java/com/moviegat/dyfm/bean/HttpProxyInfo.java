package com.moviegat.dyfm.bean;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpProxyInfo {
	private String dbId;
	private String ip;
	private Integer port;
	private AtomicInteger execTotal;
	private ProxyType proxyType;
	private Integer callTotal;

	public HttpProxyInfo(Integer initExecTotal) {
		execTotal = new AtomicInteger(initExecTotal);
	}

	public HttpProxyInfo(String ip, Integer port, Integer initExecTotal) {
		this(initExecTotal);
		this.ip = ip;
		this.port = port;
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

	public ProxyType getProxyType() {
		return proxyType;
	}

	public void setProxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
	}

	public String getDbId() {
		return dbId;
	}

	public void setDbId(String dbId) {
		this.dbId = dbId;
	}

	public Integer addExecTotal() {
		return execTotal.incrementAndGet();
	}

	public Integer getExecTotal() {
		return execTotal.get();
	}

	public Integer getCallTotal() {
		return callTotal;
	}

	public void setCallTotal(Integer callTotal) {
		this.callTotal = callTotal;
	}

	@Override
	public String toString() {
		return "HttpProxyInfo [ip=" + ip + ", port=" + port + ", execTotal="
				+ execTotal.get() + ", proxyType=" + proxyType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result
				+ ((proxyType == null) ? 0 : proxyType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpProxyInfo other = (HttpProxyInfo) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (proxyType == null) {
			if (other.proxyType != null)
				return false;
		} else if (!proxyType.equals(other.proxyType))
			return false;
		return true;
	}
}
