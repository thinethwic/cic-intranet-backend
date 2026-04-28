package com.intranet.cic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CicApplication {

	public static void main(String[] args) {
		SpringApplication.run(CicApplication.class, args);
	}

}
