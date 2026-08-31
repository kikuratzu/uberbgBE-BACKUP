package com.uber.bg.uber.bg;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.uber.bg.uber.bg.Repositories.Jpa")
@EnableMongoRepositories(basePackages = "com.uber.bg.uber.bg.Repositories.Mongo")
@EnableRedisRepositories(basePackages = "com.uber.bg.uber.bg.Repositores.Redis")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
