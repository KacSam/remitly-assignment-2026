package com.remitly.assignment;

import org.springframework.boot.SpringApplication;

public class TestRemitlyAssignment2026Application {

	public static void main(String[] args) {
		SpringApplication.from(RemitlyAssignment2026Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
