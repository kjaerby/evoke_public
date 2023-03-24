package moe.evoke.application;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.vaadin.artur.helpers.LaunchUtil;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@EnableVaadin
public class Application extends SpringBootServletInitializer {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(Application.class, args));
    }

}
