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

import com.sideproject.PttSearch.Dao.StockArticleDao;
import com.sideproject.PttSearch.Model.Stock;
import com.sideproject.PttSearch.Model.StockArticle;
import com.sideproject.PttSearch.Model.StockChart;

@Repository
public class StockArticleDaoImpl implements StockArticleDao {
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public StockArticle add(StockArticle stockArticle) {
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.stock_article "
				   + " (STOCK_NO, TITLE, AUTHOR_ID, URL, TYPE, ARTICLE_META_DATE, CREATE_DATE) "
				   + " VALUES (:stock_no, :title, :author_id, :url, :type, :article_meta_date, NOW())";
		
		jdbcTemplate.update(sql, sqlParameterSource ,keyHolder);
		
		stockArticle.setId(keyHolder.getKey().intValue());
		
		return stockArticle;
		
	}

	@Override
	public void delete(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " DELETE FROM heroku_55771e5bfc7407d.stock_article WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);
	}

	@Override
	public void updateLineTitle(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.stock_article "
				   + " SET LINE_TITLE = :line_title WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);

	}

	@Override
	public void updateRange(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.stock_article "
				   + " SET STOCK_RANGE = :stock_range WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);
	}

	@Override
	public void updateAfterRange(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.stock_article "
				   + " SET AFTER_STOCK_RANGE = :after_stock_range, STOCK_STATUS = :stock_status, RESULT = :result "
				   + " WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);
	}

	@Override
	public void updateAuthorId(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.stock_article "
				   + " SET AUTHOR_ID = :author_id WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);		
	}

	@Override
	public StockChart getStockChartByArticleId(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);	
		RowMapper<StockChart> rowMapper = new BeanPropertyRowMapper<StockChart>(StockChart.class);
		
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.stock_chart WHERE STOCK_ARTICLE_ID = :id ";
		
		StockChart stockChart = new StockChart();
		try {
			
			stockChart = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			
		}

		return stockChart;
	}

	@Override
	public StockArticle getArticleById(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		RowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);

		String sql = " SELECT article.*,author.AUTHOR as AUTHOR_NAME "
				   + " FROM heroku_55771e5bfc7407d.stock_article as article, heroku_55771e5bfc7407d.stock_author as author "
				   + " WHERE article.AUTHOR_ID = author.ID AND article.ID = :id ";	

		return jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public StockArticle getPreviousArticle(StockArticle stockArticle) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		RowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = "SELECT * FROM heroku_55771e5bfc7407d.stock_article WHERE ARTICLE_META_DATE < :article_meta_date "
				   + " ORDER BY ARTICLE_META_DATE DESC, ID LIMIT 1 ";		
		
		try {
			stockArticle = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			stockArticle = new StockArticle();
		}
		
		return stockArticle;
	}



	@Override
	public StockArticle getNextArticle(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		RowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_article WHERE ARTICLE_META_DATE > :article_meta_date "
				   + " ORDER BY ARTICLE_META_DATE, ID LIMIT 1 ";
		
		try {
			
			stockArticle = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			stockArticle = new StockArticle();
		}
		
		return stockArticle;
	}



	@Override
	public List<StockArticle> getStockArticleByPostDate(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		RowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT article.*,author.AUTHOR as AUTHOR_NAME "
				   + " FROM heroku_55771e5bfc7407d.stock_article as article, heroku_55771e5bfc7407d.stock_author as author "
				   + " WHERE ARTICLE_META_DATE = :article_meta_date AND article.AUTHOR_ID = author.ID ";
		
		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public List<StockArticle> getLatestArticle(StockArticle stockArticle) {
		
		BeanPropertyRowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT article.*,author.AUTHOR as AUTHOR_NAME "
				   + " FROM heroku_55771e5bfc7407d.stock_article as article, heroku_55771e5bfc7407d.stock_author as author "
				   + " WHERE ARTICLE_META_DATE = ( "
				   + " SELECT ARTICLE_META_DATE FROM heroku_55771e5bfc7407d.stock_article WHERE ID =  "
				   + " (SELECT MAX(ID) FROM heroku_55771e5bfc7407d.stock_article) ) AND article.AUTHOR_ID = author.ID ORDER BY ID ";
		
		return jdbcTemplate.query(sql, rowMapper);
	}

	@Override
	public List<Stock> getStockDataByStock_article_id(StockArticle stockArticle) {
				
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);
		
		BeanPropertyRowMapper<Stock> rowMapper = new BeanPropertyRowMapper<Stock>(Stock.class);

		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock WHERE STOCK_ARTICLE_ID = :id ORDER BY ID";	
		
		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public List<StockArticle> getAllStockArticle(StockArticle stockArticle) {
		BeanPropertyRowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_article ";

		return jdbcTemplate.query(sql, rowMapper);
	}

	@Override
	public List<StockArticle> getStockArticleByAuthorId(StockArticle stockArticle) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockArticle);

		BeanPropertyRowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_article where AUTHOR_ID = :author_id ";
		
		if(stockArticle.getStart() != null && stockArticle.getEnd() != null) {
			sql += " ORDER BY ID LIMIT :start , :end ";
		}

		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public List<StockArticle> getStockArticleHaveNoResult(StockArticle stockArticle) {
		BeanPropertyRowMapper<StockArticle> rowMapper = new BeanPropertyRowMapper<StockArticle>(StockArticle.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_article WHERE AFTER_STOCK_RANGE is null ";

		return jdbcTemplate.query(sql, rowMapper);
	}
	
}
