package com.moviegat.dyfm.bean.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 电影 class
 * 
 * @author XX
 * 
 */
@Entity
@Table(name = "dianying_movie")
public class MovieBean implements Serializable {
	private static final long serialVersionUID = 3452087598535388128L;

	@Id
	@GeneratedValue(generator = "ud")
	@GenericGenerator(name = "ud", strategy = "uuid")
	@Column(name = "id", length = 32, nullable = true)
	private String id;

	@Column(name = "dy_movie_id")
	private String dyMovieId;

	@Column(name = "dy_movie_url")
	private String dyMovieUrl;

	@Column(name = "dy_poster_url")
	private String dyPosterUrl;

	@Column(name = "type")
	private String type;

	@Column(name = "poster_url")
	private String posterUrl;

	@Column(name = "ch_name")
	private String chName;

	@Column(name = "en_name")
	private String enName;

	@Column(name = "year")
	private Integer year;

	@Column(name = "directors")
	private String directors;

	@Column(name = "starrings")
	private String starrings;

	@Column(name = "genres")
	private String genres;

	@Column(name = "regions")
	private String regions;

	@Column(name = "show_tms")
	private String showTms;

	@Column(name = "alias")
	private String alias;

	@Column(name = "file_lens")
	private String fileLens;

	@Column(name = "douban_url")
	private String doubanUrl;

	@Column(name = "grade_douban")
	private Double gradeDouban;

	@Column(name = "imdb_url")
	private String imdbUrl;

	@Column(name = "grade_imdb")
	private Double gradeImdb;

	@Column(name = "best_reso_url")
	private String bestResoUrl;

	@Column(name = "prevue_reso_url")
	private String prevueResoUrl;

	@Column(name = "play_url_list")
	private String playUrlList;

	@Column(name = "plot_summary")
	private String plotSummary;

	@Column(name = "movie_reso")
	private String movieReso;

	@Column(name = "have_reso")
	private Boolean haveReso;

	@Column(name = "last_gather_tm")
	private Date lastGatherTm;

	@Column(name = "gather_num")
	private Integer gatherNum;

	@Column(name = "reso_num")
	private Integer resoNum;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDyMovieId() {
		return dyMovieId;
	}

	public void setDyMovieId(String dyMovieId) {
		this.dyMovieId = dyMovieId;
	}

	public String getDyMovieUrl() {
		return dyMovieUrl;
	}

	public void setDyMovieUrl(String dyMovieUrl) {
		this.dyMovieUrl = dyMovieUrl;
	}

	public String getDyPosterUrl() {
		return dyPosterUrl;
	}

	public void setDyPosterUrl(String dyPosterUrl) {
		this.dyPosterUrl = dyPosterUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}

	public String getChName() {
		return chName;
	}

	public void setChName(String chName) {
		this.chName = chName;
	}

	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getDirectors() {
		return directors;
	}

	public void setDirectors(String directors) {
		this.directors = directors;
	}

	public String getStarrings() {
		return starrings;
	}

	public void setStarrings(String starrings) {
		this.starrings = starrings;
	}

	public String getGenres() {
		return genres;
	}

	public void setGenres(String genres) {
		this.genres = genres;
	}

	public String getRegions() {
		return regions;
	}

	public void setRegions(String regions) {
		this.regions = regions;
	}

	public String getShowTms() {
		return showTms;
	}

	public void setShowTms(String showTms) {
		this.showTms = showTms;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getFileLens() {
		return fileLens;
	}

	public void setFileLens(String fileLens) {
		this.fileLens = fileLens;
	}

	public Double getGradeDouban() {
		return gradeDouban;
	}

	public void setGradeDouban(Double gradeDouban) {
		this.gradeDouban = gradeDouban;
	}

	public Double getGradeImdb() {
		return gradeImdb;
	}

	public void setGradeImdb(Double gradeImdb) {
		this.gradeImdb = gradeImdb;
	}

	public String getBestResoUrl() {
		return bestResoUrl;
	}

	public void setBestResoUrl(String bestResoUrl) {
		this.bestResoUrl = bestResoUrl;
	}

	public String getPrevueResoUrl() {
		return prevueResoUrl;
	}

	public void setPrevueResoUrl(String prevueResoUrl) {
		this.prevueResoUrl = prevueResoUrl;
	}

	public String getPlayUrlList() {
		return playUrlList;
	}

	public void setPlayUrlList(String playUrlList) {
		this.playUrlList = playUrlList;
	}

	public String getPlotSummary() {
		return plotSummary;
	}

	public void setPlotSummary(String plotSummary) {
		this.plotSummary = plotSummary;
	}

	public String getMovieReso() {
		return movieReso;
	}

	public void setMovieReso(String movieReso) {
		this.movieReso = movieReso;
	}

	public Boolean getHaveReso() {
		return haveReso;
	}

	public void setHaveReso(Boolean haveReso) {
		this.haveReso = haveReso;
	}

	public Date getLastGatherTm() {
		return lastGatherTm;
	}

	public void setLastGatherTm(Date lastGatherTm) {
		this.lastGatherTm = lastGatherTm;
	}

	public Integer getGatherNum() {
		return gatherNum;
	}

	public void setGatherNum(Integer gatherNum) {
		this.gatherNum = gatherNum;
	}

	public String getDoubanUrl() {
		return doubanUrl;
	}

	public void setDoubanUrl(String doubanUrl) {
		this.doubanUrl = doubanUrl;
	}

	public String getImdbUrl() {
		return imdbUrl;
	}

	public void setImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
	}

	public Integer getResoNum() {
		return resoNum;
	}

	public void setResoNum(Integer resoNum) {
		this.resoNum = resoNum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chName == null) ? 0 : chName.hashCode());
		result = prime * result
				+ ((dyMovieId == null) ? 0 : dyMovieId.hashCode());
		result = prime * result
				+ ((dyMovieUrl == null) ? 0 : dyMovieUrl.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MovieBean other = (MovieBean) obj;
		if (chName == null) {
			if (other.chName != null)
				return false;
		} else if (!chName.equals(other.chName))
			return false;
		if (dyMovieId == null) {
			if (other.dyMovieId != null)
				return false;
		} else if (!dyMovieId.equals(other.dyMovieId))
			return false;
		if (dyMovieUrl == null) {
			if (other.dyMovieUrl != null)
				return false;
		} else if (!dyMovieUrl.equals(other.dyMovieUrl))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MovieBean [id=" + id + ", dyMovieUrl=" + dyMovieUrl + ", type="
				+ type + ", chName=" + chName + ", enName=" + enName
				+ ", year=" + year + ", gradeDouban=" + gradeDouban
				+ ", gradeImdb=" + gradeImdb + ", gatherNum=" + gatherNum
				+ ",resoNum=" + resoNum + "]";
	}

	@PrePersist
	public void pre() {
		lastGatherTm = new Date();
	}
}
