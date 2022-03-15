package com.sideproject.PttSearch.Service;

import java.net.URL;

public interface ToolService {
	
	public String getTodayStr();
	
	public String getYesterdayStr(String format);

	public String changeDateFormat(String date, String past_formal, String new_formal);

	public void downLoadImage(URL url, String imageName);
	
	public String extractNumber(String str);          
	
	public Double countStockRange(Double FirstOpen, Double lastClose);          


}
