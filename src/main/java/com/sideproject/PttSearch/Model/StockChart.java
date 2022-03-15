package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class StockChart {
	
	private Integer id;
	private Integer stock_article_id;
	private String stock_no;
	private String  url;
	private String  deletehash;

	private String  create_date;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getStock_article_id() {
		return stock_article_id;
	}

	public void setStock_article_id(Integer stock_article_id) {
		this.stock_article_id = stock_article_id;
	}

	public String getStock_no() {
		return stock_no;
	}

	public void setStock_no(String stock_no) {
		this.stock_no = stock_no;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDeletehash() {
		return deletehash;
	}

	public void setDeletehash(String deletehash) {
		this.deletehash = deletehash;
	}

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}
}
