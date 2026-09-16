package demo.usercart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "products")
public class Product {

	@Id
	private Integer id;

	@Column(nullable = false, length = 255)
	String title;

	@Column(length = 2048)
	String description;

	@Column(length = 255)
	String category;

	double price;

	@Column(length = 255)
	private String image;
	
	@Lob
	@JsonIgnore
	@ToString.Exclude
	@Column(name = "image_data", columnDefinition = "LONGBLOB")
	private byte[] imageData;

	// 圖片格式，例如 image/jpeg
	@Column(name = "image_content_type", length = 50)
	private String imageContentType;
}
