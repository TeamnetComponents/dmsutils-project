package integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import java.util.Properties;

/**
 * Created by hanna.botar on 7/1/2014.
 */
@Configuration
@ComponentScan(basePackages = {"integration.service", "integration.config"})
public class JmsConfig {

   @Bean
   public JndiTemplate jndiTemplate() {
       JndiTemplate jndiTemplate = new JndiTemplate();
       Properties props = new Properties();
       props.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
//       props.setProperty("java.naming.provider.url", "iiop://dino-dev.intranet.teamnet.ro:9810/");
//       props.put(Context.PROVIDER_URL, "corbaloc:iiop:alexandruk-mbl:2809");
//       props.put(Context.PROVIDER_URL, "corbaloc:iiop:hannab-mbl:2809");
//       props.put(Context.PROVIDER_URL, "corbaloc:iiop:luciandr-mbl:2809");

       // TODO
       // PREPROD
       props.put(Context.PROVIDER_URL, "corbaloc:iiop:WPSFO:2810");

       // PROD
//       props.put(Context.PROVIDER_URL, "corbaloc:iiop:amappfo01:9810,:amappfo02:9811");

       // TST
//       props.put(Context.PROVIDER_URL, "corbaloc:iiop:WPSFOTST:2810");

       props.put("org.omg.CORBA.ORBClass","com.ibm.CORBA.iiop.ORB");

       jndiTemplate.setEnvironment(props);

       return jndiTemplate;
   }

    @Bean(name = "connectionFactory")
    public JndiObjectFactoryBean connectionFactory() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate());
//        jndiObjectFactoryBean.setJndiName("jms/CrocoDocQCF");
//        jndiObjectFactoryBean.setJndiName("jms/CrocoDocQCF1");
//        jndiObjectFactoryBean.setJndiName("jms/CrocoQCF");

        // PREPROD
        jndiObjectFactoryBean.setJndiName("jms/CrocoFOQueueCF");
//
//        // PROD
////        jndiObjectFactoryBean.setJndiName("jms/AMFOQueueCF");
//
//        // TST
////        jndiObjectFactoryBean.setJndiName("jms/AMFOQueueCF");
//
        return jndiObjectFactoryBean;
    }

    @Bean(name = "queue")
    public JndiObjectFactoryBean queue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate());
//        jndiObjectFactoryBean.setJndiName("jms/CrocoDocQueueIn");
//        jndiObjectFactoryBean.setJndiName("jms/CrocoQueue");
//        jndiObjectFactoryBean.setJndiName("jms/CrocoHQueue");

        // PREPROD
        jndiObjectFactoryBean.setJndiName("jms/CrocoFOQueue");

        // PROD
//        jndiObjectFactoryBean.setJndiName("jms/AMFOQueue");

        // TST
//        jndiObjectFactoryBean.setJndiName("jms/AMFOQueue");

        return jndiObjectFactoryBean;
    }

    @Bean
    @Inject
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination((Destination) queue().getObject());
        jmsTemplate.setConnectionFactory((ConnectionFactory) connectionFactory().getObject());
        return jmsTemplate;
    }

    /*@Bean
    public ConsumerOne consumerOne() {
        return new ConsumerOne();
    }*/

    /*@Bean
    public DefaultMessageListenerContainer defaultMessageListenerContainer() {
        DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
        dmlc.setConnectionFactory((ConnectionFactory) connectionFactory().getObject());
        dmlc.setDestination((Destination) queue().getObject());
        dmlc.setAutoStartup(false);

        return dmlc;
    }*/

}
