package com.moviegat.dyfm.task.proxychecing;

public class CheckingProxy {
	private String ip;
	private Integer port;
	private ProxyState stat;
	private ProxyForm form;
	private String dbId;

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

	public ProxyState getStat() {
		return stat;
	}

	public void setStat(ProxyState stat) {
		this.stat = stat;
	}

	public ProxyForm getForm() {
		return form;
	}

	public void setForm(ProxyForm form) {
		this.form = form;
	}

	public String getDbId() {
		return dbId;
	}

	public void setDbId(String dbId) {
		this.dbId = dbId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		CheckingProxy other = (CheckingProxy) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "CheckingProxy [ip=" + ip + ", port=" + port + ", stat=" + stat
				+ ", form=" + form + ", dbId=" + dbId + "]";
	}
}

enum ProxyForm {
	DB, WEB
}

enum ProxyState {
	SAVE, DEL
}