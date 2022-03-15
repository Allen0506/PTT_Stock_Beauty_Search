package com.sideproject.PttSearch.Service.impl;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Dao.StockArticleDao;
import com.sideproject.PttSearch.Dao.StockAuthorDao;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Model.StockAuthor;
import com.sideproject.PttSearch.Model.StockChart;
import com.sideproject.PttSearch.Service.BeautyLineService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class StockLineServiceImpl implements BeautyLineService {

	@Value("${line.bot.channel-token}")
	private String LINE_TOKEN;

	@Value("${ngrok_link}")
	private String ngrok_link;

//	@Autowired
//	private ToolServiceImpl toolService;
	
	@Autowired
	private StockArticleDao stockArticleDao;
	
	@Autowired
	private StockAuthorDao stockAuthorDao;

	private OkHttpClient client = new OkHttpClient();

	@Override
	public void replyLine(JSONObject object) {

		String user_id = ((JSONObject) object.getJSONArray("events").get(0)).getJSONObject("source").get("userId")
				.toString();

		JSONObject userObject = getUser(user_id);

		String text = ((JSONObject) object.getJSONArray("events").get(0)).getJSONObject("message").get("text").toString();

		String replyToken = ((JSONObject) object.getJSONArray("events").get(0)).get("replyToken").toString();

		JSONObject replyBody = new JSONObject();
		JSONArray messages = new JSONArray();
		JSONObject message = new JSONObject();

		StockArticle stockArticle = new StockArticle();

		String[] userText = text.split(" ");

		if (userText.length == 1 && userText[0].equals("股板標的")) {

			List<StockArticle> articleList = stockArticleDao.getLatestArticle(stockArticle);
			
			replyLatestArticle(userObject, messages, message, articleList);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);

		// 搜尋特定日期表特文章 USER傳送訊息格式: 股板標的 搜尋文章 日期 yyyyMMdd
		} else if(userText.length == 4 && userText[1].equals("搜尋文章")){

			stockArticle.setArticle_meta_date(userText[3]);
			
			List<StockArticle> articleList = stockArticleDao.getStockArticleByPostDate(stockArticle);
			
			replyLatestArticle(userObject, messages, message, articleList);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);
			
		// 搜尋特定文章 顯示內文圖片 USER傳送訊息格式:股板標的 搜尋文章 編碼後id 範圍 n~n+9
		} else if(userText.length == 5 && userText[1].equals("搜尋文章") && userText[3].equals("範圍")) {
			
			String[] range;
			try {
				range = new String(java.util.Base64.getDecoder().decode(userText[4])).split("~");

			} catch (Exception e) {
				message.put("type", "text");
				message.put("text", userObject.get("displayName") + "你好: 語法有誤請重新輸入或重新點擊文章跑馬燈的圖片");
				messages.put(message);
				return;
			}

			
			stockArticle.setId(Integer.valueOf(new String(java.util.Base64.getDecoder().decode(userText[2]))));
			stockArticle = stockArticleDao.getArticleById(stockArticle);
			stockArticle.setStart(Integer.valueOf(range[0]));
			stockArticle.setEnd(Integer.valueOf(range[1]));
						
			replyStockDetail(userObject, messages, message, stockArticle);
			
			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);
		}

	}
	
	// 回復用戶呼叫股板標的指令
	public void replyStockDetail(JSONObject userObject, JSONArray messages, JSONObject message, StockArticle stockArticle) {
		
		StockChart stockChart = stockArticleDao.getStockChartByArticleId(stockArticle);
		
		StockAuthor stockAuthor = new StockAuthor();
		stockAuthor.setId(stockArticle.getAuthor_id());
		stockAuthor = stockAuthorDao.getAuthorById(stockAuthor);
		
		List<StockArticle> stockArticleList = stockArticleDao.getStockArticleByAuthorId(stockArticle);
		
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(userObject.get("displayName") + "你好:" + stockArticle.getLine_title() + "前五個交易日的K線走勢圖\n")
					  .append("作者: " + stockArticle.getAuthor_name() + "共有" + stockArticleList.size() + "篇文章 ");
		
		if(stockAuthor.getWin_rate() != null) {
			messageBuilder.append(", 勝率為:" + stockAuthor.getWin_rate());
		}
				
		message.put("type", "text");
		try {
			message.put("text", messageBuilder.toString());
		} catch (JSONException je) {
			message.put("text", "你好: 因為LINE API傳送資料出現問題 請再重新輸入");
			messages.put(message);

			return;
		}
		messages.put(message);
		
		if(stockChart.getUrl() != null) {
			
			message = new JSONObject();
			message.put("type", "image");
			message.put("originalContentUrl", stockChart.getUrl());
			message.put("previewImageUrl", stockChart.getUrl());
			
			messages.put(message);
		}
		
		message = new JSONObject();
		
		JSONObject template = new JSONObject();
		JSONObject carousel = new JSONObject();
		JSONArray carousel_bubbles = new JSONArray();
		JSONObject bubble = new JSONObject();
		JSONObject bubble_header = new JSONObject();
		JSONArray header_contents = new JSONArray();
		JSONObject header_content = new JSONObject();
		JSONObject bubble_body = new JSONObject();
		String bar_color;
		String box_color;
		String code;
		
		template.put("type", "carousel");
		carousel.put("type", "carousel");
		
		for(StockArticle eachArticle : stockArticleList) {

			// 條顏色      紅#CE0000  綠#0D8186
			// box顏色  紅#FF5151  綠#96FED1
			
			bubble.put("type", "bubble");
			
			bubble_header = new JSONObject();
			
			bubble_header.put("type", "box");
			bubble_header.put( "layout", "vertical");
			
			header_content.put("type", "text");
			header_content.put("text", eachArticle.getLine_title());

			header_content.put("size", "3xl");
			header_content.put("align", "center");

			header_contents.put(header_content);
			header_content = new JSONObject();
			
			header_content.put("type", "text");
			header_content.put("text", "發文者:" + stockArticle.getAuthor_name());

			header_content.put("size", "xl");
			header_content.put("align", "center");

			header_contents.put(header_content);
			header_content = new JSONObject();
			
			header_content.put("type", "text");
			header_content.put("text", "發文日期:" + eachArticle.getArticle_meta_date());

			header_content.put("size", "xl");
			header_content.put("align", "center");

			header_contents.put(header_content);
			header_content = new JSONObject();
			
			header_content.put("type", "text");		
			if(eachArticle.getStock_range() > 0) {
				header_content.put("text", "發文前五個交易日漲" + Math.abs(eachArticle.getStock_range()) + "%");
			}else if(eachArticle.getStock_range() < 0){
				header_content.put("text", "發文前五個交易日跌" + Math.abs(eachArticle.getStock_range()) + "%");
			}else {
				header_content.put("text", "發文前五個交易日平盤");
			}
			
			header_content.put("margin", "lg");
			header_content.put("size", "xl");
			header_content.put("align", "center");
			
			header_contents.put(header_content);
			header_content = new JSONObject();
			
			if(eachArticle.getStock_range() >= 0) {
				bar_color = "#CE0000";
			}else {
				bar_color = "#0D8186";
			}
			
			header_content.put("type", "box");		
			header_content.put("layout", "vertical");		
			header_content.put("contents", new JSONArray().put(new JSONObject()
					.put("type", "box")
					.put("layout", "vertical")
					.put("contents", new JSONArray().put(new JSONObject().put("type", "filler")))
					.put("width", Math.abs(eachArticle.getStock_range()) + "%")
					.put("height", "6px")
					.put("backgroundColor", bar_color)
					));		
			
			if(eachArticle.getType() == 0) {
				box_color = "#96FED1";
			}else {
				box_color = "#FF5151";
			}
			
			header_content.put("backgroundColor", "#9FD8E36E");		
			header_content.put("height", "6px");		
			header_content.put("margin", "sm");	
			
			header_contents.put(header_content);
			
			
			
			if(eachArticle.getStock_status() != 0) {
				
				header_content = new JSONObject();
				
				header_content.put("type", "text");		
				if(eachArticle.getAfter_stock_range() > 0) {
					header_content.put("text", "發文後五個交易日漲" + Math.abs(eachArticle.getAfter_stock_range()) + "%");
				}else if(eachArticle.getAfter_stock_range() < 0){
					header_content.put("text", "發文後五個交易日跌" + Math.abs(eachArticle.getAfter_stock_range()) + "%");
				}else {
					header_content.put("text", "發文後五個交易日平盤");
				}
				
				header_content.put("margin", "lg");
				header_content.put("size", "xl");
				header_content.put("align", "center");
				
				header_contents.put(header_content);
				header_content = new JSONObject();
				
				if(eachArticle.getAfter_stock_range() >= 0) {
					bar_color = "#CE0000";
				}else {
					bar_color = "#0D8186";
				}
				
				header_content.put("type", "box");		
				header_content.put("layout", "vertical");		
				header_content.put("contents", new JSONArray().put(new JSONObject()
						.put("type", "box")
						.put("layout", "vertical")
						.put("contents", new JSONArray().put(new JSONObject().put("type", "filler")))
						.put("width", Math.abs(eachArticle.getAfter_stock_range()) + "%")
						.put("height", "6px")
						.put("backgroundColor", bar_color)
						));		
				
				if(eachArticle.getType() == 0) {
					box_color = "#96FED1";
				}else {
					box_color = "#FF5151";
				}
				
				header_content.put("backgroundColor", "#9FD8E36E");		
				header_content.put("height", "6px");		
				header_content.put("margin", "sm");	
				
				header_contents.put(header_content);
				
			}else {
				header_content = new JSONObject();
				
				header_content.put("type", "text");		

				header_content.put("text", "發文後尚未有五個交易日");
				
				
				header_content.put("margin", "lg");
				header_content.put("size", "xl");
				header_content.put("align", "center");
				
				header_contents.put(header_content);
				header_content = new JSONObject();
				

				bar_color = "#0D8186";
				
				header_content.put("type", "box");		
				header_content.put("layout", "vertical");		
				header_content.put("contents", new JSONArray().put(new JSONObject()
						.put("type", "box")
						.put("layout", "vertical")
						.put("contents", new JSONArray().put(new JSONObject().put("type", "filler")))
						.put("width", "0%")
						.put("height", "6px")
						.put("backgroundColor", bar_color)
						));		
				
				if(eachArticle.getType() == 0) {
					box_color = "#96FED1";
				}else {
					box_color = "#FF5151";
				}
				
				header_content.put("backgroundColor", "#9FD8E36E");		
				header_content.put("height", "6px");		
				header_content.put("margin", "sm");	
				
				header_contents.put(header_content);
			}
			

			bubble_header.put("contents", header_contents);
			bubble_header.put("paddingAll", "12px");
			bubble_header.put("backgroundColor", box_color);
			bubble_header.put("paddingTop", "10px");
			bubble_header.put("paddingBottom", "16px");
			
			bubble.put("header", bubble_header);
			
			// id 編碼
			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(eachArticle.getId()).getBytes());
			
						
			bubble_body.put("type", "box");
			bubble_body.put("layout", "vertical");
			bubble_body.put("contents", new JSONArray()
					.put(new JSONObject()
							.put("type", "box")
							.put("layout", "vertical")
							.put("contents", new JSONArray().put(new JSONObject()
								.put("type", "text")
								.put("contents",  new JSONArray())
								.put("size",  "xl")
								.put("wrap", true)
								.put("text", eachArticle.getTitle())
								.put("color", "#ffffff")
								.put("weight", "bold")
								.put("style", "normal")
								.put("maxLines", 1)
							))
							.put("spacing", "sm")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "uri")
									.put("label", "查看原文")
									.put("uri", eachArticle.getUrl())
							)
							.put("style", "secondary")
							.put("offsetTop", "xs")
							.put("offsetBottom", "xs")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "message")
									.put("label", "查看相關資訊")
									.put("text", "股板標的 搜尋文章 " + code)							
							)
							.put("style", "secondary")
							.put("offsetTop", "lg")
							.put("offsetBottom", "lg")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "uri")
									.put("label", "查看股市同學會資料")
									.put("uri", "https://www.cmoney.tw/forum/stock/" + eachArticle.getStock_no())							
							)
							.put("style", "secondary")
							.put("offsetTop", "25px")
//							.put("offsetBottom", "lg")
					)
			);
			bubble_body.put("paddingAll", "20px");
			bubble_body.put("backgroundColor", "#464F69");
			bubble_body.put("height", "250px");

			bubble.put("body", bubble_body);
			
			carousel_bubbles.put(bubble);
			
			bubble_header = new JSONObject();
			header_contents = new JSONArray();
			header_content = new JSONObject();
			bubble_body = new JSONObject();
			bubble = new JSONObject();
			
		}		
		
		carousel.put("contents", carousel_bubbles);
		
		message = new JSONObject();
		message.put("type", "flex");
		message.put("altText", "作者:" + stockAuthor.getAuthor() + " 所有標的文章");
		message.put("contents", carousel);
		messages.put(message);

	}

	
	// 回復用戶呼叫股板標的指令
	public void replyLatestArticle(JSONObject userObject, JSONArray messages, JSONObject message, List<StockArticle> articleList) {
		
		message.put("type", "text");
		try {
			message.put("text", userObject.get("displayName") + "你好: " + articleList.get(0).getArticle_meta_date() + " 標的文章共有"
					+ articleList.size() + "篇");
		} catch (JSONException je) {
			message.put("text", "你好: 因為LINE API傳送資料出現問題 請再重新輸入");
			messages.put(message);

			return;
		}
		messages.put(message);
		
		JSONObject template = new JSONObject();
		JSONObject carousel = new JSONObject();
		JSONObject bubble = new JSONObject();
		
		JSONObject hero = new JSONObject();
		JSONObject body = new JSONObject();
		JSONArray body_content = new JSONArray();
		JSONObject body_content_text = new JSONObject();
		JSONObject body_content_button = new JSONObject();
		JSONObject body_content_button_action = new JSONObject();
		JSONArray carousel_bubbles = new JSONArray();
	
		template.put("type", "carousel");
		carousel.put("type", "carousel");

		
		StockArticle previousArticle = stockArticleDao.getPreviousArticle(articleList.get(0));
		StockArticle nextArticle = stockArticleDao.getNextArticle(articleList.get(articleList.size() - 1));
		
		if (previousArticle.getId() != null) {

			previousArticle = stockArticleDao.getArticleById(previousArticle);

			bubble.put("type", "bubble");

			hero.put("type", "image");
			hero.put("url", ngrok_link + "/images/TOOL/toLEFT.png");
			hero.put("size", "full");
			hero.put("aspectMode", "cover");
			hero.put("aspectRatio", "20:15");

			bubble.put("hero", hero);

			body.put("type", "box");
			body.put("layout", "vertical");

			body_content_button.put("type", "button");
			body_content_button.put("gravity", "center");
			body_content_button.put("style", "primary");
			body_content_button.put("offsetTop", "30px");

			body_content_button_action.put("type", "message");
			body_content_button_action.put("label", "查看前一天文章");
			body_content_button_action.put("text", "股板標的 搜尋文章 日期 " + previousArticle.getArticle_meta_date());

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body.put("contents", body_content);

			bubble.put("body", body);

			carousel_bubbles.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			body = new JSONObject();
			body_content = new JSONArray();
			body_content_text = new JSONObject();
			body_content_button = new JSONObject();
			body_content_button_action = new JSONObject();

		}
		
		JSONObject bubble_header = new JSONObject();
		JSONArray header_contents = new JSONArray();
		JSONObject header_content = new JSONObject();
		JSONObject bubble_body = new JSONObject();
		String bar_color;
		String box_color;
		String code;

		for(StockArticle eachArticle : articleList) {

			// 條顏色      紅#CE0000  綠#0D8186
			// box顏色  紅#FF5151  綠#96FED1
			
			bubble.put("type", "bubble");
			
			bubble_header = new JSONObject();
			
			bubble_header.put("type", "box");
			bubble_header.put( "layout", "vertical");
			
			header_content.put("type", "text");
			header_content.put("text", eachArticle.getLine_title());

			header_content.put("size", "3xl");
			header_content.put("align", "center");

			header_contents.put(header_content);
			header_content = new JSONObject();
			
			header_content.put("type", "text");
			header_content.put("text", "發文者:" + eachArticle.getAuthor_name());

			header_content.put("size", "xl");
			header_content.put("align", "center");

			header_contents.put(header_content);
			header_content = new JSONObject();
			
			header_content.put("type", "text");		
			if(eachArticle.getStock_range() > 0) {
				header_content.put("text", "發文前五個交易日漲" + Math.abs(eachArticle.getStock_range()) + "%");
			}else if(eachArticle.getStock_range() < 0){
				header_content.put("text", "發文前五個交易日跌" + Math.abs(eachArticle.getStock_range()) + "%");
			}else {
				header_content.put("text", "發文前五個交易日平盤");
			}
			
			header_content.put("margin", "lg");
			header_content.put("size", "xl");
			header_content.put("align", "center");
			
			header_contents.put(header_content);
			header_content = new JSONObject();
			
			if(eachArticle.getStock_range() >= 0) {
				bar_color = "#CE0000";
			}else {
				bar_color = "#0D8186";
			}
			
			header_content.put("type", "box");		
			header_content.put("layout", "vertical");		
			header_content.put("contents", new JSONArray().put(new JSONObject()
					.put("type", "box")
					.put("layout", "vertical")
					.put("contents", new JSONArray().put(new JSONObject().put("type", "filler")))
					.put("width", Math.abs(eachArticle.getStock_range()) + "%")
					.put("height", "6px")
					.put("backgroundColor", bar_color)
					));		
			
			if(eachArticle.getType() == 0) {
				box_color = "#96FED1";
			}else {
				box_color = "#FF5151";
			}
			
			header_content.put("backgroundColor", "#9FD8E36E");		
			header_content.put("height", "6px");		
			header_content.put("margin", "sm");	
			
			header_contents.put(header_content);
			
			
			
			if(eachArticle.getStock_status() != 0) {
				
				header_content = new JSONObject();
				
				header_content.put("type", "text");		
				if(eachArticle.getAfter_stock_range() > 0) {
					header_content.put("text", "發文後五個交易日漲" + Math.abs(eachArticle.getAfter_stock_range()) + "%");
				}else if(eachArticle.getAfter_stock_range() < 0){
					header_content.put("text", "發文後五個交易日跌" + Math.abs(eachArticle.getAfter_stock_range()) + "%");
				}else {
					header_content.put("text", "發文後五個交易日平盤");
				}
				
				header_content.put("margin", "lg");
				header_content.put("size", "xl");
				header_content.put("align", "center");
				
				header_contents.put(header_content);
				header_content = new JSONObject();
				
				if(eachArticle.getAfter_stock_range() >= 0) {
					bar_color = "#CE0000";
				}else {
					bar_color = "#0D8186";
				}
				
				header_content.put("type", "box");		
				header_content.put("layout", "vertical");		
				header_content.put("contents", new JSONArray().put(new JSONObject()
						.put("type", "box")
						.put("layout", "vertical")
						.put("contents", new JSONArray().put(new JSONObject().put("type", "filler")))
						.put("width", Math.abs(eachArticle.getAfter_stock_range()) + "%")
						.put("height", "6px")
						.put("backgroundColor", bar_color)
						));		
				
				if(eachArticle.getType() == 0) {
					box_color = "#96FED1";
				}else {
					box_color = "#FF5151";
				}
				
				header_content.put("backgroundColor", "#9FD8E36E");		
				header_content.put("height", "6px");		
				header_content.put("margin", "sm");	
				
				header_contents.put(header_content);
			}
			

			bubble_header.put("contents", header_contents);
			bubble_header.put("paddingAll", "12px");
			bubble_header.put("backgroundColor", box_color);
			bubble_header.put("paddingTop", "10px");
			bubble_header.put("paddingBottom", "16px");
			
			bubble.put("header", bubble_header);
			
			// id 編碼
			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(eachArticle.getId()).getBytes());
			
						
			bubble_body.put("type", "box");
			bubble_body.put("layout", "vertical");
			bubble_body.put("contents", new JSONArray()
					.put(new JSONObject()
							.put("type", "box")
							.put("layout", "vertical")
							.put("contents", new JSONArray().put(new JSONObject()
								.put("type", "text")
								.put("contents",  new JSONArray())
								.put("size",  "xl")
								.put("wrap", true)
								.put("text", eachArticle.getTitle())
								.put("color", "#ffffff")
								.put("weight", "bold")
								.put("style", "normal")
								.put("maxLines", 1)
							))
							.put("spacing", "sm")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "uri")
									.put("label", "查看原文")
									.put("uri", eachArticle.getUrl())
							)
							.put("style", "secondary")
							.put("offsetTop", "xs")
							.put("offsetBottom", "xs")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "message")
									.put("label", "查看相關資訊")
									.put("text", "股板標的 搜尋文章 " + code + " 範圍 " + java.util.Base64.getEncoder().encodeToString("0~10".getBytes()))							
							)
							.put("style", "secondary")
							.put("offsetTop", "lg")
							.put("offsetBottom", "lg")
					)
					.put(new JSONObject()
							.put("type", "button")
							.put("action", new JSONObject()
									.put("type", "uri")
									.put("label", "查看股市同學會資料")
									.put("uri", "https://www.cmoney.tw/forum/stock/" + eachArticle.getStock_no())							
							)
							.put("style", "secondary")
							.put("offsetTop", "25px")
//							.put("offsetBottom", "lg")
					)
			);
			bubble_body.put("paddingAll", "20px");
			bubble_body.put("backgroundColor", "#464F69");
			bubble_body.put("height", "250px");

			bubble.put("body", bubble_body);
			
			carousel_bubbles.put(bubble);
			
			bubble_header = new JSONObject();
			header_contents = new JSONArray();
			header_content = new JSONObject();
			bubble_body = new JSONObject();
			bubble = new JSONObject();
			
		}		

		
		if (nextArticle.getId() != null) {

			nextArticle = stockArticleDao.getArticleById(nextArticle);

			bubble.put("type", "bubble");

			hero.put("type", "image");
			hero.put("url", ngrok_link + "/images/TOOL/toRIGHT.png");
			hero.put("size", "full");
			hero.put("aspectMode", "cover");
			hero.put("aspectRatio", "20:15");

			bubble.put("hero", hero);

			body.put("type", "box");
			body.put("layout", "vertical");

			body_content_button.put("type", "button");
			body_content_button.put("gravity", "center");
			body_content_button.put("style", "primary");
			body_content_button.put("offsetTop", "30px");

			body_content_button_action.put("type", "message");
			body_content_button_action.put("label", "查看下一天文章");
			body_content_button_action.put("text", "股板標的 搜尋文章 日期 " + nextArticle.getArticle_meta_date());

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body.put("contents", body_content);

			bubble.put("body", body);

			carousel_bubbles.put(bubble);
		}

		
		carousel.put("contents", carousel_bubbles);
		


		message = new JSONObject();
		message.put("type", "flex");
		message.put("altText", articleList.get(0).getArticle_meta_date() + " 股版標的文章");
		message.put("contents", carousel);
		messages.put(message);
		
	}

	public JSONObject getUser(String user_id) {

		JSONObject userObject = new JSONObject();

		Request request = new Request.Builder().url("https://api.line.me/v2/bot/profile/" + user_id)
				.header("Authorization", "Bearer {" + LINE_TOKEN + "}").build();

		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				System.err.println(e);

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// user回傳格式 {"userId":"","":"","pictureUrl":"","language":""}
				JSONObject replyObject = new JSONObject(response.body().string());
				userObject.put("displayName", replyObject.get("displayName").toString());
			}

		});

		synchronized (userObject) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		return userObject;
	}

	public void sendMessage(JSONObject replyBody) {
		Request request = new Request.Builder().url("https://api.line.me/v2/bot/message/reply")
				.header("Authorization", "Bearer {" + LINE_TOKEN + "}").post(okhttp3.RequestBody
						.create(MediaType.parse("application/json; charset=utf-8"), replyBody.toString()))
				.build();

		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				System.err.println(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				System.out.println(response.body().string());
			}

		});

	}

}
