package demo.usercart.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import demo.usercart.service.ProductService;

@Component
@ConditionalOnProperty(name = "app.product-image.import-enabled", havingValue = "true", matchIfMissing = false)
public class ProductImageImportRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ProductImageImportRunner.class);

	private final ProductService productService;

	public ProductImageImportRunner(ProductService productService) {
		this.productService = productService;
	}

	@Override
	public void run(ApplicationArguments args) {

		log.info("開始轉存商品圖片");

		ProductService.ImageImportResult result = productService.importProductImages();

		log.info("商品圖片轉存完成：新增 {} 筆，略過 {} 筆", result.importedCount(), result.skippedCount());
	}
}