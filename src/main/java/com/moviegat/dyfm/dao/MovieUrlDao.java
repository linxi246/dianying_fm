package com.moviegat.dyfm.dao;

import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieUrlBean;

public interface MovieUrlDao extends CrudRepository<MovieUrlBean, String> {

}
