package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class PttArticle {
	private Integer id;
	private String title;
	private String new_title;
	private String url;
	private Integer quantity;
	private Integer type;
	private String push;
	private String post_date;
	
	private Integer start;
	private Integer end;

	
	//文章內的日期
	private String article_meta_date;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNew_title() {
		return new_title;
	}

	public void setNew_title(String new_title) {
		this.new_title = new_title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPush() {
		return push;
	}

	public void setPush(String push) {
		this.push = push;
	}

	public String getPost_date() {
		return post_date;
	}

	public void setPost_date(String post_date) {
		this.post_date = post_date;
	}

	public String getArticle_meta_date() {
		return article_meta_date;
	}

	public void setArticle_meta_date(String article_meta_date) {
		this.article_meta_date = article_meta_date;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

}
