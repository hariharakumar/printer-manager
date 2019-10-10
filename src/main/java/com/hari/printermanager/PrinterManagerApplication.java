package com.hari.printermanager;

import com.btmatthews.springboot.memcached.EnableMemcached;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMemcached

public class PrinterManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrinterManagerApplication.class, args);
	}

}
