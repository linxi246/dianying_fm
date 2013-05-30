package com.moviegat.dyfm.task.datamoving;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.dao.UrlExecuteStatDao;
import com.moviegat.dyfm.exception.ResourceNotFountException;
import com.moviegat.dyfm.exception.RespUrlException;
import com.moviegat.dyfm.service.htmlparse.MovieParse;
import com.moviegat.dyfm.util.MovieCode;
import com.moviegat.dyfm.util.MovieDoMain;

@Service
public class MovieBodyDise {
	private Logger logger = Logger.getLogger(MovieBodyDise.class);

	@Autowired
	private MovieDao movieDao;
	@Autowired
	private MovieUrlDao movieUrlDao;
	@Autowired
	private UrlExecuteStatDao urlExecuteStatDao;

	@Transactional
	public void dise() throws Exception {
		MovieParse mvoieParse = new MovieParse();
		List<MovieBody> bodyList = null;

		while (!(bodyList = getBody()).isEmpty()) {
			List<MovieBean> movieList = Lists.newArrayList();
			List<String> movieIdList = Lists.newArrayList();
			List<String> movieUrlIdList = Lists.newArrayList();
			StringBuffer idsStr = new StringBuffer("'");
			List<UrlExecuteStatBean> urlExecs = Lists.newArrayList();

			for (MovieBody body : bodyList) {
				String htlm = body.getHtml();
				String dbId = body.getId();
				String url = body.getUrl();
				String type = body.getType();

				try {
					this.checkRespHaveAlertError(htlm);
				} catch (ResourceNotFountException err) {
					String errMsg = err.getMessage();

					UrlExecuteStatBean urlExecuteStat = new UrlExecuteStatBean();
					urlExecuteStat.setDbId(dbId);
					urlExecuteStat.setFialMsg(errMsg);
					urlExecuteStat.setLastExecTm(new Date());
					urlExecuteStat.setUrl(MovieDoMain.MOIVE_MAIN_URL + url);
					urlExecuteStat.setUrlType(type);
					urlExecuteStat.setFialErrCode(MovieCode.SERVICE_ERR
							.toString());

					idsStr.append(body.getId() + "','");

					Long isExist = urlExecuteStatDao.checkIsExist(dbId);

					if (isExist != null && isExist == 0L) {
						urlExecs.add(urlExecuteStat);

						movieUrlIdList.add(dbId);
					}

					logger.info("检查失败 --> " + body.getId());

					continue;
				}

				try {
					MovieBean movie = mvoieParse.parseByResult(htlm);
					movieIdList.add(movie.getDyMovieId());

					Iterable<String> temp = Splitter.on('/').omitEmptyStrings()
							.split(url);

					movie.setDyMovieUrl(Iterables.get(temp, 1));
					movie.setType(StringUtils.trimToEmpty(body.getType()));

					movieList.add(movie);
					movieUrlIdList.add(dbId);
				} catch (Exception err) {
					String errMsg = err.getMessage();

					UrlExecuteStatBean urlExecuteStat = new UrlExecuteStatBean();
					urlExecuteStat.setDbId(dbId);
					urlExecuteStat.setFialMsg(errMsg);
					urlExecuteStat.setLastExecTm(new Date());
					urlExecuteStat.setUrl(MovieDoMain.MOIVE_MAIN_URL + url);
					urlExecuteStat.setUrlType(type);
					urlExecuteStat.setFialErrCode(MovieCode.PARSE_ERR
							.toString());

					Long isExist = urlExecuteStatDao.checkIsExist(dbId);

					if (isExist != null && isExist == 0L) {
						urlExecs.add(urlExecuteStat);
					}

					logger.info("html解析失败 --> " + body.getId());
				} finally {
					idsStr.append(body.getId() + "','");
				}
			}

			try {
				final List<String> existId = movieDao
						.findDyMovieIdById(movieIdList);

				Collection<MovieBean> savingMovieList = Collections2.filter(
						movieList, new Predicate<MovieBean>() {
							@Override
							public boolean apply(MovieBean input) {
								String dyMovieId = input.getDyMovieId();
								return !existId.contains(dyMovieId);
							}
						});

				if (!savingMovieList.isEmpty()) {
					movieDao.save(savingMovieList);
				}

				movieUrlDao.updateIsGatherByIdIn(movieUrlIdList);

				if (!urlExecs.isEmpty()) {
					urlExecuteStatDao.save(urlExecs);
				}

				if (idsStr.length() > 5) {
					this.updateBody(idsStr.toString().substring(0,
							idsStr.toString().length() - 2));
				}
			} catch (Exception e) {
				logger.info("movieIdList --> " + movieIdList);
				logger.info("movieUrlIdList --> " + movieUrlIdList);
				logger.info(e.getMessage());
			}
		}
	}

	private Connection getConn() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection connect = DriverManager
					.getConnection("jdbc:mysql://60.190.98.250:3306/movies?"
							+ "user=root&password=vistech&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
			return connect;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateBody(String idsStr) throws SQLException {
		Connection conn = this.getConn();

		String update = "update dianying_body set sign = 1 where id in ("
				+ idsStr + ")";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(update);

		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt = null;
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

	private List<MovieBody> getBody() throws ClassNotFoundException,
			SQLException {
		Connection conn = this.getConn();

		Statement stmt = conn.createStatement();
		ResultSet result = stmt
				.executeQuery("select * from dianying_body where sign =0 LIMIT 100");

		List<MovieBody> bodyList = Lists.newArrayList();
		while (result.next()) {
			MovieBody movieBody = new MovieBody();

			String id = result.getString(1);
			String html = result.getString(2);
			String url = result.getString(3);
			String type = result.getString(4);

			movieBody.setId(id);
			movieBody.setHtml(html);
			movieBody.setType(type);
			movieBody.setUrl(url);

			bodyList.add(movieBody);
		}
		if (result != null) {
			try {
				result.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = null;
		}

		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt = null;
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			conn = null;
		}

		return bodyList;
	}

	/**
	 * 检查返回内容是否错误
	 * 
	 * @param html
	 * @throws RespUrlException
	 * @throws ResourceNotFountException
	 */
	private void checkRespHaveAlertError(String html) throws IOException {
		Document doc = Jsoup.parse(html);
		Elements htmlEle = doc.select("html");
		String key = null;
		if (!htmlEle.isEmpty()) {
			key = doc.select("html").first().attr("xmlns:wb");
		}
		Elements eles = doc.select("div.alert-error");
		String errMsg = eles.text();

		if (StringUtils.indexOf(errMsg, "影片暂时不可以访问") != -1) { // 链接资源错误
			throw new ResourceNotFountException(errMsg);
		} else if (StringUtils.indexOf(errMsg, "遇到一个错误了") != -1) {
			throw new ResourceNotFountException(errMsg);
		} else {
			if (!eles.isEmpty()) {
				throw new RespUrlException("请求页面结果错误");
			} else if (!"http://open.weibo.com/wb".equals(key)) {
				throw new RespUrlException("代理返回结果错误");
			}
		}
	}
}

class MovieBody {
	String id;
	String html;
	String url;
	String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
