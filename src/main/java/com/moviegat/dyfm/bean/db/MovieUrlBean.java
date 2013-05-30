package com.moviegat.dyfm.bean.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "dianying_movie_url")
public class MovieUrlBean extends MovieBasic implements Serializable {
	private static final long serialVersionUID = 5152526987387909942L;

	@Column(name = "douban")
	private Double douban;
	@Column(name = "imdb")
	private Double imdb;
	@Column(name = "year")
	private Integer year;
	@Column(name = "type")
	private String type;
	private Boolean isGather;
	@Column(name = "tm")
	private Date tm;
	
	@Column(name = "url", length = 100, nullable = true)
	public String getUrl() {
		return url;
	}

	public Date getTm() {
		return tm;
	}

	public void setTm(Date tm) {
		this.tm = tm;
	}

	public Double getDouban() {
		return douban;
	}

	public void setDouban(Double douban) {
		this.douban = douban;
	}

	public Double getImdb() {
		return imdb;
	}

	public void setImdb(Double imdb) {
		this.imdb = imdb;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Column(name = "is_gather")
	public Boolean getIsGather() {
		return isGather;
	}

	public void setIsGather(Boolean isGather) {
		this.isGather = isGather;
	}

	@PrePersist
	public void prePersist() {
		tm = new Date();
	}

	@Override
	public String toString() {
		return "MovieUrlBean [id=" + id + ", url=" + url + ", year=" + year
				+ ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		MovieUrlBean other = (MovieUrlBean) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
