package com.sideproject.PttSearch.Service;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sideproject.PttSearch.Model.PttArticle;
import com.sideproject.PttSearch.Model.StockArticle;

public interface StockArticleService {
	
	//儲存PTT股版推薦股票文章
	public void saveStockArticle(WebDriver driver, List<StockArticle> stockArticleList, List<String> urlList);
	
	//儲存YAHOO前五個交易日的資料
	public void saveStock(WebDriver driver, List<StockArticle> stockArticleList);
	
	//將走勢圖截圖傳到imgur
	public void saveStockChart(WebDriver driver, List<StockArticle> stockArticleList);
	
	//取得股版推薦股票的交易資訊
	public void getStockTradeData(List<WebElement> richContent, List<WebElement> imageContent, PttArticle pttArticle);


}
