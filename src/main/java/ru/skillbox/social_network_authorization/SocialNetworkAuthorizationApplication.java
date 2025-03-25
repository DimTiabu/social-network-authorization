package ru.skillbox.social_network_authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SocialNetworkAuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialNetworkAuthorizationApplication.class, args);
	}

}