package com.sideproject.PttSearch.Dao;

import java.util.List;

import com.sideproject.PttSearch.Model.StockAuthor;

public interface StockAuthorDao {
	
	public StockAuthor add(StockAuthor stockAuthor);
	
	public void updateWinRate(StockAuthor stockAuthor);
	
	public StockAuthor getAuthorById(StockAuthor stockAuthor);

	public StockAuthor getAuthorByName(StockAuthor stockAuthor);
	
	public List<StockAuthor> getAllAuthor(StockAuthor stockAuthor);

}
