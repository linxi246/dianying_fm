package com.moviegat.dyfm.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieCateStatBean;

public interface MovieCateStatDao extends
		CrudRepository<MovieCateStatBean, String> {
	
	List<MovieCateStatBean> findByCateType(String cateType);
	
}
