package com.sideproject.PttSearch.Controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sideproject.PttSearch.Service.impl.BeautyLineServiceImpl;
import com.sideproject.PttSearch.Service.impl.StockLineServiceImpl;

@RestController
public class IndexController {

	@Value("${imgur_client_id}")
	private String IMGUR_CLIENT_ID;

	@Value("${imgur_album}")
	private String IMGUR_ALBUM;

	@Autowired
	private BeautyLineServiceImpl beautyLineService;

	@Autowired
	private StockLineServiceImpl stockLineService;

	@RequestMapping(value = "/line" , method = {RequestMethod.POST})
	public void line(@RequestBody String requestBody) {

		JSONObject object = new JSONObject(requestBody);

		try {

			String text = ((JSONObject) object.getJSONArray("events").get(0)).getJSONObject("message").get("text")
					.toString();

			if (text.contains("表特")) {
				beautyLineService.replyLine(object);
			}

			if (text.contains("股板標的")) {
				stockLineService.replyLine(object);
			}

		} catch (JSONException je) {

		}

	}

}
