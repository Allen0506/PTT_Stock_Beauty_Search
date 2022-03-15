package com.sideproject.PttSearch.Service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Dao.BeautyImageDao;
import com.sideproject.PttSearch.Dao.PttArticleDao;
import com.sideproject.PttSearch.Dao.StockArticleDao;
import com.sideproject.PttSearch.Dao.StockAuthorDao;
import com.sideproject.PttSearch.Model.BeautyImage;
import com.sideproject.PttSearch.Model.PttArticle;
import com.sideproject.PttSearch.Model.Stock;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Model.StockAuthor;
import com.sideproject.PttSearch.Service.SearchService;

@Component
public class SearchServiceImpl implements SearchService {

	@Autowired
	private PttArticleDao pttArticleDao;

	@Autowired
	private BeautyImageDao beautyImageDao;
	
	@Autowired
	private StockArticleDao stockArticleDao;
	
	@Autowired
	private StockAuthorDao stockAuthorDao;

	@Autowired
	private ToolServiceImpl toolService;

	@Override
	public void getPostDetail(WebDriver driver, List<WebElement> rEntList, String todayStr,
			List<PttArticle> pttArticleList) {

		PttArticle pttArticle;

		// r-ent 元素 [10, [閒聊] SOLEND 的IDO或許是一台靈車(強烈建議…, a3556959, ⋯, 11/02]
		for (WebElement eachPost : rEntList) {

			pttArticle = new PttArticle();

			String[] post = eachPost.getText().split("\n");
			if (!post[post.length - 1].equals("M") && post[post.length - 1].equals(todayStr) && post.length == 5) {

				if (post[2].equals("DevilHotel")) {
					continue;
				}

				try {
					if (post[0].equals("爆")) {

						pttArticle.setPush("99");
						pttArticle.setTitle(post[1]);
						pttArticle.setPost_date(post[4]);
						pttArticle.setUrl(driver.findElement(By.linkText(post[1])).getAttribute("href"));

						pttArticleList.add(pttArticle);

					} else if (Integer.valueOf(post[0]) >= 30) {

						pttArticle.setPush(post[0]);
						pttArticle.setTitle(post[1]);
						pttArticle.setPost_date(post[4]);
						pttArticle.setUrl(driver.findElement(By.linkText(post[1])).getAttribute("href"));

						pttArticleList.add(pttArticle);

					}
				} catch (NumberFormatException e) {

				}
			}

		}
	}

	@Override
	public void savePhoto(List<WebElement> richContent, List<WebElement> imageContent, PttArticle pttArticle) {

		// Article_meta_date 文章日期格式 Fri Dec 10 09:15:42 2021
		// EEE MMM dd HH:mm:ss yyyy
		pttArticle.setArticle_meta_date(toolService.changeDateFormat(pttArticle.getArticle_meta_date(),
				"EEE MMM dd HH:mm:ss yyyy", "yyyyMMdd"));

		String new_title = pttArticle.getArticle_meta_date() + "_"
				+ (pttArticleDao.getArticleByDate(pttArticle).size() + 1);

		// 文章標題存入資料庫
		pttArticle.setNew_title(new_title);

		if (pttArticle.getTitle().substring(1, 3).equals("正妹")) {
			pttArticle.setType(0);
		} else if (pttArticle.getTitle().substring(1, 3).equals("帥哥")) {
			pttArticle.setType(1);
		} else {
			pttArticle.setType(2);
		}
		pttArticle = pttArticleDao.add(pttArticle);

		BeautyImage beautyImage;

		// 圖片存檔
		Integer imageCount = 0;
		String[] getImageType = null;
		for (int j = 0; j < richContent.size(); j++) {

			beautyImage = new BeautyImage();
			getImageType = richContent.get(j).getAttribute("href").split("\\.");

			if (richContent.get(j).getAttribute("href").contains("i.imgur.com")) {

				if (!getImageType[getImageType.length - 1].equals("mp4")) {

					if (!getImageType[getImageType.length - 1].contains(".jpg")
							|| !getImageType[getImageType.length - 1].contains(".jpeg")
							|| !getImageType[getImageType.length - 1].contains(".png")) {

						beautyImage.setUrl(richContent.get(j).getAttribute("href") + ".jpg");
						getImageType = beautyImage.getUrl().split("\\.");

					} else {
						beautyImage.setUrl(richContent.get(j).getAttribute("href"));
					}

					beautyImage.setArticle_id(pttArticle.getId());
					beautyImage.setType(0);

					beautyImageDao.add(beautyImage);

					imageCount++;
				}

				// 本機測試機 下載圖片
//				File dir_file = new File("src\\main\\resources\\images\\" + new_title); /* 路徑跟檔名 */
//				if (!Files.exists(Paths.get("src\\main\\resources\\images\\" + new_title))) {
//					dir_file.mkdirs();
//				}
//
//				try {
//					URL url = new URL(richContent.get(j).getAttribute("href").toString());
//					String imageName = "src\\main\\resources\\images\\" + new_title + "\\" + imageCount + "."
//							+ getImageType[getImageType.length - 1];
//
//					toolService.downLoadImage(url, imageName);
//
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

			// ptt_web無法顯示的圖片 所以要自己儲存
			} else if (richContent.get(j).getAttribute("href").contains("imgur.com")) {

				imageCount++;

				File dir_file = new File("src\\main\\resources\\images\\" + new_title); /* 路徑跟檔名 */
				if (!Files.exists(Paths.get("src\\main\\resources\\images\\" + new_title))) {
					dir_file.mkdirs();
				}

				String imageStr = richContent.get(j).getAttribute("href");
				StringBuilder imageUrl = new StringBuilder();
				String imageName = "";


				System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");

				ChromeOptions options = new ChromeOptions();

				options.addArguments("--headless");

				WebDriver imgDriver = new ChromeDriver(options);

				WebDriverWait wait = new WebDriverWait(imgDriver, 3);

				imgDriver.get(imageStr);

				synchronized (imgDriver) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

				try {
					imgDriver.findElement(By.className("btn-wall--yes")).click();
				} catch (NoSuchElementException ne) {

				}

				imgDriver.manage().window().maximize();
				
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("image-placeholder")));
					imgDriver.findElement(By.className("image-placeholder")).click();
				} catch (Exception e) {
					if (richContent.get(j).getAttribute("href").contains(".jpg")
							|| richContent.get(j).getAttribute("href").contains(".jpeg")
							|| richContent.get(j).getAttribute("href").contains(".png")) {
						
					}else {
						
						imgDriver.close();
						continue;
					}
				}

				WebElement img = null;

				try {

					img = imgDriver.findElement(By.className("ImageViewerContent")).findElement(By.tagName("img"));
					imageUrl = new StringBuilder(img.getAttribute("src"));

				} catch (NoSuchElementException ne) {
					imageUrl = new StringBuilder();
					imageUrl.append(richContent.get(j).getAttribute("href").substring(0, 8));
					imageUrl.append("i.");
					if (!richContent.get(j).getAttribute("href").contains(".jpg")
							|| !richContent.get(j).getAttribute("href").contains(".jpeg")
							|| !richContent.get(j).getAttribute("href").contains(".png")) {

						imageUrl.append(richContent.get(j).getAttribute("href").substring(8));
						imageUrl.append(".jpg");
					}

				}

				beautyImage.setUrl(imageUrl.toString());

				imgDriver.close();

				getImageType = imageUrl.toString().split("\\.");

				imageName = "src\\main\\resources\\images\\" + new_title + "\\" + imageCount + "."
						+ getImageType[getImageType.length - 1];

				try {
					URL url = new URL(imageUrl.toString());

					toolService.downLoadImage(url, imageName);

					beautyImage.setArticle_id(pttArticle.getId());
					beautyImage.setType(0);
					beautyImage.setUrl(imageUrl.toString());

					beautyImageDao.add(beautyImage);

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (richContent.get(j).getAttribute("href").contains(".jpg")
					|| richContent.get(j).getAttribute("href").contains(".png")) {

				beautyImage.setArticle_id(pttArticle.getId());
				beautyImage.setUrl(richContent.get(j).getAttribute("href"));
				beautyImage.setType(0);

				beautyImageDao.add(beautyImage);

				imageCount++;

//				// 本機測試機 下載圖片
//				File dir_file = new File("src\\main\\resources\\images\\" + new_title); /* 路徑跟檔名 */
//				if (!Files.exists(Paths.get("src\\main\\resources\\images\\" + new_title))) {
//					dir_file.mkdirs();
//				}
//
//				try {
//					URL url = new URL(richContent.get(j).getAttribute("href").toString());
//					String imageName = "src\\main\\resources\\images\\" + new_title + "\\" + imageCount + "."
//							+ getImageType[getImageType.length - 1];
//
//					toolService.downLoadImage(url, imageName);
//
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

			} else if (richContent.get(j).getAttribute("href").equals(pttArticle.getUrl())) {
				break;
			}

		}

		pttArticle.setQuantity(imageCount);
		pttArticleDao.updateQuantity(pttArticle);

	}

	@Override
	public void updateStockResult(WebDriver driver) {
		
		List<StockArticle> articleList = stockArticleDao.getStockArticleHaveNoResult(new StockArticle());
		List<Stock> stockList;
		for(StockArticle eachStockArticle : articleList) {
			
			stockList = stockArticleDao.getStockDataByStock_article_id(eachStockArticle);
			String date = stockList.get(stockList.size()-1).getDate();
			
			StringBuilder url = new StringBuilder();
			List<WebElement> tradeList = new ArrayList<>();

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
			List<Stock> afterStockList = new ArrayList<>();
			int getDate = 0;
			int countNum = 0;
			StockAuthor stockAuthor = new StockAuthor();
			for (int i = 1; i <= tradeList.size()-1; i++) {
								
				if(getDate == 0 && date.equals(tradeList.get(i).findElements(By.className("Py(10px)")).get(0).findElement(By.tagName("span")).getText())) {

					i = i-6;
					
					if(i < 7) {
						countNum =1;
						break;
					}
					
					getDate = 1;
				}
				else if(getDate == 1 && afterStockList.size() != 5) {
					Stock stock = new Stock();
					stock.setDate(tradeList.get(i).findElements(By.className("Py(10px)")).get(0).findElement(By.tagName("span")).getText());
					stock.setOpen(Double.parseDouble(tradeList.get(i).findElements(By.className("Py(10px)")).get(1).findElement(By.tagName("span")).getText().replaceAll(",", "")));
					stock.setClose(Double.parseDouble(tradeList.get(i).findElements(By.className("Py(10px)")).get(4).findElement(By.tagName("span")).getText().replaceAll(",", "")));
					afterStockList.add(stock);
				}
				else if(getDate == 1 && afterStockList.size() == 5) {
					eachStockArticle.setAfter_stock_range(toolService.countStockRange(afterStockList.get(4).getOpen(), afterStockList.get(0).getClose()));
					eachStockArticle.setStock_status(1);
					
					if(eachStockArticle.getType() == 0) {
						if(eachStockArticle.getAfter_stock_range() <= 0) {
							eachStockArticle.setResult(1);
						}else {
							eachStockArticle.setResult(0);
						}
					}else {
						if(eachStockArticle.getAfter_stock_range() >= 0) {
							eachStockArticle.setResult(1);
						}else {
							eachStockArticle.setResult(0);
						}
					}

					stockArticleDao.updateAfterRange(eachStockArticle);
					
					stockAuthor.setId(eachStockArticle.getAuthor_id());
					stockAuthorDao.updateWinRate(stockAuthor);

					break;

				}
			}

			if(getDate == 1 && countNum == 1) {
				break;
			}
		}
		
	}

}
