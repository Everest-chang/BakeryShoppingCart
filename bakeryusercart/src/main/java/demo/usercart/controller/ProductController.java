package demo.usercart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.usercart.model.Product;
import demo.usercart.repository.ProductRepository;
import demo.usercart.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

	@Autowired
	ProductService productService;

	@GetMapping
	public List<Product> getProducts() {
		List<Product> products = productService.getProducts();
		return products;
	}

	@GetMapping("/productid/{productid}")
	public Product getProductsById(@PathVariable Integer productid) {

		return productService.getProductsById(productid);
	}

	@GetMapping("/category/{category}")
	public List<Product> getProductsByCategory(@PathVariable String category) {
		List<Product> products = productService.getProductsByCategory(category);
		return products;
	}

	// 公開商品圖片，不需要登入
	@GetMapping("/{productId}/image")
	public ResponseEntity<byte[]> getProductImage(@PathVariable Integer productId) {
		ProductService.ProductImageContent image = productService.getProductImage(productId);

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
				.contentLength(image.data().length).header("X-Content-Type-Options", "nosniff")
				.cacheControl(CacheControl.noStore()).body(image.data());
	}

}
