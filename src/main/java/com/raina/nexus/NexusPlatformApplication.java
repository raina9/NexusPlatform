package com.raina.nexus;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NexusPlatformApplication {

	@PostConstruct
	public void checkEnv() {
		System.out.println(
				"WEATHER_API_KEY = " +
						System.getenv("WEATHER_API_KEY")
		);
	}

	public static void main(String[] args) {
		SpringApplication.run(
				NexusPlatformApplication.class,
				args
		);
	}
}