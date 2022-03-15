package com.sideproject.PttSearch.Dao;

import java.util.List;

import com.sideproject.PttSearch.Model.PttArticle;

public interface PttArticleDao {
	
	public PttArticle add(PttArticle pttArticle);
	
	public void updateQuantity(PttArticle pttArticle);
		
	public PttArticle getArticleById(PttArticle pttArticle);
	
	public PttArticle getArticleByNewTitle(PttArticle pttArticle);
	
	public PttArticle getPreviousArticle(PttArticle pttArticle);
	
	public PttArticle getNextArticle(PttArticle pttArticle);

	public List<PttArticle> getArticleByDate(PttArticle pttArticle);
	
	public List<PttArticle> getLatestArticle(PttArticle pttArticle);

	public List<PttArticle> getArticleByPush(PttArticle pttArticle);

}
