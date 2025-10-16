package com.nexusfi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the NexusFi application.
 * 
 * This class bootstraps the Spring Boot application and starts the embedded web server.
 * 
 * @SpringBootApplication is a convenience annotation that combines three annotations:
 * 1. @Configuration - Marks this class as a source of bean definitions
 * 2. @EnableAutoConfiguration - Tells Spring Boot to auto-configure based on dependencies
 *    (e.g., sees PostgreSQL driver → configures DataSource automatically)
 * 3. @ComponentScan - Scans com.nexusfi package and sub-packages for Spring components
 *    (finds your @Entity, @Repository, @Service, @Controller classes)
 * 
 * When this runs, Spring Boot will:
 * - Load configuration from application.yml
 * - Connect to PostgreSQL database (nexusfi)
 * - Initialize Hibernate with your JPA entities
 * - Set up Spring Security
 * - Start embedded Tomcat server on port 8080
 * 
 * @author Francisco Williams Jiménez Hernández (williamsjmzhdz)
 * @since 1.0.0
 */
@SpringBootApplication
public class NexusFiApplication {

    /**
     * Main method - the entry point when you run the application.
     * 
     * SpringApplication.run() does the heavy lifting:
     * - Creates the ApplicationContext (Spring's IoC container)
     * - Registers all beans (your entities, repositories, services)
     * - Starts the web server
     * - Keeps the application running until you stop it (Ctrl+C)
     * 
     * @param args Command-line arguments (can override properties, e.g., --server.port=9090)
     */
    public static void main(String[] args) {
        SpringApplication.run(NexusFiApplication.class, args);
    }
    
}
