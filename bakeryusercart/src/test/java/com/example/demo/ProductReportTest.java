package com.example.demo;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import demo.usercart.BakeryusercartApplication;
import demo.usercart.dao.ProductDao;
import demo.usercart.model.Product;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(classes = BakeryusercartApplication.class)
public class ProductReportTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ProductReportTest.class);

	// 使用你的 MyBatis DAO
	@Autowired
	@Qualifier("ProductDaoMybatis")
	private ProductDao productDao;

	@Test
	public void testGenerateReport() throws Exception {

		try {

			logger.info("開始執行 Product JasperReport PDF 測試...");

			// ==========================================
			// 步驟 1：從 MySQL 取得 Product 資料
			// ==========================================

			logger.info("正在從資料庫取得商品資料...");
			List<Product> products = productDao.findAll();
			logger.info("成功取得 {} 筆商品資料", products.size());
			if (products.isEmpty()) {
				logger.warn("目前 products 資料表沒有商品資料");
			}

			// 把 List<Product> 包裝成 JasperReports 可以使用的資料來源
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(products);

			// ==========================================
			// 步驟 2：讀取 ProductList.jrxml
			// ==========================================
			 // 💡 為什麼用 getResourceAsStream 而不是 new File(...)？
            // 因為 Spring Boot 上線打包成 .jar 檔後，resources 目錄會被壓縮進 jar 內部。
            // 普通的 File 讀取在打包後會引發 FileNotFoundException (找不到實體硬碟路徑)。
            // 透過 ClassLoader 的 getResourceAsStream 可以直接穿透 jar 檔，將資源轉為記憶體資料流 (Stream)，
            // 保證無論在 IDE 開發環境還是伺服器上線環境，都能 100% 成功讀取報表版型檔案！

			InputStream jrxmlStream = getClass().getResourceAsStream("/reports/Product_Report.jrxml");
			if (jrxmlStream == null) {
				throw new RuntimeException("找不到 ProductList.jrxml，" + "請確認檔案放在 " + "src/main/resources/reports/");
			}

			// 把 .jrxml 編譯成 JasperReport
			JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
			logger.info("ProductList.jrxml 編譯成功");

			// ==========================================
			// 步驟 3：準備報表參數
			// ==========================================

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("ReportTitle","潘媽媽烘焙坊 商品清單");
			// 把：
			// 1. 報表模板
			// 2. parameters
			// 3. Product 資料
			// 合在一起
			JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			logger.info("商品資料填充成功");

			// ==========================================
			// 步驟 4：產生 PDF
			// ==========================================

			String outputPath = "product_report.pdf";
			JasperExportManager.exportReportToPdfFile(print, outputPath);

			// ==========================================
			// 步驟 5：顯示 PDF 位置
			// ==========================================

			File outputFile = new File(outputPath);
			logger.info("PDF 產出完成");
			logger.info("PDF 路徑：{}", outputFile.getAbsolutePath());
			
		} catch (Exception e) {
			logger.error("Product PDF 產出失敗：", e);
			if (e.getCause() != null) {
				logger.error("詳細錯誤原因：", e.getCause());
			}
			throw e;
		}
	}
}
