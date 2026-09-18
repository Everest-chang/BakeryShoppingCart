package demo.usercart.daojpaimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.ProductDao;
import demo.usercart.model.Product;
import demo.usercart.repository.ProductRepository;

@Repository("ProductDaoJpa")
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

	// 讀取指定商品的圖片
	@Override
	public Product findImageById(Integer id) {
		return productRepository.findImageById(id).map(image -> {
			Product product = new Product();

			product.setId(image.getId());
			product.setImageData(image.getImageData());
			product.setImageContentType(image.getImageContentType());

			return product;
		}).orElse(null);
	}

	// 轉存圖片，已經有圖片時不覆蓋
	@Override
	public int saveImageIfAbsent(Integer id, byte[] imageData, String imageContentType) {
		return productRepository.saveImageIfAbsent(id, imageData, imageContentType);
	}

}
