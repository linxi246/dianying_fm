package com.moviegat.dyfm.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class MovieCateColl {
	private List<MovieCateInfo> movieCateList;
	private String cateName;

	public String getCateName() {
		return cateName;
	}

	public void setCateName(String cateName) {
		this.cateName = cateName;
	}

	public List<MovieCateInfo> getMovieCateList() {
		return movieCateList;
	}

	public void setMovieCateList(List<MovieCateInfo> movieCateList) {
		this.movieCateList = movieCateList;
	}

	public int passRespUrlTotal(final Integer nextCatePoint) {
		int total = 0;

		for (MovieCateInfo movieCate : movieCateList) {

			Map<String, Integer> passUrlTotal = movieCate.getUrlPageTotal();

			Collection<Integer> pageTotal = passUrlTotal.values();
			Iterables.removeIf(pageTotal, new Predicate<Integer>() {
				@Override
				public boolean apply(Integer input) {
					return input > nextCatePoint;
				}

			});
			total += pageTotal.size();
		}

		return total;
	}

}
