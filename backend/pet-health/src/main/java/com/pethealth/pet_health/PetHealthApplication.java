package com.pethealth.pet_health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling 
public class PetHealthApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetHealthApplication.class, args);
	}

}
