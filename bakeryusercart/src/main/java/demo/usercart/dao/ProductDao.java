package demo.usercart.dao;

import java.util.List;

import demo.usercart.model.Product;

public interface ProductDao {
	
	List<Product> findAll();
	
	Product findById(Integer id);
	
	List<Product> findByCategory(String category);
	
	// 讀取指定商品的圖片內容與格式
	Product findImageById(Integer id);

	// 只替尚未儲存圖片的商品寫入圖片資料
	int saveImageIfAbsent(
	        Integer id,
	        byte[] imageData,
	        String imageContentType
	);

}
