package com.moviegat.dyfm.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.ProxyBean;

public interface ProxyDao extends CrudRepository<ProxyBean, String> {
	
	List<ProxyBean> findByLastusetmLessThan(Sort sort);
	
	@Query("SELECT MIN(P.lastusetm) FROM ProxyBean P")
	Date findMinLastusetm();
}
