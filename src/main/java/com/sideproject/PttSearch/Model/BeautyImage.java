package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class BeautyImage {
	private Integer id;
	private Integer article_id;
	private String url;
	private Integer type;
	private Integer start;
	private Integer end;


	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getArticle_id() {
		return article_id;
	}
	public void setArticle_id(Integer article_id) {
		this.article_id = article_id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
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
