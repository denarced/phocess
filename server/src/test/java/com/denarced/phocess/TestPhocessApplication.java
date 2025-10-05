package com.denarced.phocess;

import org.springframework.boot.SpringApplication;

public class TestPhocessApplication {

    public static void main(String[] args) {
        SpringApplication.from(PhocessApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
