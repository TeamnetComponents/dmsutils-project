package integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by hanna.botar on 7/7/2014.
 */
@Configuration
//@ImportResource(value = {"classpath*:META-INF/integration/applicationContext.xml"})
//@ImportResource(value = {"classpath*:META-INF/integration/jdbcContext.xml"})
@ImportResource(value = {"classpath*:META-INF/integration/jmsContext.xml"})
public class IntegrationConfig {
}
