package com.moviegat.dyfm.dao;

import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieBean;

public interface MovieDao extends CrudRepository<MovieBean, Integer> {

}
