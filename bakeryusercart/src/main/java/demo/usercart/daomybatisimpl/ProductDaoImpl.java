package demo.usercart.daomybatisimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.ProductDao;
import demo.usercart.mapper.ProductMapper;
import demo.usercart.model.Product;

@Repository("ProductDaoMybatis")
public class ProductDaoImpl implements ProductDao {

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

	// 讀取指定商品的圖片
	@Override
	public Product findImageById(Integer id) {
		return productMapper.findImageById(id);
	}

	// 轉存圖片，已經有圖片時不覆蓋
	@Override
	public int saveImageIfAbsent(Integer id, byte[] imageData, String imageContentType) {
		return productMapper.saveImageIfAbsent(id, imageData, imageContentType);
	}

}
