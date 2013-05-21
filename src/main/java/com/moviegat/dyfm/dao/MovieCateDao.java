package com.moviegat.dyfm.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieCateBean;

public interface MovieCateDao extends CrudRepository<MovieCateBean, String> {
	
	
	public List<MovieCateBean> findByCateType(String cateType);
}
