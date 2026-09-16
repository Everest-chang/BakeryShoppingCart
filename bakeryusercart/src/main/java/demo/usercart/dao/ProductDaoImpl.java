package demo.usercart.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.model.Product;
import demo.usercart.repository.ItemRepository;
import demo.usercart.repository.ProductRepository;

@Repository("productDaoJpa")
public class ProductDaoImpl implements ProductDao {

	@Autowired
	ProductRepository productRepository;
	
	@Override
	public List<Product> findAll() {
		return productRepository.findAll();
	}

	@Override
	public Product findById(Integer id) {
		return productRepository.findById(id).orElse(null);
	}

	@Override
	public List<Product> findByCategory(String category) {
		return productRepository.findByCategory(category);
	}

}
