package com.moviegat.dyfm.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

/**
 * 记录Url执行状态的类
 * 
 * @author XX
 * 
 */
public class UrlExecuteStatBean implements Serializable {

	private static final long serialVersionUID = 6629520874855780134L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;

	@Column(name = "url", nullable = true, length = 200)
	private String url;

	@Column(name = "exec_num", nullable = true)
	private Integer execNum;

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

	public Integer getExecNum() {
		return execNum;
	}

	public void setExecNum(Integer execNum) {
		this.execNum = execNum;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((execNum == null) ? 0 : execNum.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lastExecTm == null) ? 0 : lastExecTm.hashCode());
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
		return other == this ? true : other.getUrl().equals(url);
	}
}
