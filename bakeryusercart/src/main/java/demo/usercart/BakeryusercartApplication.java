package demo.usercart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("demo.usercart.mapper")
public class BakeryusercartApplication {

	public static void main(String[] args) {
		SpringApplication.run(BakeryusercartApplication.class, args);
	}

}
