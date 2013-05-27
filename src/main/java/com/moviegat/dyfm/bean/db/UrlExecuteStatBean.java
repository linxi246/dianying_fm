package com.moviegat.dyfm.bean.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 链接执行状态
 * 
 * @author XX
 * 
 */
@Entity
@Table(name = "url_exec_stat")
public class UrlExecuteStatBean implements Serializable {

	private static final long serialVersionUID = 6629520874855780134L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;

	@Column(name = "db_id", length = 32, nullable = true)
	private String dbId;

	@Column(name = "url", nullable = true, length = 200)
	private String url;

	@Column(name = "fial_msg", nullable = true)
	private String fialMsg;

	@Column(name = "url_type", nullable = true, length = 10)
	private String urlType;

	@Column(name = "last_exec_tm", nullable = true)
	private Date lastExecTm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFialMsg() {
		return fialMsg;
	}

	public void setFialMsg(String fialMsg) {
		this.fialMsg = fialMsg;
	}

	public String getUrlType() {
		return urlType;
	}

	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}

	public Date getLastExecTm() {
		return lastExecTm;
	}

	public void setLastExecTm(Date lastExecTm) {
		this.lastExecTm = lastExecTm;
	}

	public String getDbId() {
		return dbId;
	}

	public void setDbId(String dbId) {
		this.dbId = dbId;
	}

	@Override
	public String toString() {
		return "UrlExecuteStatBean [url=" + url + ", fialMsg=" + fialMsg
				+ ", urlType=" + urlType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((urlType == null) ? 0 : urlType.hashCode());
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
		UrlExecuteStatBean other = (UrlExecuteStatBean) obj;
		if (dbId == null) {
			if (other.dbId != null)
				return false;
		} else if (!dbId.equals(other.dbId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (urlType == null) {
			if (other.urlType != null)
				return false;
		} else if (!urlType.equals(other.urlType))
			return false;
		return true;
	}
}
