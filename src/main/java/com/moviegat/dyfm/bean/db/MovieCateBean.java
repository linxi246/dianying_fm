package com.moviegat.dyfm.bean.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dianying_movie_cates")
public class MovieCateBean implements Serializable {

	private static final long serialVersionUID = 1411606883873934523L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;
	@Column(name = "cate_type", length = 10, nullable = true)
	private String cateType;
	@Column(name = "cate_url", length = 50, nullable = true)
	private String cateUrl;
	@Column(name = "total_page_num")
	private int totalPageNum;
//	@Column(name = "exec_page_coll", length = 1000)
//	private String execPageColl;
	@Column(name = "is_read")
	private Boolean isRead;
	@Column(name = "insert_tm")
	private Date insertTm;
	@Column(name = "read_tm")
	private Date readTm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCateUrl() {
		return cateUrl;
	}

	public void setCateUrl(String cateUrl) {
		this.cateUrl = cateUrl;
	}

	public int getTotalPageNum() {
		return totalPageNum;
	}

	public void setTotalPageNum(int totalPageNum) {
		this.totalPageNum = totalPageNum;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Date getInsertTm() {
		return insertTm;
	}

	public void setInsertTm(Date insertTm) {
		this.insertTm = insertTm;
	}

	public Date getReadTm() {
		return readTm;
	}

	public void setReadTm(Date readTm) {
		this.readTm = readTm;
	}

	public String getCateType() {
		return cateType;
	}

	public void setCateType(String cateType) {
		this.cateType = cateType;
	}

//	public String getExecPageColl() {
//		return execPageColl;
//	}
//
//	public void setExecPageColl(String execPageColl) {
//		this.execPageColl = execPageColl;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		result = prime * result
				+ ((cateType == null) ? 0 : cateType.hashCode());
		result = prime * result + ((cateUrl == null) ? 0 : cateUrl.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((insertTm == null) ? 0 : insertTm.hashCode());
		result = prime * result + ((isRead == null) ? 0 : isRead.hashCode());
		result = prime * result + ((readTm == null) ? 0 : readTm.hashCode());
		result = prime * result + totalPageNum;
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
		MovieCateBean other = (MovieCateBean) obj;
		return other == this ? true : other.getCateUrl().equals(cateUrl);
	}

}
