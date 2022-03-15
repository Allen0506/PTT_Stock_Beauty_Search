package com.sideproject.PttSearch.Service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Dao.StockArticleDao;
import com.sideproject.PttSearch.Dao.StockAuthorDao;
import com.sideproject.PttSearch.Dao.StockChartDao;
import com.sideproject.PttSearch.Dao.StockDao;
import com.sideproject.PttSearch.Model.PttArticle;
import com.sideproject.PttSearch.Model.Stock;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Model.StockAuthor;
import com.sideproject.PttSearch.Model.StockChart;
import com.sideproject.PttSearch.Service.StockArticleService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class StockArticleServiceImpl implements StockArticleService {

	@Value("${imgur_client_id}")
	private String IMGUR_CLIENT_ID;

	@Autowired
	private ToolServiceImpl toolService;

	@Autowired
	private StockArticleDao stockArticleDao;

	@Autowired
	private StockChartDao stockChartDao;
	
	@Autowired
	private StockAuthorDao stockAuthorDao;

	@Autowired
	private StockDao stockDao;

	@Override
	public void saveStockArticle(WebDriver driver, List<StockArticle> stockArticleList, List<String> urlList) {

		String main_contain;
		String[] contain;
		String target = null;
		
		StockAuthor stockAuthor;

		for (int i = 0; i < urlList.size(); i++) {
			stockAuthor = new StockAuthor();

			driver.get(urlList.get(i));

			StockArticle stockArticle = new StockArticle();

			main_contain = driver.findElement(By.id("main-content")).getText();
			contain = main_contain.split("\n");

			for (String str : contain) {
				if (str.contains("標的：") && str.contains("US")) {
					target = str;
					break;
				}
			}

			if (target != null) {
				target = null;
				continue;
			}

			try {
				stockArticle.setUrl(urlList.get(i));
				
				stockAuthor.setAuthor(driver.findElements(By.className("article-meta-value")).get(0).getText().split(" ")[0]);
				
				stockAuthor = stockAuthorDao.getAuthorByName(stockAuthor);
				if(stockAuthor.getId() == null) {
					stockAuthor = stockAuthorDao.add(stockAuthor);
					stockArticle.setAuthor_id(stockAuthor.getId());
				}else {
					stockArticle.setAuthor_id(stockAuthor.getId());
				}

				stockArticle.setTitle(driver.findElements(By.className("article-meta-value")).get(2).getText());
				stockArticle.setStock_no(toolService.extractNumber(stockArticle.getTitle()));

				if (!stockArticle.getStock_no().equals("")) {

					stockArticle.setArticle_meta_date(
							driver.findElements(By.className("article-meta-value")).get(3).getText());
					
					stockArticle.setArticle_meta_date(
							toolService.changeDateFormat(stockArticle.getArticle_meta_date(), "EEE MMM dd HH:mm:ss yyyy",
							"yyyyMMdd"));

					if (stockArticle.getTitle().contains("多")) {
						stockArticle.setType(1);

						stockArticle = stockArticleDao.add(stockArticle);

						stockArticleList.add(stockArticle);
					} else if (stockArticle.getTitle().contains("空")) {
						stockArticle.setType(0);

						stockArticle = stockArticleDao.add(stockArticle);

						stockArticleList.add(stockArticle);
					} else {

						for (String line : contain) {
							if (line.contains("分類：") && line.contains("多")) {
								stockArticle.setType(0);

								stockArticle = stockArticleDao.add(stockArticle);

								stockArticleList.add(stockArticle);
								break;
							} else if (line.contains("分類：") && line.contains("空")) {
								stockArticle.setType(0);

								stockArticle = stockArticleDao.add(stockArticle);

								stockArticleList.add(stockArticle);
								break;
							}
						}
					}

				}

			} catch (IndexOutOfBoundsException ie) {

			}

		}

	}

	@Override
	public void saveStock(WebDriver driver, List<StockArticle> stockArticleList) {

		StringBuilder url;
		List<WebElement> tradeList;
		Stock stock;
		Double FirstOpen = null; 
		Double lastClose = null;
		
		for (StockArticle eachStockArticle : stockArticleList) {

			url = new StringBuilder();
			tradeList = new ArrayList<>();

			// yahoo 股票資料網址 https://finance.yahoo.com/quote/3016.TW/history?p=3016.TW
			url.append("https://finance.yahoo.com/quote/");
			url.append(eachStockArticle.getStock_no());
			url.append(".TW/history?p=");
			url.append(eachStockArticle.getStock_no());
			url.append(".TW");

			driver.get(url.toString());

			try {

				tradeList = driver.findElement(By.id("Col1-1-HistoricalDataTable-Proxy"))
						.findElements(By.tagName("tr"));

			} catch (NoSuchElementException ne) {

				url = new StringBuilder();
				tradeList = new ArrayList<>();

				// yahoo 上櫃 股票 資料網址 https://finance.yahoo.com/quote/3016.TWO/history?p=3016.TWO
				url.append("https://finance.yahoo.com/quote/");
				url.append(eachStockArticle.getStock_no());
				url.append(".TWO/history?p=");
				url.append(eachStockArticle.getStock_no());
				url.append(".TWO");

				driver.get(url.toString());

				tradeList = driver.findElement(By.id("Col1-1-HistoricalDataTable-Proxy"))
						.findElements(By.tagName("tr"));
			}

			// 第一行顯示 Date Open High Low Close* Adj Close** Volume
			// Dec 20, 2021 130.00 134.00 130.00 132.50 132.50 4,721,978
			// 只取前五筆資訊
			for (int i = 1; i <= 5; i++) {
				
				List<WebElement> dataList = tradeList.get(i).findElements(By.className("Py(10px)"));

				stock = new Stock();

				stock.setStock_article_id(eachStockArticle.getId());
				stock.setStock_no(eachStockArticle.getStock_no());
				stock.setDate(dataList.get(0).findElement(By.tagName("span")).getText());
				stock.setOpen(Double.parseDouble(dataList.get(1).findElement(By.tagName("span")).getText().replaceAll(",", "")));
				stock.setHigh(Double.parseDouble(dataList.get(2).findElement(By.tagName("span")).getText().replaceAll(",", "")));
				stock.setLow(Double.parseDouble(dataList.get(3).findElement(By.tagName("span")).getText().replaceAll(",", "")));
				stock.setClose(Double.parseDouble(dataList.get(4).findElement(By.tagName("span")).getText().replaceAll(",", "")));
				stock.setAdj_close(Double.parseDouble(dataList.get(5).findElement(By.tagName("span")).getText().replaceAll(",", "")));
				stock.setVolume(dataList.get(6).findElement(By.tagName("span")).getText());
				
				if(i == 1) {
					FirstOpen = stock.getOpen();
				}
				
				if(i == 5) {
					lastClose = stock.getClose();
				}

				stockDao.add(stock);
			}
			
			eachStockArticle.setStock_range(toolService.countStockRange(FirstOpen, lastClose));
			stockArticleDao.updateRange(eachStockArticle);
		}

	}

	@Override
	public void saveStockChart(WebDriver driver, List<StockArticle> stockArticleList) {

		List<File> fileList = new ArrayList<>();
		List<StockChart> chartList = new ArrayList<>();

		for (int i = 0; i < stockArticleList.size(); i++) {

			StockArticle eachArticle = stockArticleList.get(i);

			try {
				driver.get("https://www.cmoney.tw/forum/stock/" + eachArticle.getStock_no());
			}catch(TimeoutException te) {
				driver.get("https://www.cmoney.tw/forum/stock/" + eachArticle.getStock_no());
			}
			
			driver.manage().window().maximize();

			try {
				
				driver.findElements(By.className("btn-secondary-100")).get(1).click();
			}catch(IndexOutOfBoundsException ie) {
				stockArticleDao.delete(stockArticleList.get(i));
				stockArticleList.remove(i);
				continue;
			}
			
			eachArticle.setLine_title(driver.findElement(By.className("stockData__name")).getText()
					+ driver.findElement(By.className("stockData__id")).getText());
			stockArticleDao.updateLineTitle(eachArticle);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0,320)", "");

			synchronized (driver) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			File SrcFile = driver.findElement(By.className("stockPanel__body")).getScreenshotAs(OutputType.FILE);

			fileList.add(SrcFile);

			StockChart stockChart = new StockChart();
			stockChart.setStock_article_id(eachArticle.getId());
			stockChart.setStock_no(eachArticle.getStock_no());
			chartList.add(stockChart);

		}

		uploadImgToImgur(fileList, chartList);

	}

	public void uploadImgToImgur(List<File> fileList, List<StockChart> chartList) {
		OkHttpClient client = new OkHttpClient();

		for (int i = 0; i < fileList.size(); i++) {

			StringBuilder res = new StringBuilder();

			Request request = new Request.Builder().url("https://api.imgur.com/3/image")
					.header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
					.post(okhttp3.RequestBody.create(MediaType.parse("file; charset=utf-8"), fileList.get(i)))
					.build();

			client.newCall(request).enqueue(new Callback() {

				@Override
				public void onFailure(Call call, IOException e) {
					System.err.println(e);

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException, JSONException {

					String responseStr = response.body().string();
					res.append(responseStr);
				}
			});

			synchronized (res) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			try {
				JSONObject replyObject = new JSONObject(res.toString());
				chartList.get(i).setDeletehash(((JSONObject) replyObject.get("data")).get("deletehash").toString());
				chartList.get(i).setUrl(((JSONObject) replyObject.get("data")).get("link").toString());
				stockChartDao.add(chartList.get(i));

			} catch (JSONException je) {
				i = i - 1;
			}

		}

	}

	@Override
	public void getStockTradeData(List<WebElement> richContent, List<WebElement> imageContent, PttArticle pttArticle) {

	}

}
