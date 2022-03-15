package com.sideproject.PttSearch.Service.impl;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Dao.BeautyImageDao;
import com.sideproject.PttSearch.Dao.PttArticleDao;
import com.sideproject.PttSearch.Model.BeautyImage;
import com.sideproject.PttSearch.Model.PttArticle;
import com.sideproject.PttSearch.Service.BeautyLineService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class BeautyLineServiceImpl implements BeautyLineService {

	@Value("${line.bot.channel-token}")
	private String LINE_TOKEN;

	@Value("${ngrok_link}")
	private String ngrok_link;

	@Autowired
	private PttArticleDao pttArticleDao;

	@Autowired
	private BeautyImageDao beautyImageDao;

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

		PttArticle pttArticle = new PttArticle();
		BeautyImage beautyImage;

		String[] userText = text.split(" ");

		if (userText.length == 1 && userText[0].equals("表特")) {
			
			List<PttArticle> articleList = pttArticleDao.getLatestArticle(pttArticle);

			replyPttBeauty(userObject, messages, message, articleList);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);

			// 搜尋特定文章 顯示內文圖片 USER傳送訊息格式:表特 搜尋文章 編碼後id 範圍 n-n+9
		} else if (userText.length == 5 && userText[0].equals("表特") && userText[1].equals("搜尋文章")
				&& userText[3].equals("範圍")) {

			replySearchPttBeautyArticle(userObject, messages, message, userText);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);

			// 搜尋特定日期表特文章 USER傳送訊息格式: 表特 搜尋文章 日期 yyyyMMdd
		} else if (userText.length == 4 && userText[0].equals("表特") && userText[1].equals("搜尋文章")
				&& userText[2].equals("日期")) {

			pttArticle.setArticle_meta_date(userText[3]);
			
			List<PttArticle> articleList = pttArticleDao.getArticleByDate(pttArticle);

			replyPttBeauty(userObject, messages, message, articleList);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);

			// 顯示單張圖片 USER傳送訊息格式: 表特 搜尋圖片 id
		} else if (userText.length == 3 && userText[0].equals("表特") && userText[1].equals("搜尋圖片")) {
			String id;
			try {
				id = new String(java.util.Base64.getDecoder().decode(userText[2]));
			} catch (Exception e) {
				message = new JSONObject();
				message.put("type", "text");
				message.put("text", "你好: 語法有誤請重新輸入或重新點擊文章跑馬燈的圖片");
				messages.put(message);

				replyBody.put("replyToken", replyToken);
				replyBody.put("messages", messages);

				sendMessage(replyBody);
				return;
			}
			
			beautyImage = new BeautyImage();
			beautyImage.setId(Integer.valueOf(id));

			beautyImage = beautyImageDao.getImageById(beautyImage);

			message.put("type", "image");
			if (beautyImage.getType() == 0) {
				message.put("originalContentUrl", beautyImage.getUrl());
				message.put("previewImageUrl", beautyImage.getUrl());

			} else if (beautyImage.getType() == 1) {
				message.put("originalContentUrl", ngrok_link + "/images/" + beautyImage.getUrl());
				message.put("previewImageUrl", ngrok_link + "/images/" + beautyImage.getUrl());
			} else {
				message = new JSONObject();
				message.put("type", "text");
				message.put("text", "你好: 語法有誤請重新輸入或重新點擊文章跑馬燈的圖片");

			}
			messages.put(message);

			replyBody.put("replyToken", replyToken);
			replyBody.put("messages", messages);

			sendMessage(replyBody);

		} 

	}

	// 回復用戶呼叫表特指令
	public void replyPttBeauty(JSONObject userObject, JSONArray messages, JSONObject message, List<PttArticle> articleList) {

		message.put("type", "text");
		try {
			message.put("text", userObject.get("displayName") + "你好: " + articleList.get(0).getArticle_meta_date() + "表特30+文章共有"
					+ articleList.size() + "篇");
		} catch (JSONException je) {
			message.put("text", "你好: 因為LINE API傳送資料出現問題 請再重新輸入");
			messages.put(message);

			return;
		}
		messages.put(message);

		JSONObject template = new JSONObject();
		JSONArray content = new JSONArray();
		JSONObject bubble = new JSONObject();
		JSONObject hero = new JSONObject();
		JSONObject body = new JSONObject();
		JSONArray body_content = new JSONArray();
		JSONObject body_content_text = new JSONObject();
		JSONObject body_content_button = new JSONObject();
		JSONObject body_content_button_action = new JSONObject();

		template.put("type", "carousel");

		PttArticle previousArticle = pttArticleDao.getPreviousArticle(articleList.get(0));
		PttArticle nextArticle = pttArticleDao.getNextArticle(articleList.get(articleList.size() - 1));

		if (previousArticle.getId() != null) {

			previousArticle = pttArticleDao.getArticleById(previousArticle);

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
			body_content_button.put("offsetTop", "50px");

			body_content_button_action.put("type", "message");
			body_content_button_action.put("label", "查看前一天文章");
			body_content_button_action.put("text", "表特 搜尋文章 日期 " + previousArticle.getArticle_meta_date());

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body.put("contents", body_content);

			bubble.put("body", body);

			content.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			body = new JSONObject();
			body_content = new JSONArray();
			body_content_text = new JSONObject();
			body_content_button = new JSONObject();
			body_content_button_action = new JSONObject();

		}

		BeautyImage beautyImage;
		String code;
		String range_code;

		for (PttArticle eachArticle : articleList) {

			beautyImage = new BeautyImage();

			beautyImage.setArticle_id(eachArticle.getId());

			beautyImage = beautyImageDao.getImageByArticle_id(beautyImage).get(0);

			bubble.put("type", "bubble");

			hero.put("type", "image");
			if (beautyImage.getType() == 0) {
				hero.put("url", beautyImage.getUrl());
			} else {
				hero.put("url", ngrok_link + "/images/" + beautyImage.getUrl());
			}
			hero.put("size", "full");
			hero.put("aspectMode", "cover");
			hero.put("aspectRatio", "20:15");

			bubble.put("hero", hero);

			body.put("type", "box");
			body.put("layout", "vertical");

			body_content_text.put("type", "text");
			body_content_text.put("text", eachArticle.getTitle());
			body_content_text.put("style", "normal");
			body_content_text.put("weight", "bold");
			body_content_text.put("align", "center");

			body_content.put(body_content_text);

			body_content_button.put("type", "button");
			body_content_button.put("style", "primary");
			body_content_button.put("offsetTop", "md");
			body_content_button.put("offsetEnd", "none");
			body_content_button.put("offsetBottom", "xs");

			body_content_button_action.put("type", "uri");
			body_content_button_action.put("label", "查看原文網址");
			body_content_button_action.put("uri", eachArticle.getUrl());

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body_content_button = new JSONObject();
			body_content_button_action = new JSONObject();

			body_content_button.put("type", "button");
			body_content_button.put("style", "secondary");
			body_content_button.put("offsetTop", "lg");

			body_content_button_action.put("type", "message");
			body_content_button_action.put("label", "查看所有圖片");

			// id 編碼
			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(eachArticle.getId()).getBytes());
			range_code = java.util.Base64.getEncoder().encodeToString("0~10".getBytes());

			body_content_button_action.put("text", "表特 搜尋文章 " + code + " 範圍 " + range_code);

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body.put("contents", body_content);

			bubble.put("body", body);

			content.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			body = new JSONObject();
			body_content = new JSONArray();
			body_content_text = new JSONObject();
			body_content_button = new JSONObject();
			body_content_button_action = new JSONObject();
		}

		if (nextArticle.getId() != null) {

			nextArticle = pttArticleDao.getArticleById(nextArticle);

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
			body_content_button.put("offsetTop", "50px");

			body_content_button_action.put("type", "message");
			body_content_button_action.put("label", "查看下一天文章");
			body_content_button_action.put("text", "表特 搜尋文章 日期 " + nextArticle.getArticle_meta_date());

			body_content_button.put("action", body_content_button_action);

			body_content.put(body_content_button);

			body.put("contents", body_content);

			bubble.put("body", body);

			content.put(bubble);

		}

		template.put("contents", content);

		message = new JSONObject();
		message.put("type", "flex");
		message.put("altText", articleList.get(0).getArticle_meta_date()+"表特版30推文以上文章");
		message.put("contents", template);
		messages.put(message);

		System.out.println("messages = "+messages.toString());
	}

	// 取得特定文章的所有圖片並做跑馬燈顯示
	public void replySearchPttBeautyArticle(JSONObject userObject, JSONArray messages, JSONObject message,
			String[] userText) {

		// 搜尋特定文章 顯示內文圖片 USER傳送訊息格式:表特 搜尋文章 編碼後id 範圍 n-n+9
		String[] range;
		try {
			range = new String(java.util.Base64.getDecoder().decode(userText[4])).split("~");

		} catch (Exception e) {
			message.put("type", "text");
			message.put("text", userObject.get("displayName") + "你好: 語法有誤請重新輸入或重新點擊文章跑馬燈的圖片");
			messages.put(message);
			return;
		}

		String id = new String(java.util.Base64.getDecoder().decode(userText[2]));

		PttArticle pttArticle = new PttArticle();
		BeautyImage beautyImage = new BeautyImage();

		pttArticle.setId(Integer.valueOf(id));

		pttArticle = pttArticleDao.getArticleById(pttArticle);

		beautyImage.setArticle_id(pttArticle.getId());
		beautyImage.setStart(Integer.valueOf(range[0]));
		beautyImage.setEnd(Integer.valueOf(range[1]));

		if (beautyImage.getStart() < 0 || beautyImage.getEnd() < 0) {
			message.put("type", "text");
			message.put("text", userObject.get("displayName") + "你好: 圖片範圍有誤請重新輸入或重新點擊文章跑馬燈的圖片");
			messages.put(message);
			return;
		}

		List<BeautyImage> imageList = beautyImageDao.getImageByArticle_id(beautyImage);

		message.put("type", "text");

		StringBuilder range_str;

		if (pttArticle.getQuantity() > 10) {
			range_str = new StringBuilder();
			range_str.append(userObject.get("displayName") + "你好: " + pttArticle.getTitle() + " 共有"
					+ pttArticle.getQuantity() + "張圖片");

			if (beautyImage.getStart() + 10 > pttArticle.getQuantity()) {
				range_str.append(" 以下是 第" + (beautyImage.getStart() + 1) + "張到第" + pttArticle.getQuantity() + "張");
			} else {
				range_str.append(" 以下是 第" + (beautyImage.getStart() + 1) + "張到第" + (beautyImage.getStart() + 10) + "張");
			}

			message.put("text", range_str.toString());

		} else {
			message.put("text", userObject.get("displayName") + "你好: " + pttArticle.getTitle() + " 共有"
					+ pttArticle.getQuantity() + "張圖片");

		}
		messages.put(message);

		JSONObject template = new JSONObject();
		JSONArray contents = new JSONArray();
		JSONObject bubble = new JSONObject();
		JSONObject hero = new JSONObject();
		JSONObject action = new JSONObject();

		String code;
		String range_code;

		template.put("type", "carousel");

		if (pttArticle.getQuantity() > 10 && beautyImage.getStart() != 0) {
			bubble.put("type", "bubble");

			hero.put("type", "image");
			hero.put("url", ngrok_link + "/images/TOOL/toLEFT.png");

			hero.put("aspectMode", "cover");
			hero.put("margin", "none");
			hero.put("size", "full");
			hero.put("aspectRatio", "20:20");

			bubble.put("hero", hero);

			action.put("type", "message");
			action.put("label", "action");

			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(pttArticle.getId()).getBytes());
			range_code = java.util.Base64.getEncoder()
					.encodeToString(((beautyImage.getStart() - 10) + "~10").getBytes());
			action.put("text", "表特 搜尋文章 " + code + " 範圍 " + range_code);

			bubble.put("action", action);

			contents.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			action = new JSONObject();

		}

		for (BeautyImage eachImage : imageList) {

			bubble.put("type", "bubble");

			hero.put("type", "image");

			if (eachImage.getType() == 0) {
				hero.put("url", eachImage.getUrl());
			} else {
				hero.put("url", ngrok_link + "/images/" + eachImage.getUrl());
			}

			hero.put("aspectMode", "cover");
			hero.put("margin", "none");
			hero.put("size", "full");
			hero.put("aspectRatio", "20:20");

			bubble.put("hero", hero);

			action.put("type", "message");
			action.put("label", "action");

			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(eachImage.getId()).getBytes());

			action.put("text", "表特 搜尋圖片 " + code);

			bubble.put("action", action);

			contents.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			action = new JSONObject();
		}

		if (pttArticle.getQuantity() > 10
//				&& pttArticle.getQuantity() - (pttArticle.getQuantity() % 10) != beautyImage.getStart()) {
				&& beautyImage.getStart() + imageList.size() != pttArticle.getQuantity()) {
			bubble.put("type", "bubble");

			hero.put("type", "image");
			hero.put("url", ngrok_link + "/images/TOOL/toRIGHT.png");

			hero.put("aspectMode", "cover");
			hero.put("margin", "none");
			hero.put("size", "full");
			hero.put("aspectRatio", "20:20");

			bubble.put("hero", hero);

			action.put("type", "message");
			action.put("label", "action");

			code = java.util.Base64.getEncoder().encodeToString(String.valueOf(pttArticle.getId()).getBytes());
			range_code = java.util.Base64.getEncoder()
					.encodeToString(((beautyImage.getStart() + 10) + "~10").getBytes());
			action.put("text", "表特 搜尋文章 " + code + " 範圍 " + range_code);

			bubble.put("action", action);

			contents.put(bubble);

			bubble = new JSONObject();
			hero = new JSONObject();
			action = new JSONObject();

		}

		template.put("contents", contents);

		message = new JSONObject();
		message.put("type", "flex");
		message.put("altText", pttArticle.getTitle() + " 所有圖片");
		message.put("contents", template);

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
