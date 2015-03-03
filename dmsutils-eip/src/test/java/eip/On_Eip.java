package eip;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by battamir.sugarjav on 3/3/2015.
 */
public class On_Eip {

    public static void main(String[] args){
        System.out.println("beforeApplicationContext");
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/META-INF/integration/jdbcContext.xml");
        System.out.println("afterApplicationContext");
    }

    @Test
    public void check_application_context(){
        System.out.println("beforeApplicationContext");
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/META-INF/integration/jdbcContext.xml");
        System.out.println("afterApplicationContext");
    }
}
