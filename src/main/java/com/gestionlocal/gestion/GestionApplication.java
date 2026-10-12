package com.gestionlocal.gestion;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class GestionApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
    }

	public static void main(String[] args) {
		SpringApplication.run(GestionApplication.class, args);
	}

}
