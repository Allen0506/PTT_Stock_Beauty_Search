package com.sideproject.PttSearch.Service.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sideproject.PttSearch.Service.ToolService;

@Component
public class ToolServiceImpl implements ToolService {

	@Override
	public String getTodayStr() {
		Date today = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd");
		return format.format(today);	
	}

	@Override
	public String getYesterdayStr(String format) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String yesterday = new SimpleDateFormat(format).format(cal.getTime());
		return yesterday;
	}

	@Override
	public String changeDateFormat(String date, String past_formal, String new_formal) {
		
		Date new_date = null;
		
		try {
			new_date = new SimpleDateFormat(past_formal,Locale.ENGLISH).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}


		return new SimpleDateFormat(new_formal,Locale.ENGLISH).format(new_date);
	}

	@Override
	public void downLoadImage(URL url, String imageName) {
		
		try {		
			DataInputStream dis = new DataInputStream(url.openStream());
			FileOutputStream fos = new FileOutputStream(new File(imageName));

			byte[] buffer = new byte[1024];
			int length;
			while ((length = dis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
			fos.close();
			dis.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String extractNumber(String str) {
		
	    if(str == null || str.isEmpty()) return "";
	    
	    StringBuilder sb = new StringBuilder();
	    boolean found = false;
	    for(char c : str.toCharArray()){
	        if(Character.isDigit(c)){
	            sb.append(c);
	            found = true;
	        } else if(found){
	            break;                
	        }
	    }
	    
	    return sb.toString();
	}

	@Override
	public Double countStockRange(Double FirstOpen, Double lastClose) {
		
		Double result =  new BigDecimal(Double.toString(FirstOpen)).subtract( new BigDecimal(Double.toString(lastClose))).doubleValue();

		BigDecimal result2 = new BigDecimal(Double.toString(result)).divide(new BigDecimal(Double.toString(FirstOpen)),MathContext.DECIMAL32);
				
	    String str = new DecimalFormat("0.0").format(result2.multiply(new BigDecimal(Double.toString(100))).doubleValue());  

		return Double.valueOf(str);
	}

}
