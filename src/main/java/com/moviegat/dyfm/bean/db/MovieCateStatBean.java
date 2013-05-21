package com.moviegat.dyfm.bean.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dianying_movie_cates_stat", uniqueConstraints = { @UniqueConstraint(columnNames = { "cate_url" }) })
public class MovieCateStatBean implements Serializable {
	private static final long serialVersionUID = 6791159050904033157L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;

	@Column(name = "cate_url", length = 100, unique = true, nullable = true)
	private String cateUrl;

	@Column(name = "cate_type", length = 20, nullable = true)
	private String cateType;

	@Column(name = "page_total", nullable = true)
	private int pageTotal;

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

	public String getCateType() {
		return cateType;
	}

	public void setCateType(String cateType) {
		this.cateType = cateType;
	}

	public int getPageTotal() {
		return pageTotal;
	}

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cateType == null) ? 0 : cateType.hashCode());
		result = prime * result + ((cateUrl == null) ? 0 : cateUrl.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + pageTotal;
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
		MovieCateStatBean other = (MovieCateStatBean) obj;

		return other == this ? true : other.getCateUrl().equals(cateUrl);
	}

}
