package com.moviegat.dyfm.dao;

import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;

public interface UrlExecuteStatDao extends
		CrudRepository<UrlExecuteStatBean, String> {

}
