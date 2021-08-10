package com.teste.testejwt;

import com.teste.testejwt.domain.AppUser;
import com.teste.testejwt.domain.Role;
import com.teste.testejwt.service.AppUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
public class TesteJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(TesteJwtApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(AppUserService appUserService) {
		return args -> {
			appUserService.saveRole(new Role(null, "ROLE_USER"));
			appUserService.saveRole(new Role(null, "ROLE_MANAGER"));
			appUserService.saveRole(new Role(null, "ROLE_ADMIN"));
			appUserService.saveRole(new Role(null, "ROLE_SUPERADMIN"));

			appUserService.saveAppUser(new AppUser(null, "John Peter", "john", "123", new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "Travis Scott", "travis", "123", new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "Will Smith", "william", "123", new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "Jorge Da Silva", "jorgin", "123", new ArrayList<>()));

			appUserService.addRoleToAppUser("john", "ROLE_USER");
			appUserService.addRoleToAppUser("travis", "ROLE_MANAGER");
			appUserService.addRoleToAppUser("william", "ROLE_ADMIN");
			appUserService.addRoleToAppUser("jorgin", "ROLE_USER");
			appUserService.addRoleToAppUser("jorgin", "ROLE_SUPERADMIN");
			appUserService.addRoleToAppUser("jorgin", "ROLE_ADMIN");
		};
	}

}
