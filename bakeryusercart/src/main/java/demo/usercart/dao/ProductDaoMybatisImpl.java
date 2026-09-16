package demo.usercart.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.mapper.ProductMapper;
import demo.usercart.model.Product;

@Repository("productDaoMybatis")
public class ProductDaoMybatisImpl implements ProductDao {

	@Autowired
	private ProductMapper productMapper;
	
	@Override
	public List<Product> findAll() {
		return productMapper.findAll();
	}

	@Override
	public Product findById(Integer id) {
		return productMapper.findById(id);
	}

	@Override
	public List<Product> findByCategory(String category) {
		return productMapper.findByCategory(category);
	}

}
