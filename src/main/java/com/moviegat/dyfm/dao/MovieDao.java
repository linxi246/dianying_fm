package com.moviegat.dyfm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.moviegat.dyfm.bean.db.MovieBean;

public interface MovieDao extends CrudRepository<MovieBean, Integer> {

	@Query("select M.dyMovieId from MovieBean M group by dyMovieId having count(M.dyMovieId)>1")
	List<String> findReportMovieId();

	@Query("select count(M.dyMovieId) from MovieBean M where M.dyMovieId=?1")
	Long checkIsExist(String dyMovieId);

	List<MovieBean> findByDyMovieId(String dyMovieId);

	List<MovieBean> findByDyMovieUrlNotIn(List<String> dyMovieUrl);

	@Query("SELECT M.dyMovieId from MovieBean M where M.dyMovieId in ?1")
	List<String> findDyMovieIdById(List<String> movieIds);
}
