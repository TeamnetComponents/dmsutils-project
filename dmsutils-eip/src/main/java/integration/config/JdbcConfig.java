package integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Configuration
@ComponentScan(basePackages = {"ro.croco.integration.service", "ro.croco.integration.config"})
public class JdbcConfig {
    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();

        return jdbcTemplate;
    }
    
    
}
