package demo.usercart.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import demo.usercart.exception.ReviewException;
import demo.usercart.exception.ReviewException.Reason;

@Component
public class ReviewImageValidator {

	// 每張最多 5 MB
	private static final int MAX_FILE_BYTES = 5 * 1024 * 1024;

	// 防止小檔案解碼成過大的圖片，占用大量記憶體
	private static final int MAX_DIMENSION = 10_000;
	private static final long MAX_PIXELS = 20_000_000L;

	public List<ValidatedImage> validate(List<MultipartFile> files, int remainingSlots) {

		if (remainingSlots < 0 || remainingSlots > 5) {
			throw invalid("圖片數量設定不正確");
		}

		// 沒上傳圖片是允許的
		if (files == null || files.isEmpty()) {
			return List.of();
		}

		if (files.size() > remainingSlots) {
			throw invalid("圖片總數最多 5 張，目前還能新增 " + remainingSlots + " 張");
		}

		List<ValidatedImage> results = new ArrayList<>();

		for (int i = 0; i < files.size(); i++) {
			results.add(validateOne(files.get(i), i + 1));
		}

		return List.copyOf(results);
	}

	private ValidatedImage validateOne(MultipartFile file, int number) {

		String label = "第 " + number + " 張圖片";

		if (file == null || file.isEmpty()) {
			throw invalid(label + "是空檔案");
		}

		if (file.getSize() > MAX_FILE_BYTES) {
			throw invalid(label + "超過 5 MB");
		}

		byte[] data;

		// 限制讀取量，最多讀 5 MB + 1 byte
		try (InputStream input = file.getInputStream()) {
			data = input.readNBytes(MAX_FILE_BYTES + 1);
		} catch (IOException e) {
			throw invalid(label + "讀取失敗，請重新上傳");
		}

		if (data.length == 0) {
			throw invalid(label + "是空檔案");
		}

		if (data.length > MAX_FILE_BYTES) {
			throw invalid(label + "超過 5 MB");
		}

		// 根據實際檔案內容辨識圖片，不相信副檔名或上傳 MIME
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);

				MemoryCacheImageInputStream imageInput = new MemoryCacheImageInputStream(bytes)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

			if (!readers.hasNext()) {
				throw invalid(label + "不是可辨識的圖片");
			}

			ImageReader reader = readers.next();

			try {
				reader.setInput(imageInput, true, true);

				String format = reader.getFormatName().toLowerCase(Locale.ROOT);

				String contentType;

				if ("jpeg".equals(format) || "jpg".equals(format)) {
					contentType = "image/jpeg";
				} else if ("png".equals(format)) {
					contentType = "image/png";
				} else {
					throw invalid(label + "格式不支援，只允許 JPEG 或 PNG");
				}

				// 先檢查尺寸，再進行完整解碼
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);

				if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
						|| (long) width * height > MAX_PIXELS) {

					throw invalid(label + "尺寸過大，單邊最多 10,000 像素，" + "總像素最多 2,000 萬");
				}

				// 解碼器若提出警告，也拒絕這個檔案
				boolean[] hasWarning = { false };

				reader.addIIOReadWarningListener((source, warning) -> hasWarning[0] = true);

				BufferedImage decoded = reader.read(0);

				if (decoded == null) {
					throw invalid(label + "無法解碼");
				}

				try {
					if (hasWarning[0]) {
						throw invalid(label + "內容可能不完整，請重新匯出後上傳");
					}
				} finally {
					decoded.flush();
				}

				return new ValidatedImage(contentType, data);

			} finally {
				reader.dispose();
			}

		} catch (IOException e) {
			throw invalid(label + "內容損壞或無法解碼，請更換檔案");
		}
	}

	private ReviewException invalid(String message) {
		return new ReviewException(Reason.INVALID_INPUT, message);
	}

	// 驗證成功後，交給 Service 儲存
	public record ValidatedImage(String contentType, byte[] data) {
	}
}