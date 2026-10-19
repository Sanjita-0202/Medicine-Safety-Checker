package com.medicinesafetychecker.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

// @ServletComponentScan picks up PrescriptionServlet, a plain @WebServlet
// (not a Spring @Controller) registered directly with the embedded container.
@ServletComponentScan
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
