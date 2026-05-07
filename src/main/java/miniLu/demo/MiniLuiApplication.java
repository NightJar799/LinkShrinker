package miniLu.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniLuiApplication {
	public static void main(String[] args) {
		SpringApplication.run(MiniLuiApplication.class, args);
	}

}
