package com.sideproject.PttSearch.Dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.sideproject.PttSearch.Dao.StockDao;
import com.sideproject.PttSearch.Model.Stock;

@Repository
public class StockDaoImpl implements StockDao {
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public void add(Stock stock) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stock);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.stock "
				   + " (STOCK_NO, STOCK_ARTICLE_ID, DATE, OPEN, HIGH, LOW, CLOSE, ADJ_CLOSE, VOLUME, CREATE_DATE) "
				   + " VALUES (:stock_no, :stock_article_id, :date, :open, :high, :low, :close, :adj_close, :volume, NOW()) ";		
		
		jdbcTemplate.update(sql, sqlParameterSource);

	}

}
