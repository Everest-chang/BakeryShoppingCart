package demo.usercart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import demo.usercart.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	List<Product> findByCategory(String category);

	// 只查詢圖片需要的欄位
	@Query("""
			SELECT
			    p.id AS id,
			    p.imageData AS imageData,
			    p.imageContentType AS imageContentType
			FROM Product p
			WHERE p.id = :id
			""")
	Optional<ProductImageView> findImageById(@Param("id") Integer id);

	// 只替尚未存入圖片的商品寫入內容
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			UPDATE Product p
			SET p.imageData = :imageData,
			    p.imageContentType = :imageContentType
			WHERE p.id = :id
			  AND p.imageData IS NULL
			""")
	int saveImageIfAbsent(@Param("id") Integer id, @Param("imageData") byte[] imageData,
			@Param("imageContentType") String imageContentType);

	// 圖片查詢結果，只包含以下三個欄位
	interface ProductImageView {

		Integer getId();

		byte[] getImageData();

		String getImageContentType();
	}

}
