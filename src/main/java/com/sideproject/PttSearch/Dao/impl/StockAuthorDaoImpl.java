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

import com.sideproject.PttSearch.Dao.StockAuthorDao;
import com.sideproject.PttSearch.Model.StockAuthor;

@Repository
public class StockAuthorDaoImpl implements StockAuthorDao{
	
	@Autowired
	NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public StockAuthor add(StockAuthor stockAuthor) {
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockAuthor);
		
		String sql = " INSERT INTO heroku_55771e5bfc7407d.stock_author "
				   + " (AUTHOR,CREATE_DATE) "
				   + " VALUES (:author,NOW())";
		
		jdbcTemplate.update(sql, sqlParameterSource ,keyHolder);
		
		stockAuthor.setId(keyHolder.getKey().intValue());
		
		return stockAuthor;
	}

	@Override
	public void updateWinRate(StockAuthor stockAuthor) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockAuthor);
		
		String sql = " UPDATE heroku_55771e5bfc7407d.stock_author SET WIN_RATE =  "
				   + " (SELECT ( (SELECT COUNT(*) FROM heroku_55771e5bfc7407d.stock_article where AUTHOR_ID = :id and STOCK_STATUS = 1 and RESULT = 1) "
				   + "/(SELECT COUNT(*) FROM heroku_55771e5bfc7407d.stock_article where AUTHOR_ID = :id and STOCK_STATUS = 1) * 100)) " 
				   + " WHERE ID = :id ";	
		
		jdbcTemplate.update(sql, sqlParameterSource);		
	}

	@Override
	public StockAuthor getAuthorById(StockAuthor stockAuthor) {
		
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockAuthor);
		
		RowMapper<StockAuthor> rowMapper = new BeanPropertyRowMapper<StockAuthor>(StockAuthor.class);

		String sql = "SELECT * FROM heroku_55771e5bfc7407d.stock_author WHERE ID = :id ";

		return jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
	}

	@Override
	public StockAuthor getAuthorByName(StockAuthor stockAuthor) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockAuthor);
		
		RowMapper<StockAuthor> rowMapper = new BeanPropertyRowMapper<StockAuthor>(StockAuthor.class);

		String sql = "SELECT * FROM heroku_55771e5bfc7407d.stock_author WHERE AUTHOR = :author ";
		
		try {
			stockAuthor = jdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper);
		}catch(EmptyResultDataAccessException ee) {
			
		}

		return stockAuthor;
	}

	@Override
	public List<StockAuthor> getAllAuthor(StockAuthor stockAuthor) {
		SqlParameterSource sqlParameterSource=new BeanPropertySqlParameterSource(stockAuthor);
		
		RowMapper<StockAuthor> rowMapper = new BeanPropertyRowMapper<StockAuthor>(StockAuthor.class);
		
		String sql = " SELECT * FROM heroku_55771e5bfc7407d.stock_author ";
		
		return jdbcTemplate.query(sql, sqlParameterSource, rowMapper);
	}

}
