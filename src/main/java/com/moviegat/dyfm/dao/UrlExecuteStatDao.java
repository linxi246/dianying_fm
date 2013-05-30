package com.moviegat.dyfm.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;

public interface UrlExecuteStatDao extends
		CrudRepository<UrlExecuteStatBean, String> {
	
	@Query("select count(M.dbId) from UrlExecuteStatBean M where M.dbId=?1")
	Long checkIsExist(String dbId);
}
