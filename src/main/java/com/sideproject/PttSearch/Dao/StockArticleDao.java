package com.sideproject.PttSearch.Dao;

import java.util.List;

import com.sideproject.PttSearch.Model.Stock;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Model.StockChart;

public interface StockArticleDao {

	public StockArticle add(StockArticle stockArticle);
	
	public void delete(StockArticle stockArticle);

	public void updateLineTitle(StockArticle stockArticle);	
	
	public void updateRange(StockArticle stockArticle);
	
	public void updateAfterRange(StockArticle stockArticle);
	
	public void updateAuthorId(StockArticle stockArticle);
	
	public StockChart getStockChartByArticleId(StockArticle stockArticle);

	public StockArticle getPreviousArticle(StockArticle stockArticle);

	public StockArticle getArticleById(StockArticle stockArticle);

	public StockArticle getNextArticle(StockArticle stockArticle);

	public List<StockArticle> getStockArticleByPostDate(StockArticle stockArticle);

	public List<StockArticle> getLatestArticle(StockArticle stockArticle);

	public List<Stock> getStockDataByStock_article_id(StockArticle stockArticle);
	
	public List<StockArticle> getAllStockArticle(StockArticle stockArticle);
	
	public List<StockArticle> getStockArticleByAuthorId(StockArticle stockArticle);

	public List<StockArticle> getStockArticleHaveNoResult(StockArticle stockArticle);

}
