package com.moviegat.dyfm.bean.db;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public class MovieBasic {
	protected String id;
	protected String url;
	
	public MovieBasic() {
		super();
	}

	public MovieBasic(String url) {
		super();
		this.url = url;
	}

	public MovieBasic(String id, String url) {
		super();
		this.id = id;
		this.url = url;
	}
	
	@Transient
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "MovieBasic [url=" + url + "]";
	}
}
