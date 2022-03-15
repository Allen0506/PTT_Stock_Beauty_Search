package com.sideproject.PttSearch;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Model.PttArticle;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Service.impl.SearchServiceImpl;
import com.sideproject.PttSearch.Service.impl.StockArticleServiceImpl;
import com.sideproject.PttSearch.Service.impl.ToolServiceImpl;

@Component
public class ScheduledTasks {
	
	@Autowired
	private SearchServiceImpl searchService;
	
	@Autowired
	private StockArticleServiceImpl stockArticleService;
	
	@Autowired
	private ToolServiceImpl toolService;
	
	@Scheduled(cron = "0 5 0 * * *")
	public void stockCrawler() {
		
		String yesterdayStr = toolService.getYesterdayStr("MM/dd");
		
		System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
		
		//無頭模式
		ChromeOptions options = new ChromeOptions();		
		options.addArguments("--headless");
		options.addArguments(
				"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");

		WebDriver driver = new ChromeDriver(options);
		
		driver.get("https://www.ptt.cc/bbs/Stock/index.html");
		
		driver.findElement(By.name("q")).sendKeys("標的");
		driver.findElement(By.name("q")).sendKeys(Keys.ENTER);
		
		List<WebElement> rEntList = driver.findElements(By.className("r-ent"));
		
		WebElement firstPostElement = rEntList.get(0);
		String[] firstPost = firstPostElement.getText().split("\n");
		
		List<String> urlList = new ArrayList<>();
		
		String[] post;
		
		// 判斷不是M文及日期是今天的文章
		while (!firstPost[firstPost.length - 1].equals("M") && firstPost[firstPost.length - 1].equals(yesterdayStr)) {
			
			for(WebElement eachPost : rEntList) {
				post = eachPost.getText().split("\n");
				
				if(post[post.length - 1].equals(yesterdayStr)) {				
					//有推噓數格式  [4, [標的] 群創3481 短多, Danielhoho, ⋯, 12/15]
					//無推噓數格式  [[標的] 8299.TWO 群聯 多, Paul1021, ⋯, 12/15]
					if(eachPost.getText().contains("[標的]") && !eachPost.getText().contains("Re:") && !eachPost.getText().contains("請益")) {
						
						if(post.length == 5) {
							urlList.add(driver.findElement(By.linkText(post[1])).getAttribute("href"));
						}else {
							urlList.add(driver.findElement(By.linkText(post[0])).getAttribute("href"));
						}
						
					}
				}
				
			}
			
			driver.findElement(By.linkText("‹ 上頁")).click();
			rEntList = driver.findElements(By.className("r-ent"));
			firstPostElement = rEntList.get(0);
			firstPost = firstPostElement.getText().split("\n");
		}
		
		for(WebElement eachPost : rEntList) {
			post = eachPost.getText().split("\n");		
			if(post[post.length - 1].equals(yesterdayStr)) {				
				//有推噓數格式  [4, [標的] 群創3481 短多, Danielhoho, ⋯, 12/15]
				//無推噓數格式  [[標的] 8299.TWO 群聯 多, Paul1021, ⋯, 12/15]
				if(eachPost.getText().contains("[標的]") && !eachPost.getText().contains("Re:") && !eachPost.getText().contains("請益")) {
					
					if(post.length == 5) {
						urlList.add(driver.findElement(By.linkText(post[1])).getAttribute("href"));
					}else {
						urlList.add(driver.findElement(By.linkText(post[0])).getAttribute("href"));
					}
					
				}
			}
		}

		List<StockArticle> stockArticleList = new ArrayList<>();
		
		stockArticleService.saveStockArticle(driver, stockArticleList, urlList);
		
		stockArticleService.saveStock(driver, stockArticleList);
		
		driver.close();

	}
	
	@Scheduled(cron = "0 10 0 * * *")
	public void beautyCrawler() {

		String yesterdayStr = toolService.getYesterdayStr("MM/dd");
		List<PttArticle> pttArticleList = new ArrayList<>();

		System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");

//		WebDriver driver = new ChromeDriver();
		
		//無頭模式
		ChromeOptions options = new ChromeOptions();		
		options.addArguments("--headless");
		options.addArguments(
				"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
		WebDriver driver = new ChromeDriver(options);

//		driver.get("https://www.ptt.cc/bbs/DigiCurrency/index.html");
//		driver.get("https://www.ptt.cc/bbs/Gossiping/index.html");
		driver.get("https://www.ptt.cc/bbs/Beauty/index.html");
		driver.findElement(By.name("yes")).click();

		// r-ent 元素 [10, [閒聊] SOLEND 的IDO或許是一台靈車(強烈建議…, a3556959, ⋯, 11/02]
		List<WebElement> rEntList = driver.findElements(By.className("r-ent"));

		WebElement firstPostElement = rEntList.get(0);
		String[] firstPost = firstPostElement.getText().split("\n");
		
		// 判斷不是M文及日期是今天的文章
		while (!firstPost[firstPost.length - 1].equals("M") && firstPost[firstPost.length - 1].equals(yesterdayStr)) {
		
			searchService.getPostDetail(driver,rEntList, yesterdayStr, pttArticleList);
			driver.findElement(By.linkText("‹ 上頁")).click();
			rEntList = driver.findElements(By.className("r-ent"));
			firstPostElement = rEntList.get(0);
			firstPost = firstPostElement.getText().split("\n");
			
		}
		
		searchService.getPostDetail(driver,rEntList, yesterdayStr, pttArticleList);
		
		for (PttArticle pttArticle: pttArticleList) {
			
			driver.get(pttArticle.getUrl());
			
			List<WebElement> richContent = driver.findElements(By.tagName("a"));
			List<WebElement> imageContent = driver.findElements(By.tagName("img"));
			List<WebElement> metalineList = driver.findElements(By.className("article-metaline"));
			
			pttArticle.setArticle_meta_date(metalineList.get(2).findElement(By.className("article-meta-value")).getText());

			searchService.savePhoto(richContent, imageContent, pttArticle);

		}

		driver.close();

	}
	
	@Scheduled(cron = "0 20 0 * * *")
	public void updateStockResult() {
		
		System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments(
				"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");

		WebDriver driver = new ChromeDriver(options);

		searchService.updateStockResult(driver);
		
		driver.close();
				
	}


}
