package com.sideproject.PttSearch.Dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.sideproject.PttSearch.Dao.BeautyImageDao;
import com.sideproject.PttSearch.Model.BeautyImage;

@Repository
public class BeautyImageDaoImpl implements BeautyImageDao {
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public void add(BeautyImage beautyImage) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(beautyImage);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.beauty_image "
				   + " (ARTICLE_ID,URL,TYPE,CREATE_DATE) "
				   + " VALUES (:article_id,:url,:type,NOW()) ";
		
		jdbcTemplate.update(sql, sqlParameterSource);


	}

	@Override
	public BeautyImage getImageById(BeautyImage beautyImage) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(beautyImage);
		
		RowMapper<BeautyImage> rowMapper = new BeanPropertyRowMapper<BeautyImage>(BeautyImage.class);

		String sql = "SELECT * FROM heroku_55771e5bfc7407d.beauty_image WHERE ID = :id ";

		return jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public List<BeautyImage> getImageByArticle_id(BeautyImage beautyImage) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(beautyImage);
		
		BeanPropertyRowMapper<BeautyImage> rowMapper = new BeanPropertyRowMapper<BeautyImage>(BeautyImage.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.beauty_image WHERE ARTICLE_ID = :article_id ";
		
		if(beautyImage.getStart() != null && beautyImage.getEnd() != null) {
			sql += " ORDER BY ID LIMIT :start , :end ";
		}

		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

}
