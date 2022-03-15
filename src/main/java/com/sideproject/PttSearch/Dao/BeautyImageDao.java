package com.sideproject.PttSearch.Dao;

import java.util.List;

import com.sideproject.PttSearch.Model.BeautyImage;

public interface BeautyImageDao {
	
	public void add(BeautyImage beautyImage);
	
	public BeautyImage getImageById(BeautyImage beautyImage);
	
	public List<BeautyImage> getImageByArticle_id(BeautyImage beautyImage);
	
}
