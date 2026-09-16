package demo.usercart.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.usercart.dao.ProductDao;
import demo.usercart.model.Product;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@RestController
@RequestMapping("/api/products")
public class ProductReportController {

    @Autowired
    @Qualifier("productDaoMybatis")
    private ProductDao productDao;

    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadProductReport() throws Exception {

        // 1. 查詢商品
        List<Product> products = productDao.findAll();

        // 2. 轉成 JasperReports 資料來源
        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(products);

        // 3. 讀取 ProductReport.jrxml
        InputStream inputStream =
                getClass().getResourceAsStream(
                        "/reports/Product_Report.jrxml"
                );

        if (inputStream == null) {
            throw new RuntimeException("找不到 ProductReport.jrxml");
        }

        // 4. 編譯 jrxml
        JasperReport jasperReport =
                JasperCompileManager.compileReport(inputStream);

        // 5. 報表參數
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(
                "ReportTitle",
                "潘媽媽烘焙坊  商品清單"
        );

        // 6. 填入商品資料
        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource
                );

        // 7. 產生 PDF
        byte[] pdf =
                JasperExportManager.exportReportToPdf(
                        jasperPrint
                );

        // 8. 回傳 PDF 給瀏覽器
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ProductReport.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
