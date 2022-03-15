package com.sideproject.PttSearch.Dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.sideproject.PttSearch.Dao.StockChartDao;
import com.sideproject.PttSearch.Model.StockChart;

@Repository
public class StockChartDaoImpl implements StockChartDao {
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public void add(StockChart stockChart) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockChart);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.stock_chart "
				   + " (STOCK_NO, STOCK_ARTICLE_ID, URL, DELETEHASH, CREATE_DATE) "
				   + " VALUES (:stock_no, :stock_article_id, :url, :deletehash, NOW()) ";		
		
		jdbcTemplate.update(sql, sqlParameterSource);
	}

	@Override
	public List<StockChart> getStockChartByStock_article_id(StockChart stockChart) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockChart);
		
		BeanPropertyRowMapper<StockChart> rowMapper = new BeanPropertyRowMapper<StockChart>(StockChart.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_chart WHERE STOCK_ARTICLE_ID = :stock_article_id ";

		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

}
