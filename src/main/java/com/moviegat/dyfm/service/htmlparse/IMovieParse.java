package com.moviegat.dyfm.service.htmlparse;

public interface IMovieParse<T> {
	/**
	 * 解析html,并返回结果
	 * 
	 * @param paseHtml
	 *            解析的html
	 * @param clazz
	 *            返回的结果类
	 * @return
	 */
	public T parseByResult(String paseHtml) throws Exception;
}
