package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class StockAuthor {
	
	private Integer id;
	private String author;
	private Double win_rate;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Double getWin_rate() {
		return win_rate;
	}
	public void setWin_rate(Double win_rate) {
		this.win_rate = win_rate;
	}

}
