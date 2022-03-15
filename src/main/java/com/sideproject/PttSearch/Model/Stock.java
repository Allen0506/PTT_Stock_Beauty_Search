package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class Stock {
	
	private Integer id;
	private String stock_no;
	private Integer stock_article_id;
	private String  date;
	private double  open;
	private double  high;
	private double  low;
	private double  close;
	private double  adj_close;
	private String  volume;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getStock_no() {
		return stock_no;
	}
	public void setStock_no(String stock_no) {
		this.stock_no = stock_no;
	}
	public Integer getStock_article_id() {
		return stock_article_id;
	}
	public void setStock_article_id(Integer stock_article_id) {
		this.stock_article_id = stock_article_id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getAdj_close() {
		return adj_close;
	}
	public void setAdj_close(double adj_close) {
		this.adj_close = adj_close;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}

}
