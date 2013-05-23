package com.moviegat.dyfm.service.htmlparse;

public interface IMovieParse<T> {
	/**
	 * 解析html,并返回结果
	 * 
	 * @param paseHtml
	 *            解析的html
	 * @param url
	 *            请求的url
	 * @return
	 */
	public T parseByResult(String paseHtml,String url) throws Exception;
}
