package com.moviegat.dyfm.service.httpclient;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moviegat.dyfm.bean.MovieCateColl;
import com.moviegat.dyfm.bean.MovieCateInfo;
import com.moviegat.dyfm.bean.UrlExecuteStatBean;
import com.moviegat.dyfm.bean.db.MovieCateBean;
import com.moviegat.dyfm.bean.db.MovieCateStatBean;
import com.moviegat.dyfm.core.ExecuteUrlResp;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieCateStatDao;
import com.moviegat.dyfm.exception.RespUrlException;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieCateParse;
import com.moviegat.dyfm.service.htmlparse.MovieCountPageParse;
import com.moviegat.dyfm.util.MovieDoMain;

/**
 * 分发URL请求
 * 
 * @author XX
 * 
 */
public class MovieCateDispense {

	private Logger logger = Logger.getLogger(MovieCateDispense.class);

	/**
	 * 获得电影类型
	 * 
	 * @return
	 * @throws Exception
	 * @throws RespUrlException
	 */
	public List<MovieCateColl> doMovieCate() throws Exception {
		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		String url = MovieDoMain.MOIVE_CATE_URL;
		String result = null;
		try {
			result = ExecuteUrlResp.getUrlRespByGet(httpClient, url);
		} catch (IOException roe) {
			throw roe;
		}

		IMovieParse<List<MovieCateColl>> movieParse = new MovieCateParse();
		List<MovieCateColl> movieCateCollList = movieParse.parseByResult(
				result, url);

		return movieCateCollList;
	}

	/**
	 * 通过电影类型组合查询获得总页数
	 * 
	 * @param movieCateStatMap
	 *            记录电影的状态
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void buildMovieCateSearGetTotalPage(
			MovieCateStatDao movieCateStatDao, MovieCateDao movieCateDao,
			IPDyncDraw ipDynDraw) throws Exception {
		// 当页数小于此页数时，即无需进入下一组的类型匹配
		final Integer nextCatePoint = 50;

		char cateLink = '-';

		List<MovieCateColl> movieCateCollList = doMovieCate();

		int loopSize = movieCateCollList.size();
		int loopNum = 1;

		while (true) {
			String cateType = null;

			MovieCateColl lastMovieCateColl = movieCateCollList
					.get(loopNum - 1);

			List<String> respUrlList = Lists.newArrayList();

			for (int begin = 0; begin < loopNum; begin++) { // 构造请求url页面
				List<MovieCateInfo> movieCateList = movieCateCollList
						.get(begin).getMovieCateList();

				// 获得电影类型
				if (cateType == null) {
					cateType = lastMovieCateColl.getCateName();
				}

				movieCateList = Preconditions.checkNotNull(movieCateList);

				if (begin == 0) {
					for (MovieCateInfo movieCate : movieCateList) {
						String movieId = movieCate.getId();
						Map<String, Integer> urlPageTotal = movieCate
								.getUrlPageTotal();

						if (urlPageTotal.isEmpty()) {
							respUrlList.add(movieId);
						} else {
							// 过滤小于 50 页的url
							Integer pageTotal = urlPageTotal.get(movieId);
							if (pageTotal > nextCatePoint) {
								respUrlList.add(movieId);
							}
						}
					}
				} else {
					List<String> tempRespUrlList = Lists.newArrayList();
					for (String respUrl : respUrlList) {
						for (MovieCateInfo movieCate : movieCateList) {
							String movieId = movieCate.getId();

							String tempRespUrl = respUrl + cateLink + movieId;

							Map<String, Integer> passUrlTotal = movieCate
									.getUrlPageTotal();

							if (!passUrlTotal.isEmpty()) {
								// 过滤小于 50 页的url
								if (passUrlTotal.get(tempRespUrl) > nextCatePoint) {
									tempRespUrlList.add(tempRespUrl);
								}
							} else {
								tempRespUrlList.add(tempRespUrl);
							}
						}
					}
					respUrlList = tempRespUrlList;
				}
			}

			int respGroupLoop = 0;
			int requestGroupTotal = 100;

			for (;;) { // 分组进行url请求，每组 100 个url，小于 100的不处理
				respGroupLoop++;

				List<String> respUrlGroup = null;
				int respUrlSize = respUrlList.size();

				boolean isBreak = false;
				if (respUrlSize <= requestGroupTotal) {
					respUrlGroup = respUrlList;
					isBreak = true;
				} else {
					respUrlGroup = Lists.newArrayList(respUrlList.subList(0,
							requestGroupTotal));
					respUrlList.removeAll(respUrlGroup);
				}

				List<MovieCateStatBean> haveMovieCateStat = this
						.getMovieCateStateByDB(movieCateStatDao, cateType);

				Map<Integer, Integer> existIndexPageTotal = null;
				List<String> tempRespUrl = Lists.newArrayList(respUrlGroup);

				if (haveMovieCateStat.isEmpty())
					existIndexPageTotal = Maps.newHashMap();
				else
					existIndexPageTotal = this.filterExistMovieCateStat(
							tempRespUrl, haveMovieCateStat);

				// 构造真实的url
				List<String> respReadUrlList = Lists.transform(tempRespUrl,
						new Function<String, String>() {
							@Override
							public String apply(String input) {
								return MovieDoMain.MOIVE_CATE_URL + input
										+ "?p=" + nextCatePoint;
							}
						});
				
				List<Integer> pageTotalList = Lists.newArrayList();
				List<UrlExecuteStatBean> urlExecBads = Lists.newArrayList();
				IMovieParse<Integer> movieParse = new MovieCountPageParse();

				// 执行Url请求集合
				ExecuteUrlResp.doUrlResultByGetMethod(ipDynDraw,
						respReadUrlList, pageTotalList, movieParse,
						urlExecBads, 10);
				// /////////////

				// 将数据库中已存在的数据，添加入url请求集合中
				if (!existIndexPageTotal.isEmpty()) {
					List<Integer> tempPageTotal = Lists.newArrayList();
					Iterator<Integer> pageTotalIter = pageTotalList.iterator();

					for (int index = 0; index < respUrlGroup.size(); index++) {
						if (existIndexPageTotal.containsKey(index)) {
							tempPageTotal.add(existIndexPageTotal.get(index));
						} else {
							Integer pageTotal = pageTotalIter.next();

							tempPageTotal
									.add(pageTotal == null ? 0 : pageTotal);
						}
					}
					pageTotalList = tempPageTotal;
				}

				for (int i = 0; i < pageTotalList.size(); i++) {
					Integer pageTotal = pageTotalList.get(i);
					String respUrl = respUrlGroup.get(i);
					// 电影id
					final String movieId = Iterables.getLast(Splitter
							.on(cateLink).omitEmptyStrings().split(respUrl));
					// 单个电影类型集合
					List<MovieCateInfo> movieCateList = lastMovieCateColl
							.getMovieCateList();

					// 通过电影id找到集合中的目标对象
					MovieCateInfo movieCateInfo = (MovieCateInfo) CollectionUtils
							.find(movieCateList, new Predicate() {
								@Override
								public boolean evaluate(Object object) {
									MovieCateInfo movieCateInfo = (MovieCateInfo) object;
									String movieIdCopy = Preconditions
											.checkNotNull(movieCateInfo.getId());

									return movieIdCopy.equals(movieId);
								}
							});

					// 将url的执行结果放入找到的对象中
					movieCateInfo.setUrlPageTotal(respUrl, pageTotal);
				}

				List<MovieCateStatBean> movieCateStatDB = Lists.newArrayList();
				List<MovieCateBean> movieCateDB = Lists.newArrayList();

				for (MovieCateInfo movieCate : lastMovieCateColl
						.getMovieCateList()) {
					Map<String, Integer> passUrlTotal = movieCate
							.getUrlPageTotal();

					if (!passUrlTotal.isEmpty()) {
						for (String key : passUrlTotal.keySet()) {
							Integer pageTotal = passUrlTotal.get(key);

							MovieCateStatBean movieCateStat = new MovieCateStatBean();
							movieCateStat.setCateType(lastMovieCateColl
									.getCateName());
							movieCateStat.setCateUrl(key);
							movieCateStat.setPageTotal(pageTotal);

							movieCateStatDB.add(movieCateStat);

							// (0-nextCatepoint] 进入
							if (0 < pageTotal && pageTotal <= nextCatePoint) {
								MovieCateBean movieCateBean = new MovieCateBean();
								movieCateBean.setCateType(cateType);
								movieCateBean.setCateUrl(key);
								movieCateBean.setIsRead(false);
								movieCateBean.setInsertTm(new Date());
								movieCateBean.setTotalPageNum(pageTotal);

								movieCateDB.add(movieCateBean);
							}
						}
					}
				}
				// 根据类型查询已经存在的电影类型集合
				List<MovieCateBean> existMovieCate = movieCateDao
						.findByCateType(cateType);
				Iterables.removeAll(movieCateDB, existMovieCate);
				// 将数据库中没有的电影类型集合放入数据库中
				movieCateDao.save(movieCateDB);

				// 去除数据库中已存在的url
				Iterables.removeAll(movieCateStatDB,
						Lists.newArrayList(haveMovieCateStat));
				// 将电影类型状态集合放入数据中
				movieCateStatDao.save(movieCateStatDB);

				logger.info(cateType + " 类型，第 " + respGroupLoop
						+ " 次执行完毕;共执行链接 --> " + respReadUrlList.size() + " 条");

				if (isBreak)
					break;
			}

			if (loopNum == loopSize) {
				break;
			} else {
				loopNum++;
			}
		}
		logger.info("done~");
	}

	private List<MovieCateStatBean> getMovieCateStateByDB(
			MovieCateStatDao movieCateStatDao, String cateType) {
		return movieCateStatDao.findByCateType(cateType);
	}

	/**
	 * 过滤已存在的url，并返回url在电影集合中的索引位置与页面总数；并将已存在的url移除
	 * 
	 * @param respReadUrlList
	 * @param haveMovieCateStat
	 * @return
	 */
	private Map<Integer, Integer> filterExistMovieCateStat(
			List<String> respReadUrlList,
			List<MovieCateStatBean> haveMovieCateStat) {
		Preconditions.checkArgument(!respReadUrlList.isEmpty());

		Map<Integer, Integer> indexPageTotal = Maps.newHashMap();
		List<String> existRespReadUrlList = Lists.newArrayList();

		for (MovieCateStatBean movieCateStat : haveMovieCateStat) {
			String cateUrlKey = movieCateStat.getCateUrl();
			int index = respReadUrlList.indexOf(cateUrlKey);

			if (index != -1) {
				indexPageTotal.put(index, movieCateStat.getPageTotal());
				existRespReadUrlList.add(cateUrlKey);
			}
		}

		// 移除存在的url
		respReadUrlList.removeAll(existRespReadUrlList);

		return indexPageTotal;
	}
}
