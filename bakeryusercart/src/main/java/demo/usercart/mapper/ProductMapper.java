package demo.usercart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import demo.usercart.model.Product;

@Mapper
public interface ProductMapper {

	// 商品基本資料，不讀取圖片二進位內容
	List<Product> findAll();

	Product findById(@Param("id") Integer id);

	List<Product> findByCategory(@Param("category") String category);

	// 查詢指定商品的圖片內容與格式
	Product findImageById(@Param("id") Integer id);

	// 將資料夾圖片轉存至資料庫
	// 只更新尚未儲存圖片的商品，避免重複轉存時覆蓋
	int saveImageIfAbsent(@Param("id") Integer id, @Param("imageData") byte[] imageData,
			@Param("imageContentType") String imageContentType);
}