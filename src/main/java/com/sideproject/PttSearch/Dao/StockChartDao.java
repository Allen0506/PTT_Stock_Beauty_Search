package com.sideproject.PttSearch.Dao;

import java.util.List;

import com.sideproject.PttSearch.Model.StockChart;

public interface StockChartDao {
	
	public void add(StockChart stockChart);
	
	public List<StockChart> getStockChartByStock_article_id(StockChart stockChart);

}
