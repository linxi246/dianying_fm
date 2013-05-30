package com.moviegat.dyfm.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieUrlBean;

public interface MovieUrlDao extends CrudRepository<MovieUrlBean, String> {

	Page<MovieUrlBean> findByIsGather(Boolean isGather, Pageable pageable);

	@Query("SELECT COUNT(M.id) FROM MovieUrlBean M WHERE M.isGather = false")
	Long findIsGatherSize();

	@Query("select M.url from MovieUrlBean M group by url having count(M.url)>1")
	List<String> query1();

	@Query("select SUBSTRING(url,8,LENGTH(SUBSTRING(url,8))-1) from MovieUrlBean where isGather=1")
	List<String> query2();

	@Modifying
	@Query("update MovieUrlBean a set a.isGather = 1 where a.id in ?1")
	void updateIsGatherByIdIn(List<String> ids);

	List<MovieUrlBean> findByUrl(String url);

	List<MovieUrlBean> findByUrlIn(List<String> urls);
}