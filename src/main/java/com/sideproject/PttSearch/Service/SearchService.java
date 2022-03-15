package com.sideproject.PttSearch.Service;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sideproject.PttSearch.Model.PttArticle;

public interface SearchService {
	
	//取得表特版文章資訊
	public void getPostDetail(WebDriver driver, List<WebElement> rEntList, String todayStr, List<PttArticle> pttArticleList);
	
	//儲存表特版圖片
	public void savePhoto(List<WebElement> richContent, List<WebElement> imageContent, PttArticle pttArticle);
	
	//更新股票交易資料
	public void updateStockResult(WebDriver driver);

}
