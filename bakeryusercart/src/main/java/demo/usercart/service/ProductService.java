package demo.usercart.service;

import java.util.List;
import java.util.Optional;

import demo.usercart.exception.ProductNotFoundException;
import demo.usercart.model.Product;

public interface ProductService {

	List<Product> getProducts();

	Product getProductsById(Integer id) throws ProductNotFoundException;

	List<Product> getProductsByCategory(String category) throws ProductNotFoundException;

	// 讀取商品圖片，供圖片 API 使用
	ProductImageContent getProductImage(Integer productId);

	// 從設定的資料夾轉存商品圖片
	ImageImportResult importProductImages();

	// 圖片內容，供 Controller 回傳使用，不直接轉成 JSON
	record ProductImageContent(String contentType, byte[] data) {
	}

	// 轉存完成後的結果
	record ImageImportResult(int importedCount, int skippedCount) {
	}

}
