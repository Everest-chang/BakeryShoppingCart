package demo.usercart.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.usercart.dao.ProductDao;
import demo.usercart.exception.ProductNotFoundException;
import demo.usercart.model.Product;
import demo.usercart.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	@Qualifier("productDaoMybatis")
	ProductDao productdao;

	@Value("${app.product-image.import-directory}")
	private String imageImportDirectory;

	private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

	@Override
	public List<Product> getProducts() {
		return productdao.findAll();
		// 以後如果有商業邏輯，放這裡
		// 例如：
		// 排序
		// 篩選
		// 計算折扣
		// 判斷庫存
	}

	@Override
	public Product getProductsById(Integer id) throws ProductNotFoundException {
		Product product = productdao.findById(id);
		if (product == null) {
			throw new ProductNotFoundException("找不到商品編號");
		}
		return product;
	}

	@Override
	public List<Product> getProductsByCategory(String category) throws ProductNotFoundException {
		List<Product> products = productdao.findByCategory(category);
		if (products.isEmpty()) {
			throw new ProductNotFoundException("找不到商品類別");
		}
		return products;
	}

	@Override
	@Transactional(readOnly = true)
	public ProductImageContent getProductImage(Integer productId) {

		if (productId == null || productId <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品編號不正確");
		}

		Product product = productdao.findImageById(productId);

		if (product == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到商品");
		}

		byte[] imageData = product.getImageData();

		if (imageData == null || imageData.length == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "此商品尚未儲存圖片");
		}

		String contentType = product.getImageContentType();

		if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "商品圖片格式資料不正確");
		}

		return new ProductImageContent(contentType, imageData);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ImageImportResult importProductImages() {

		try {
			// 取得來源資料夾的實際路徑
			Path directory = Path.of(imageImportDirectory).toRealPath();

			if (!Files.isDirectory(directory)) {
				throw new IllegalArgumentException("圖片來源路徑不是資料夾");
			}

			int importedCount = 0;
			int skippedCount = 0;

			for (Product product : productdao.findAll()) {

				// 查詢是否已經有圖片
				Product stored = productdao.findImageById(product.getId());

				if (stored == null) {
					throw new IllegalStateException("轉存時找不到商品：" + product.getId());
				}

				if (stored.getImageData() != null) {
					if (stored.getImageData().length == 0) {
						throw new IllegalStateException("商品 " + product.getId() + " 已有空的圖片資料，請先檢查");
					}

					skippedCount++;
					continue;
				}

				String filename = product.getImage();

				if (filename == null || filename.isBlank()) {
					throw new IllegalArgumentException("商品 " + product.getId() + " 沒有圖片檔名");
				}

				// 本次只接受單一檔名，不接受路徑
				if (filename.contains("/") || filename.contains("\\") || filename.contains(":") || ".".equals(filename)
						|| "..".equals(filename)) {

					throw new IllegalArgumentException("商品 " + product.getId() + " 的圖片檔名不合法");
				}

				Path imagePath = directory.resolve(filename).toRealPath();

				// 確認圖片實際位於來源資料夾內
				// 也避免符號連結指到其他位置
				if (!imagePath.startsWith(directory) || !Files.isRegularFile(imagePath)) {

					throw new IllegalArgumentException("商品 " + product.getId() + " 的圖片路徑不合法");
				}

				long size = Files.size(imagePath);

				if (size == 0 || size > MAX_IMAGE_BYTES) {
					throw new IllegalArgumentException(filename + " 必須大於 0 bytes，且不可超過 5 MB");
				}

				byte[] data;

				// 即使檔案在讀取期間變大，也限制讀取量
				try (InputStream input = Files.newInputStream(imagePath)) {
					data = input.readNBytes(MAX_IMAGE_BYTES + 1);
				}

				if (data.length == 0 || data.length > MAX_IMAGE_BYTES) {
					throw new IllegalArgumentException(filename + " 的圖片大小不符合限制");
				}

				String contentType = validateProductImage(data, filename);

				int affected = productdao.saveImageIfAbsent(product.getId(), data, contentType);

				if (affected == 1) {
					importedCount++;
				} else if (affected == 0) {
					// 可能有其他交易已先完成這個商品的轉存
					Product current = productdao.findImageById(product.getId());

					if (current == null || current.getImageData() == null || current.getImageData().length == 0) {

						throw new IllegalStateException("商品 " + product.getId() + " 圖片寫入失敗");
					}

					skippedCount++;
				} else {
					throw new IllegalStateException("商品 " + product.getId() + " 更新筆數異常");
				}
			}

			return new ImageImportResult(importedCount, skippedCount);

		} catch (IOException e) {
			// 轉成執行期例外並往外拋，讓整筆交易回滾
			throw new IllegalStateException("圖片讀取失敗，本次轉存已中止，請檢查來源檔案", e);
		}
	}

	private String validateProductImage(byte[] data, String filename) throws IOException {

		try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);

				MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(bytes)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

			if (!readers.hasNext()) {
				throw new IllegalArgumentException(filename + " 不是可辨識的圖片");
			}

			ImageReader reader = readers.next();

			try {
				reader.setInput(input, true, true);

				String format = reader.getFormatName().toLowerCase(Locale.ROOT);

				String contentType;

				if ("jpeg".equals(format) || "jpg".equals(format)) {
					contentType = "image/jpeg";
				} else if ("png".equals(format)) {
					contentType = "image/png";
				} else {
					throw new IllegalArgumentException(filename + " 只允許 JPEG 或 PNG");
				}

				int width = reader.getWidth(0);
				int height = reader.getHeight(0);

				if (width <= 0 || height <= 0 || width > 10_000 || height > 10_000
						|| (long) width * height > 30_000_000L) {

					throw new IllegalArgumentException(filename + " 尺寸超過限制");
				}

				boolean[] hasWarning = { false };

				reader.addIIOReadWarningListener((source, warning) -> hasWarning[0] = true);

				BufferedImage decoded = reader.read(0);

				if (decoded == null) {
					throw new IllegalArgumentException(filename + " 無法解碼");
				}

				try {
					if (hasWarning[0]) {
						throw new IllegalArgumentException(filename + " 解碼時出現警告，請檢查圖片");
					}
				} finally {
					decoded.flush();
				}

				return contentType;

			} finally {
				reader.dispose();
			}
		}
	}

}
