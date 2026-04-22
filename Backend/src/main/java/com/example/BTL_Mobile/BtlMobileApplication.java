package com.example.BTL_Mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BtlMobileApplication {

	public static void main(String[] args) {
		SpringApplication.run(BtlMobileApplication.class, args);
	}

}
