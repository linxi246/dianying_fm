package com.moviegat.dyfm.dao;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.ProxyBean;

public interface ProxyDao extends CrudRepository<ProxyBean, String> {
	
	Page<ProxyBean> findAll(Pageable pageable);
	
	@Query("SELECT MIN(P.lastusetm) FROM ProxyBean P")
	Date findMinLastusetm();
}
