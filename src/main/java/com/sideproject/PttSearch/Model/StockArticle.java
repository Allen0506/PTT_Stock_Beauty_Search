package com.sideproject.PttSearch.Model;

import org.springframework.stereotype.Component;

@Component
public class StockArticle {
	
	private Integer id;
	private String stock_no;
	private Integer author_id;
	private String author_name;
	private String title;
	private String line_title;
	private Double stock_range;
	private Double after_stock_range;

	private String url;
	
	//0 看空 1看多
	private Integer type;
	
	//判斷發布文章後5個交易日的漲跌 0 = 沒判斷 1 = 判斷完成
	private Integer stock_status;
	
	//發文者看多看空最後結果 0:失敗 1:成功
	private Integer result;

	//文章內的日期
	private String article_meta_date;
	
	private Integer start;
	private Integer end;

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

	public Integer getAuthor_id() {
		return author_id;
	}

	public void setAuthor_id(Integer author_id) {
		this.author_id = author_id;
	}

	public String getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLine_title() {
		return line_title;
	}

	public void setLine_title(String line_title) {
		this.line_title = line_title;
	}

	public Double getStock_range() {
		return stock_range;
	}

	public void setStock_range(Double stock_range) {
		this.stock_range = stock_range;
	}
	
	public Double getAfter_stock_range() {
		return after_stock_range;
	}

	public void setAfter_stock_range(Double after_stock_range) {
		this.after_stock_range = after_stock_range;
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

	public Integer getStock_status() {
		return stock_status;
	}

	public void setStock_status(Integer stock_status) {
		this.stock_status = stock_status;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
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
