package com.sideproject.PttSearch.Dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.sideproject.PttSearch.Dao.PttArticleDao;
import com.sideproject.PttSearch.Model.PttArticle;

@Repository
public class PttArticleDaoImpl implements PttArticleDao {
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public PttArticle add(PttArticle pttArticle) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.ptt_article "
				   + " (TITLE,NEW_TITLE,URL,TYPE,PUSH,POST_DATE,ARTICLE_META_DATE,CREATE_DATE) "
				   + " VALUES (:title,:new_title,:url,:type,:push,:post_date,:article_meta_date,NOW())";
		
		jdbcTemplate.update(sql, sqlParameterSource ,keyHolder);
		
		pttArticle.setId(keyHolder.getKey().intValue());
		
		return pttArticle;
	}

	@Override
	public void updateQuantity(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(pttArticle);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.ptt_article "
				   + " SET QUANTITY = :quantity where ID = :id ";
		
		jdbcTemplate.update(sql, sqlParameterSource);

	}

	@Override
	public PttArticle getArticleById(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		RowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);

		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE ID = :id ";

		return jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public PttArticle getArticleByNewTitle(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		RowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);
		
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE NEW_TITLE = :new_title ";
		
		
		return jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
	}
	
	@Override
	public PttArticle getPreviousArticle(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		RowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);
				
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE ARTICLE_META_DATE < :article_meta_date ORDER BY ID DESC LIMIT 1 ";		
		
		try {
			pttArticle = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			pttArticle = new PttArticle();
		}
		
		return pttArticle;
	}

	@Override
	public PttArticle getNextArticle(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		RowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);
		
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE NEW_TITLE > :new_title ORDER BY POST_DATE, ID LIMIT 1";
		
		try {
			
			pttArticle = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			pttArticle = new PttArticle();
		}
		
		return pttArticle;
	}

	@Override
	public List<PttArticle> getLatestArticle(PttArticle pttArticle) {
		BeanPropertyRowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.ptt_article "
				   + " WHERE ARTICLE_META_DATE = ( "
				   + " SELECT ARTICLE_META_DATE FROM heroku_55771e5bfc7407d.ptt_article WHERE ID =  "
				   + " (SELECT MAX(ID) FROM heroku_55771e5bfc7407d.ptt_article) ) ORDER BY ID ";
		
		return jdbcTemplate.query(sql, rowMapper);
	}

	@Override
	public List<PttArticle> getArticleByDate(PttArticle pttArticle) {
				
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		BeanPropertyRowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);

		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE ARTICLE_META_DATE = :article_meta_date ORDER BY POST_DATE, ID ";

		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public List<PttArticle> getArticleByPush(PttArticle pttArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(pttArticle);
		
		BeanPropertyRowMapper<PttArticle> rowMapper = new BeanPropertyRowMapper<PttArticle>(PttArticle.class);
		
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.ptt_article WHERE PUSH >= :push";

		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

}
